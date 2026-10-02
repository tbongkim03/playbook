import { ref } from 'vue'
import * as adminApi from '@/api/admin'
import * as campusApi from '@/api/campus'

/**
 * 관리자 캠퍼스 필터 상태와 admin/me 조회를 공통화한 composable.
 * @param {boolean} showFilterForCampusAdmin - 캠퍼스 관리자일 때 필터를 표시할지 여부 (기본 true)
 */
export function useAdminCampusFilter({ showFilterForCampusAdmin = true } = {}) {
  const showCampusFilter = ref(false)
  const currentUserCampusId = ref(null)
  const selectedCampus = ref('')
  const campuses = ref([])

  const fetchAdminInfo = async () => {
    try {
      if (!sessionStorage.getItem('userType')) return null
      const response = await adminApi.getMe()
      const data = response.data.data

      if (!data.seqCampus) {
        showCampusFilter.value = true
        currentUserCampusId.value = null
        selectedCampus.value = ''
      } else {
        const id = data.seqCampus.seqCampus || data.seqCampus
        showCampusFilter.value = showFilterForCampusAdmin
        currentUserCampusId.value = id
        selectedCampus.value = String(id)
      }

      return data
    } catch (error) {
      console.error('관리자 정보 조회 실패:', error)
      return null
    }
  }

  const fetchCampuses = async () => {
    try {
      const res = await campusApi.getAll()
      campuses.value = res.data.data || []
    } catch (error) {
      console.error('캠퍼스 목록 조회 실패:', error)
    }
  }

  const getCampusParam = () =>
    showCampusFilter.value && selectedCampus.value
      ? `?campusId=${selectedCampus.value}`
      : ''

  return {
    showCampusFilter,
    currentUserCampusId,
    selectedCampus,
    campuses,
    fetchAdminInfo,
    fetchCampuses,
    getCampusParam,
  }
}
