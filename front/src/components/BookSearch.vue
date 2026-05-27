<template>
  <div class="search-root" ref="searchRoot">

    <!-- ── 통합 검색 바 ── -->
    <div class="search-bar" :class="{ 'is-focused': isFocused, 'has-value': !!query }">

      <!-- 검색 타입 드롭다운 -->
      <div class="type-select" ref="typeSelect">
        <button
          type="button"
          class="type-btn"
          @click.stop="toggleTypeMenu"
          :aria-expanded="typeMenuOpen"
        >
          <span class="type-label">{{ currentType.label }}</span>
          <svg class="chevron" :class="{ open: typeMenuOpen }"
            width="12" height="12" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2.5"
            stroke-linecap="round" stroke-linejoin="round">
            <polyline points="6 9 12 15 18 9"/>
          </svg>
        </button>

        <ul v-if="typeMenuOpen" class="type-menu" role="listbox">
          <li
            v-for="t in searchTypes"
            :key="t.value"
            class="type-menu-item"
            :class="{ selected: t.value === currentType.value }"
            role="option"
            :aria-selected="t.value === currentType.value"
            @mousedown.prevent="selectType(t)"
          >
            <svg v-if="t.value === currentType.value"
              class="check-icon" width="12" height="12" viewBox="0 0 24 24"
              fill="none" stroke="currentColor" stroke-width="2.5"
              stroke-linecap="round" stroke-linejoin="round">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
            <span v-else class="check-placeholder"></span>
            {{ t.label }}
          </li>
        </ul>
      </div>

      <!-- 구분선 -->
      <span class="search-divider"></span>

      <!-- 검색 아이콘 -->
      <span class="search-icon-wrap">
        <svg v-if="!isLoading" width="15" height="15" viewBox="0 0 24 24"
          fill="none" stroke="currentColor" stroke-width="2"
          stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
        </svg>
        <span v-else class="spinner"></span>
      </span>

      <!-- 입력 필드 -->
      <input
        ref="searchInput"
        type="text"
        class="search-input"
        :placeholder="`${currentType.label} 검색`"
        v-model="query"
        @input="onInput"
        @focus="onFocus"
        @blur="onBlur"
        @keydown="handleKeyDown"
        autocomplete="off"
        spellcheck="false"
      />

      <!-- 지우기 버튼 -->
      <button
        v-if="query"
        type="button"
        class="clear-btn"
        @mousedown.prevent="clearQuery"
        aria-label="검색어 지우기"
      >
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2.5"
          stroke-linecap="round" stroke-linejoin="round">
          <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </button>
    </div>

    <!-- ── 연관검색어 드롭다운 ── -->
    <ul
      v-if="isFocused && suggestions.length"
      class="suggestions"
      role="listbox"
      ref="suggestionList"
    >
      <li class="suggestions-header">
        <span>연관 도서</span>
      </li>
      <li
        v-for="(item, idx) in suggestions"
        :key="`s-${idx}`"
        class="suggestion-item"
        :class="{ active: idx === selectedIndex }"
        role="option"
        @mousedown.prevent="selectSuggestion(item)"
        @mouseenter="selectedIndex = idx"
      >
        <svg class="suggestion-icon" width="13" height="13" viewBox="0 0 24 24"
          fill="none" stroke="currentColor" stroke-width="2"
          stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
        </svg>
        <span class="suggestion-text" v-html="highlight(item)"></span>
      </li>
      <li v-if="query" class="suggestion-search-all" @mousedown.prevent="submitSearch">
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2"
          stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
        </svg>
        <span><strong>{{ query }}</strong> 전체 검색</span>
      </li>
    </ul>

    <!-- 입력값 있고 포커스지만 결과 없을 때 -->
    <ul v-else-if="isFocused && query && !isLoading && suggestions.length === 0" class="suggestions">
      <li class="suggestions-empty">결과 없음</li>
    </ul>

  </div>
</template>

<script>
import { swAlert } from '@/utils/sweetAlert'
import * as bookApi from '@/api/book'

const SEARCH_TYPES = [
  { value: 'title',     label: '도서명' },
  { value: 'author',    label: '저자' },
  { value: 'isbn',      label: 'ISBN' },
  { value: 'publisher', label: '출판사' },
]

