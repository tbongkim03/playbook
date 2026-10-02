import Swal from 'sweetalert2'

const titleMap = {
  success: '성공',
  error: '오류',
  warning: '경고',
  info: '안내',
}

/**
 * 결과 알림창 (확인 버튼만)
 * @param {string} message
 * @param {'success'|'error'|'warning'|'info'} type
 * @param {{ title?: string }} options
 */
export async function swAlert(message, type = 'info', options = {}) {
  const { title = titleMap[type] ?? '알림' } = options
  await Swal.fire({
    title,
    text: message,
    icon: type,
    confirmButtonText: '확인',
    confirmButtonColor: type === 'error' ? '#d33' : type === 'success' ? '#3085d6' : '#6c757d',
    allowOutsideClick: false,
  })
}

/**
 * 선택 확인창 (확인 + 취소)
 * @param {string} title
 * @param {string} message
 * @param {{ showCancelButton?: boolean, isDangerous?: boolean, cancelable?: boolean, icon?: string }} options
 * @returns {Promise<boolean>}
 */
export async function swConfirm(title, message = '', options = {}) {
  const {
    showCancelButton = true,
    isDangerous = false,
    cancelable = true,
    icon = 'question',
  } = options

  const result = await Swal.fire({
    title,
    text: message,
    icon,
    showCancelButton,
    confirmButtonText: '확인',
    cancelButtonText: '취소',
    confirmButtonColor: isDangerous ? '#d33' : '#3085d6',
    cancelButtonColor: '#6c757d',
    allowOutsideClick: cancelable,
    allowEscapeKey: cancelable,
  })

  return result.isConfirmed
}

/**
 * 자동으로 사라지는 토스트 알림
 * @param {string} message
 * @param {'success'|'error'|'warning'|'info'} type
 * @param {{ duration?: number, position?: string }} options
 */
export function swToast(message, type = 'success', options = {}) {
  const { duration = 3000, position = 'top-end' } = options
  Swal.fire({
    toast: true,
    position,
    icon: type,
    title: message,
    showConfirmButton: false,
    timer: duration,
    timerProgressBar: true,
  })
}
