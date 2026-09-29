import { ref, computed } from 'vue'

export function usePagination(list, itemsPerPage, unit = '건') {
  const currentPage = ref(1)

  const totalPages = computed(() => Math.ceil(list.value.length / itemsPerPage))

  const pagedList = computed(() => {
    const start = (currentPage.value - 1) * itemsPerPage
    return list.value.slice(start, start + itemsPerPage)
  })


  const paginationInfo = computed(() => {
    const total = list.value.length
    const start = (currentPage.value - 1) * itemsPerPage + 1
    const end = Math.min(currentPage.value * itemsPerPage, total)
    return `${start}–${end} / ${total}${unit}`
  })

  const changePage = (page) => {
    if (page >= 1 && page <= totalPages.value) currentPage.value = page
  }

  return { currentPage, totalPages, pagedList, paginationInfo, changePage }
}