export default {
  name: 'BookSearch',
  emits: ['search'],
  data() {
    return {
      searchTypes: SEARCH_TYPES,
      currentType: SEARCH_TYPES[0],
      typeMenuOpen: false,
      query: '',
      suggestions: [],
      isFocused: false,
      selectedIndex: -1,
      isLoading: false,
      debounceTimer: null,
    }
  },
  mounted() {
    document.addEventListener('mousedown', this.onOutsideClick)
  },
  beforeUnmount() {
    document.removeEventListener('mousedown', this.onOutsideClick)
  },
  methods: {
    toggleTypeMenu() {
      this.typeMenuOpen = !this.typeMenuOpen
    },
    selectType(type) {
      this.currentType = type
      this.typeMenuOpen = false
      this.suggestions = []
      this.selectedIndex = -1
      this.$refs.searchInput?.focus()
      if (this.query) this.fetchSuggestions()
    },
    onOutsideClick(e) {
      if (!this.$refs.searchRoot?.contains(e.target)) {
        this.typeMenuOpen = false
        this.isFocused = false
        this.suggestions = []
        this.selectedIndex = -1
      }
    },
    highlight(text) {
      if (!this.query) return text
      const escaped = this.query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
      return text.replace(
        new RegExp(`(${escaped})`, 'gi'),
        '<mark>$1</mark>'
      )
    },
    async fetchSuggestions() {
      if (!this.query.trim()) {
        this.suggestions = []
        this.selectedIndex = -1
        return
      }
      this.isLoading = true
      try {
        const res = await bookApi.getRelated({ q: this.query, type: this.currentType.value })
        const data = res.data.data || []
        const fieldMap = { title: 'titleBook', author: 'authorBook', isbn: 'isbnBook', publisher: 'publisherBook' }
        const field = fieldMap[this.currentType.value] || 'titleBook'
        const available = data.filter(b => b.printCheckBook === true)
        const unique = [...new Set(available.map(b => b[field]).filter(Boolean))]
        this.suggestions = unique.slice(0, 7)
        this.selectedIndex = -1
      } catch {
        this.suggestions = []
      } finally {
        this.isLoading = false
      }
    },
    selectSuggestion(text) {
      this.query = text
      this.suggestions = []
      this.selectedIndex = -1
      this.isFocused = false
      this.$refs.searchInput?.blur()
      this.$emit('search', { query: text, type: this.currentType.value, exact: true })
    },
    submitSearch() {
      this.suggestions = []
      this.selectedIndex = -1
      this.isFocused = false
      this.$refs.searchInput?.blur()
      this.$emit('search', { query: this.query, type: this.currentType.value, exact: false })
    },
    clearQuery() {
      this.query = ''
      this.suggestions = []
      this.selectedIndex = -1
      this.$refs.searchInput?.focus()
      this.$emit('search', { query: '', type: this.currentType.value, exact: false })
    },
    onInput() {
      clearTimeout(this.debounceTimer)
      this.debounceTimer = setTimeout(() => this.fetchSuggestions(), 220)
    },
    onFocus() {
      this.isFocused = true
      this.typeMenuOpen = false
      if (this.query) this.fetchSuggestions()
    },
    onBlur() {
      // onOutsideClick handles close; keep brief delay for click on suggestion
    },
    handleKeyDown(event) {
      if (this.typeMenuOpen) {
        if (event.key === 'Escape') { this.typeMenuOpen = false }
        return
      }
      if (!this.suggestions.length) {
        if (event.key === 'Enter') { event.preventDefault(); this.submitSearch() }
        return
      }
      switch (event.key) {
        case 'ArrowDown':
          event.preventDefault()
          this.selectedIndex = Math.min(this.selectedIndex + 1, this.suggestions.length - 1)
          this.scrollToSelected()
          break
        case 'ArrowUp':
          event.preventDefault()
          this.selectedIndex = Math.max(this.selectedIndex - 1, -1)
          this.scrollToSelected()
          break
        case 'Enter':
          event.preventDefault()
          if (this.selectedIndex >= 0) {
            this.selectSuggestion(this.suggestions[this.selectedIndex])
          } else {
            this.submitSearch()
          }
          break
        case 'Escape':
          event.preventDefault()
          this.suggestions = []
          this.selectedIndex = -1
          this.isFocused = false
          this.$refs.searchInput?.blur()
          break
      }
    },
    scrollToSelected() {
      this.$nextTick(() => {
        const list = this.$refs.suggestionList
        const item = list?.children[this.selectedIndex + 1] // +1 for header li
        item?.scrollIntoView({ block: 'nearest' })
      })
    },
  },
}
</script>

<style scoped>
/* ── 루트 ── */
.search-root {
  position: relative;
  width: 100%;
}

