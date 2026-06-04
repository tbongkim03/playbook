import axios from 'axios'

export const getFirstCategories = () => axios.get('/api/subjects')
export const createFirstCategory = (data) => axios.post('/api/subjects', data)
export const updateFirstCategory = (id, data) => axios.put(`/api/subjects/${id}`, data)
export const deleteFirstCategory = (id) => axios.delete(`/api/subjects/${id}`)

export const getSecondCategories = () => axios.get('/api/subtitles')
export const createSecondCategory = (data) => axios.post('/api/subtitles', data)
export const updateSecondCategory = (id, data) => axios.put(`/api/subtitles/${id}`, data)
export const deleteSecondCategory = (id) => axios.delete(`/api/subtitles/${id}`)
