<template>
  <!-- 대분류 리스트 -->
  <nav class="bg-light px-4 py-2 fixed-top w-100" style="top: 72px; z-index: 1030;">
    <ul class="nav">
      <li class="nav-item">
        <a
          class="nav-link"
          :class="{ active: selectedLargeCategory === '전체' }"
          href="#"
          @click.prevent="selectLargeCategory('전체')"
        >
          전체
        </a>
      </li>
      <li class="nav-item" v-for="(large, index) in largeCategories" :key="index">
        <a
          class="nav-link"
          :class="{ active: selectedLargeCategory === large.nameSortFirst }"
          href="#"
          @click.prevent="selectLargeCategory(large.nameSortFirst)"
        >
          {{ large.korSortFirst }}
        </a>
      </li>
    </ul>
  </nav>

  <!-- 중분류 드롭다운 리스트 -->
  <ul
    v-if="selectedLargeCategory"
    class="dropdown-menu-custom"
  >
    <li
      class="dropdown-item-custom"
      v-for="(medium, idx) in getMediumOptions(selectedLargeCategory)"
      :key="idx"
    >
      {{ medium.korSortSecond }}
    </li>
  </ul>

  <!-- 본문 영역 -->
  <main style="padding-top: 200px;">
    <h3>{{ selectedLargeCategory }} 카테고리 콘텐츠</h3>
  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const largeCategories = ref([])
const mediumCategoriesAll = ref([])

const selectedLargeCategory = ref('')

const fetchLargeCategories = async () => {
  const res = await fetch('http://localhost:8080/subjects')
  largeCategories.value = await res.json()
}

const fetchMediumCategories = async () => {
  const res = await fetch('http://localhost:8080/subtitles')
  mediumCategoriesAll.value = await res.json()
}

const getMediumOptions = (largeCode) => {
  const large = largeCategories.value.find(l => l.nameSortFirst === largeCode)
  if (!large) return []
  return mediumCategoriesAll.value.filter(m => m.seqSortFirst === large.seqSortFirst)
}

function selectLargeCategory(categoryName) {
  selectedLargeCategory.value = categoryName
}

onMounted(() => {
  fetchLargeCategories()
  fetchMediumCategories()

  selectedLargeCategory.value = '전체'
})
</script>

<style scoped>
.nav-link {
  font-weight: bold;
  color: #555;
  cursor: pointer;
  margin: 0 1vw;
}

.nav-link.active {
  color: #42bbb2;
  border-bottom: 2px solid #42bbb2;
}

.dropdown-menu-custom {
  list-style: none;
  padding: 0.5rem 1rem;
  background-color: #f9f9f9;
  border-top: 1px solid #ddd;
  margin: 0;
  position: fixed;
  top: 130px;
  left: 0;
  width: 100%;
  z-index: 1029;
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.dropdown-item-custom {
  padding: 0.5rem 1rem;
  background-color: white;
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  cursor: pointer;
}

.dropdown-item-custom:hover {
  background-color: #e8f6f5;
}
</style>
