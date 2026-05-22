<template>
    <router-link
      :to="{ name: 'BookInfo', params: { id: book.seqBook } }"
      class="book-area"
      :class="{ 
        'borrowed': book.bookBorrowed && !book.borrowedByMe,
        'my-borrowed': book.borrowedByMe 
      }"
    >
        <div class="isBooked">
            <div class="img-area">
                <img
                    v-if="hasImage"
                    :src="book.imageBook"
                    :alt="book.titleBook"
                    @error="onImageError"
                />
                <div v-else class="book-cover-placeholder" :style="placeholderStyle">
                    <div class="cover-spine"></div>
                    <div class="cover-body">
                        <div class="cover-category">{{ book.subtitleName || book.subjectName || '' }}</div>
                        <div class="cover-title">{{ book.titleBook }}</div>
                        <div class="cover-author">{{ book.authorBook }}</div>
                        <div class="cover-publisher">{{ book.publisherBook }}</div>
                    </div>
                </div>
                
                <!-- 다른 사람이 대출중인 경우 오버레이 -->
                <div v-if="book.bookBorrowed && !book.borrowedByMe" class="borrowed-overlay">
                    <div class="borrowed-badge">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" fill="currentColor"/>
                            <path d="M9 12L11 14L15 10" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        <span class="borrowed-text">대출 중</span>
                    </div>
                    <div class="borrowed-dimmer"></div>
                </div>

                <!-- 내가 대출중인 경우 오버레이 -->
                <div v-if="book.borrowedByMe" class="my-borrowed-overlay">
                    <div class="my-borrowed-badge">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        <span class="my-borrowed-text">대출 중</span>
                    </div>
                </div>
            </div>
            <div class="book-info">
                <div class="title">{{ book.titleBook }}</div>
                <div class="book-info-footer">
                    <div class="author">{{ book.authorBook }}</div>
                </div>
            </div>
        </div>
    </router-link>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
    book: { type: Object, required: true, default: () => ({}) }
})

const imageError = ref(false)
const hasImage = computed(() => !imageError.value && props.book.imageBook && props.book.imageBook.trim() !== '')
const onImageError = () => { imageError.value = true }

// 카테고리별 색상 팔레트 (배경 + 스파인 + 텍스트)
const PALETTES = [
  { bg: '#1e3a5f', spine: '#162d4a', text: '#c8dcf0', accent: '#7ab3d4' },
  { bg: '#2d4a22', spine: '#213617', text: '#c4dbb8', accent: '#88bf6e' },
  { bg: '#4a2040', spine: '#361730', text: '#ddb8d4', accent: '#c47aaa' },
  { bg: '#3a3010', spine: '#2a2208', text: '#ddd4a0', accent: '#c4aa44' },
  { bg: '#1a3a3a', spine: '#122a2a', text: '#a8d4d4', accent: '#5ab4b4' },
  { bg: '#3a1a10', spine: '#2a1008', text: '#ddb8a8', accent: '#c47a5a' },
  { bg: '#252545', spine: '#1a1a34', text: '#b8b8e0', accent: '#7878c8' },
  { bg: '#283828', spine: '#1a2a1a', text: '#b8d4b8', accent: '#68a868' },
]

const placeholderStyle = computed(() => {
  const idx = (props.book.seqBook || 0) % PALETTES.length
  const p = PALETTES[idx]
  return { '--cover-bg': p.bg, '--cover-spine': p.spine, '--cover-text': p.text, '--cover-accent': p.accent }
})

</script>

<style scoped>
.book-area {
    display: block;
    background: var(--pb-color-surface);
    border-radius: var(--pb-radius-md);
    text-decoration: none;
    transition: box-shadow 0.15s;
    overflow: hidden;
}

.book-area:hover {
    box-shadow: var(--pb-shadow-sm);
}

.book-area.borrowed,
.book-area.my-borrowed {
    position: relative;
}

.isBooked {
    display: flex;
    flex-direction: column;
}

.img-area {
    width: 100%;
    aspect-ratio: 2 / 3;
    background: var(--pb-color-surface-subtle);
    position: relative;
    overflow: hidden;
}

.img-area img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.2s ease;
}

.book-area:hover .img-area img {
    transform: scale(1.03);
}

/* ── 커버 플레이스홀더 ── */
.book-cover-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    background: var(--cover-bg);
    position: relative;
    user-select: none;
}

