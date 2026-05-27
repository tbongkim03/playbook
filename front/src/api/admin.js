import axios from 'axios'

export const login = (data) => axios.post('/api/admin/login', data)

export const getMe = () => axios.get('/api/admin/me')

export const checkMe = () => axios.get('/api/admin/me', { validateStatus: () => true })

export const getList = (campusId) =>
  axios.get('/api/admin/list', { params: campusId ? { campusId } : {} })

export const validateId = (id) =>
  axios.get('/api/admin/register/validate', { params: { id } })

export const validatePassword = (idAdmin, password) =>
  axios.post(`/api/admin/validate?id=${idAdmin}`, { password })

export const register = (data) => axios.post('/api/admin/register', data)

export const update = (data) => axios.put('/api/admin/update', data)

export const remove = (idAdmin) =>
  axios.delete('/api/admin', { data: { idAdmin } })

export const logout = () => axios.post('/api/admin/logout')

export const exportExcel = (campusId) =>
  axios.get('/api/admin/export', { params: campusId ? { campusId } : {}, responseType: 'blob' })