/* ── 검색 바 ── */
.search-bar {
  display: flex;
  align-items: center;
  height: 36px;
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-md);
  transition: border-color 0.12s ease, box-shadow 0.12s ease;
  overflow: visible;
  position: relative;
}
.search-bar.is-focused {
  border-color: var(--pb-color-brand);
  box-shadow: 0 0 0 3px var(--pb-color-brand-soft);
}

/* ── 타입 드롭다운 ── */
.type-select {
  position: relative;
  flex-shrink: 0;
}
.type-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  height: 34px;
  padding: 0 10px 0 12px;
  border: none;
  background: transparent;
  color: var(--pb-color-text-muted);
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  white-space: nowrap;
  border-radius: var(--pb-radius-md) 0 0 var(--pb-radius-md);
  transition: background 0.1s ease;
}
.type-btn:hover {
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text);
}
.type-label { letter-spacing: 0; }
.chevron {
  transition: transform 0.15s ease;
  color: var(--pb-color-text-soft);
}
.chevron.open { transform: rotate(180deg); }

/* 타입 메뉴 */
.type-menu {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  min-width: 108px;
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-md);
  box-shadow: var(--pb-shadow-popover);
  list-style: none;
  padding: 4px;
  margin: 0;
  z-index: 200;
}
.type-menu-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 10px;
  font-size: 12px;
  color: var(--pb-color-text);
  border-radius: var(--pb-radius-sm);
  cursor: pointer;
  transition: background 0.1s ease;
}
.type-menu-item:hover {
  background: var(--pb-color-surface-muted);
}
.type-menu-item.selected {
  color: var(--pb-color-brand);
  font-weight: 500;
}
.check-icon { color: var(--pb-color-brand); flex-shrink: 0; }
.check-placeholder { width: 12px; flex-shrink: 0; }

/* ── 구분선 ── */
.search-divider {
  width: 1px;
  height: 16px;
  background: var(--pb-color-border);
  flex-shrink: 0;
}

/* ── 검색 아이콘 ── */
.search-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8px 0 10px;
  color: var(--pb-color-text-soft);
  flex-shrink: 0;
}

/* 로딩 스피너 */
.spinner {
  width: 13px;
  height: 13px;
  border: 2px solid var(--pb-color-border);
  border-top-color: var(--pb-color-brand);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  display: block;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ── 입력 필드 ── */
.search-input {
  flex: 1;
  height: 100%;
  border: none;
  background: transparent;
  outline: none;
  font-size: 13px;
  color: var(--pb-color-text);
  min-width: 0;
  padding: 0;
}
.search-input::placeholder {
  color: var(--pb-color-text-soft);
}

/* ── 지우기 버튼 ── */
.clear-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border: none;
  background: transparent;
  color: var(--pb-color-text-soft);
  border-radius: var(--pb-radius-xs);
  cursor: pointer;
  margin-right: 6px;
  flex-shrink: 0;
  transition: background 0.1s ease, color 0.1s ease;
}
.clear-btn:hover {
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text);
}

/* ── 연관검색어 드롭다운 ── */
.suggestions {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-md);
  box-shadow: var(--pb-shadow-popover);
  list-style: none;
  padding: 4px;
  margin: 0;
  z-index: 150;
  max-height: 280px;
  overflow-y: auto;
}

.suggestions-header {
  padding: 6px 10px 4px;
  font-size: 10px;
  font-weight: 600;
  color: var(--pb-color-text-soft);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  cursor: default;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 10px;
  border-radius: var(--pb-radius-sm);
  cursor: pointer;
  transition: background 0.1s ease;
}
.suggestion-item:hover,
.suggestion-item.active {
  background: var(--pb-color-surface-muted);
}
.suggestion-icon {
  color: var(--pb-color-text-soft);
  flex-shrink: 0;
}
.suggestion-text {
  font-size: 13px;
  color: var(--pb-color-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.suggestion-text :deep(mark) {
  background: none;
  color: var(--pb-color-brand);
  font-weight: 600;
}

.suggestion-search-all {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 10px;
  border-radius: var(--pb-radius-sm);
  cursor: pointer;
  font-size: 13px;
  color: var(--pb-color-text-muted);
  border-top: 1px solid var(--pb-color-border);
  margin-top: 2px;
  transition: background 0.1s ease;
}
.suggestion-search-all:hover { background: var(--pb-color-surface-muted); }
.suggestion-search-all strong { color: var(--pb-color-text); font-weight: 600; }
.suggestion-search-all svg { color: var(--pb-color-text-soft); flex-shrink: 0; }

.suggestions-empty {
  padding: 16px 10px;
  font-size: 13px;
  color: var(--pb-color-text-soft);
  text-align: center;
  cursor: default;
}
</style>
