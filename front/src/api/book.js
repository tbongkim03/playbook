import axios from 'axios'

export const getAll = (params) => axios.get('/api/books', { params })

export const getAllForAdmin = () => axios.get('/api/books/all')

export const getByCategory = (id, params) =>
  axios.get('/api/books/sortFirst', { params: { id, ...params } })

export const getBySortSecond = (id, params) =>
  axios.get('/api/books/sortSecond', { params: { id, ...params } })

export const search = (params) => axios.get('/api/books/search', { params })

export const getById = (bookId) => axios.get(`/api/books/${bookId}`)

export const create = (data) => axios.post('/api/books', data)

export const remove = (seqBook) => axios.delete(`/api/books/${seqBook}`)

export const getRelated = (params) => axios.get('/api/books/related', { params })

export const exportExcel = (campusId) =>
  axios.get('/api/books/export', { params: campusId ? { campusId } : {}, responseType: 'blob' })
