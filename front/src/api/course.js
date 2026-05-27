import axios from 'axios'

export const getAll = (campusId) =>
  axios.get('/api/courses', { params: campusId ? { campusId } : {} })

export const create = (data) => axios.post('/api/courses', data)

export const update = (id, data) => axios.put(`/api/courses/${id}`, data)

export const remove = (id) => axios.delete(`/api/courses/${id}`)

export const getWork24 = () => axios.get('/api/work24/course')
