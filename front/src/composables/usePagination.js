import { ref, computed } from 'vue'

export function usePagination(list, itemsPerPage, unit = '건') {
  const currentPage = ref(1)

  const totalPages = computed(() => Math.ceil(list.value.length / itemsPerPage))

  const pagedList = computed(() => {
    const start = (currentPage.value - 1) * itemsPerPage
    return list.value.slice(start, start + itemsPerPage)
  })

  const paginationItems = computed(() => {
    const total = totalPages.value
    const current = currentPage.value
    if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1)
    const items = [1]
    if (current > 3) items.push('...')
    const start = Math.max(2, current - 1)
    const end = Math.min(total - 1, current + 1)
    for (let i = start; i <= end; i++) items.push(i)
    if (current < total - 2) items.push('...')
    items.push(total)
    return items
  })

  const paginationInfo = computed(() => {
    const total = list.value.length
    const start = (currentPage.value - 1) * itemsPerPage + 1
    const end = Math.min(currentPage.value * itemsPerPage, total)
    return `${start}–${end} / 전체 ${total}${unit}`
  })

  const changePage = (page) => {
    if (page >= 1 && page <= totalPages.value) currentPage.value = page
  }

  return { currentPage, totalPages, pagedList, paginationItems, paginationInfo, changePage }
}