.cover-spine {
    width: 12px;
    flex-shrink: 0;
    background: var(--cover-spine);
    box-shadow: inset -2px 0 4px rgba(0,0,0,0.25);
}

.cover-body {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
    padding: 16px 14px 18px;
    gap: 6px;
    background: linear-gradient(175deg, transparent 40%, rgba(0,0,0,0.35) 100%);
}

.cover-category {
    font-size: 9px;
    font-weight: 600;
    letter-spacing: 0.1em;
    text-transform: uppercase;
    color: var(--cover-accent);
    opacity: 0.9;
}

.cover-title {
    font-size: 14px;
    font-weight: 700;
    color: var(--cover-text);
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
    word-break: keep-all;
}

.cover-author {
    font-size: 11px;
    color: var(--cover-accent);
    font-weight: 500;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.cover-publisher {
    font-size: 9px;
    color: var(--cover-text);
    opacity: 0.5;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

/* 다른 사람이 대여중인 경우 오버레이 스타일 */
.borrowed-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 10;
}

.borrowed-dimmer {
    position: absolute;
    inset: 0;
    background: rgba(30, 31, 29, 0.35);
}

.borrowed-badge {
    position: absolute;
    top: 8px;
    right: 8px;
    background: var(--pb-color-danger);
    color: #fff;
    padding: 3px 8px;
    border-radius: 999px;
    font-size: 11px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 4px;
    z-index: 11;
}

.borrowed-text {
    font-size: 11px;
}

/* 내가 대여중인 경우 */
.my-borrowed-overlay {
    position: absolute;
    inset: 0;
    z-index: 10;
}

.my-borrowed-badge {
    position: absolute;
    top: 8px;
    right: 8px;
    background: var(--pb-color-success);
    color: #fff;
    padding: 3px 8px;
    border-radius: 999px;
    font-size: 0.75rem;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 4px;
    box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
    animation: pulse-green 2s infinite;
    z-index: 11;
}

.my-borrowed-text {
    font-size: 11px;
}

.book-info {
    padding: 10px 12px 12px;
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.title {
    font-size: 13px;
    font-weight: 600;
    color: var(--pb-color-heading);
    line-height: 1.45;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
}

.book-area:hover .title {
    color: var(--pb-color-brand);
}

.book-info-footer {
    display: flex;
}

.author {
    font-size: 12px;
    color: var(--pb-color-text-soft);
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
    .borrowed-badge, .my-borrowed-badge {
        top: 8px;
        right: 8px;
        padding: 4px 8px;
        font-size: 0.7rem;
    }
    
    .borrowed-text, .my-borrowed-text {
        font-size: 0.65rem;
    }
}

/* Playbook design baseline overrides */
.book-area {
    display: block;
    width: 100%;
    background: transparent;
    border-radius: var(--pb-radius-md);
    color: inherit;
}

.book-area:hover {
    color: inherit;
}

.book-area:hover .title {
    color: var(--pb-color-brand-strong);
    text-decoration: none;
}

.isBooked {
    gap: 12px;
}

.img-area {
    aspect-ratio: 3 / 4.35;
    width: 100%;
    height: auto;
    background: var(--pb-color-surface-muted);
    border-radius: var(--pb-radius-sm);
    box-shadow: var(--pb-shadow-xs);
}

.img-area img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.book-area:hover .img-area {
    box-shadow: var(--pb-shadow-sm);
}

.book-info {
    min-height: 74px;
    padding: 0;
    gap: 4px;
}

.title {
    color: var(--pb-color-heading);
    font-size: 0.98rem;
    line-height: 1.38;
    letter-spacing: 0;
}

.author {
    color: var(--pb-color-text-muted);
    font-size: 0.85rem;
    letter-spacing: 0;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 1;
    -webkit-box-orient: vertical;
}

.borrowed-dimmer {
    background: rgba(31, 42, 36, 0.42);
    backdrop-filter: blur(1px);
}

.borrowed-badge,
.my-borrowed-badge {
    top: 10px;
    right: 10px;
    border-radius: 999px;
    box-shadow: var(--pb-shadow-xs);
    animation: none;
}

.borrowed-badge {
    background: var(--pb-color-danger);
}

.my-borrowed-badge {
    background: var(--pb-color-success);
}

@media (max-width: 768px) {
    .isBooked {
        gap: 9px;
    }

    .book-info {
        min-height: 66px;
    }

    .title {
        font-size: 0.9rem;
    }

    .author {
        font-size: 0.78rem;
    }
}</style>
