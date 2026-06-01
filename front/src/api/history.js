import axios from 'axios'

export const getBooks = (campusId, startDate, endDate) => {
  const params = {}
  if (campusId) params.campusId = campusId
  if (startDate) params.startDate = startDate
  if (endDate) params.endDate = endDate
  return axios.get('/api/history/book', { params })
}

export const getMe = () => axios.get('/api/history/me')

export const borrow = (barcode) =>
  axios.post('/api/history/borrow', barcode, { headers: { 'Content-Type': 'text/plain' } })

export const returnBook = (barcode) =>
  axios.put('/api/history/return', barcode, { headers: { 'Content-Type': 'text/plain' } })

export const getPopularFirst = (courseId, campusId, startDate, endDate) => {
  const url = courseId ? `/api/history/popular/first/${courseId}` : '/api/history/popular/first'
  const params = {}
  if (campusId) params.campusId = campusId
  if (startDate) params.startDate = startDate
  if (endDate) params.endDate = endDate
  return axios.get(url, { params })
}

export const getPopularSecond = (courseId, campusId, startDate, endDate) => {
  const url = courseId ? `/api/history/popular/second/${courseId}` : '/api/history/popular/second'
  const params = {}
  if (campusId) params.campusId = campusId
  if (startDate) params.startDate = startDate
  if (endDate) params.endDate = endDate
  return axios.get(url, { params })
}

export const getUserRank = (courseId, campusId, startDate, endDate) => {
  const url = courseId ? `/api/history/rank/${courseId}` : '/api/history/rank'
  const params = {}
  if (campusId) params.campusId = campusId
  if (startDate) params.startDate = startDate
  if (endDate) params.endDate = endDate
  return axios.get(url, { params })
}

export const exportExcel = (campusId) =>
  axios.get('/api/history/export', { params: campusId ? { campusId } : {}, responseType: 'blob' })
