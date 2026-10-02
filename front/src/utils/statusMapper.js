// 학생 계정 상태 (available / stop / overdue)
export const USER_STATUS_TEXT = {
  available: '정상',
  stop: '정지',
  overdue: '연체',
}
export const USER_STATUS_CLASS = {
  available: 'status-active',
  stop: 'status-stopped',
  overdue: 'status-overdue',
}
export const getUserStatusText = (status) => USER_STATUS_TEXT[status] || status
export const getUserStatusClass = (status) => USER_STATUS_CLASS[status] || 'status-default'

// 대출 이력 상태 (booked / returned / overdue)
export const RENTAL_STATUS_TEXT = {
  booked: '대여중',
  returned: '반납완료',
  overdue: '연체',
}
export const getRentalStatusText = (status) => RENTAL_STATUS_TEXT[status] || status

// 도서 상태 (printCheckBook + bookBorrowed 조합 → borrowed / available / unavailable)
export const getBookStatus = (book) => {
  if (!book.printCheckBook) return 'unavailable'
  if (book.bookBorrowed) return 'borrowed'
  return 'available'
}
export const BOOK_STATUS_TEXT = {
  borrowed: '대출 중',
  available: '대출 가능',
  unavailable: '대출 불가',
}
export const BOOK_STATUS_CLASS = {
  borrowed: 'status-borrowed',
  available: 'status-available',
  unavailable: 'status-unavailable',
}
export const getBookStatusText = (book) => BOOK_STATUS_TEXT[getBookStatus(book)] || '대출 불가'
export const getBookStatusClass = (book) => BOOK_STATUS_CLASS[getBookStatus(book)] || 'status-unavailable'
