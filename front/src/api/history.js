import axios from 'axios'

export const getBooks = (campusId) =>
  axios.get('/api/history/book', { params: campusId ? { campusId } : {} })

export const getMe = () => axios.get('/api/history/me')

export const borrow = (barcode) =>
  axios.post('/api/history/borrow', barcode, { headers: { 'Content-Type': 'text/plain' } })

export const returnBook = (barcode) =>
  axios.put('/api/history/return', barcode, { headers: { 'Content-Type': 'text/plain' } })

export const getPopularFirst = (courseId, campusId) => {
  const url = courseId ? `/api/history/popular/first/${courseId}` : '/api/history/popular/first'
  return axios.get(url, { params: campusId ? { campusId } : {} })
}

export const getPopularSecond = (courseId, campusId) => {
  const url = courseId ? `/api/history/popular/second/${courseId}` : '/api/history/popular/second'
  return axios.get(url, { params: campusId ? { campusId } : {} })
}

export const getUserRank = (courseId, campusId) => {
  const url = courseId ? `/api/history/rank/${courseId}` : '/api/history/rank'
  return axios.get(url, { params: campusId ? { campusId } : {} })
}

export const exportExcel = (campusId) =>
  axios.get('/api/history/export', { params: campusId ? { campusId } : {}, responseType: 'blob' })
