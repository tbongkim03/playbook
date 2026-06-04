import axios from 'axios'

export const getAll = () => axios.get('/api/campus')

export const getAllIncludeInactive = () => axios.get('/api/campus/all')

export const create = (data) => axios.post('/api/campus', data)

export const update = (id, data) => axios.put(`/api/campus/${id}`, data)

export const remove = (id) => axios.delete(`/api/campus/${id}`)
