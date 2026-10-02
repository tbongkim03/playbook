import { swAlert } from '@/utils/sweetAlert.js'

export async function handleApiError(error, defaultMessage = '오류가 발생했습니다.') {
  const status = error.response?.status
  const serverMsg = error.response?.data?.msg

  if (status === 401) {
    await swAlert('로그인이 필요합니다.', 'info')
  } else if (status === 403) {
    await swAlert('접근 권한이 없습니다.', 'warning')
  } else if (status === 400) {
    await swAlert(serverMsg || '입력 정보를 확인해주세요.', 'warning')
  } else {
    await swAlert(serverMsg || defaultMessage, 'error')
  }
}
