import axios from 'axios'

export const login = (data) => axios.post('/api/users/login', data)

export const getMe = () => axios.get('/api/users/me')

export const getList = (campusId) =>
  axios.get('/api/users/list', { params: campusId ? { campusId } : {} })

export const register = (data) => axios.post('/api/users/register', data)

export const validatePassword = (password) =>
  axios.post('/api/users/validate', { password })

export const updatePassword = (newPassword) =>
  axios.put('/api/users/password', { newPassword })

export const removeByAdmin = (idUser) =>
  axios.delete('/api/users', { data: { idUser } })

export const deleteAccount = () => axios.delete('/api/users')

export const checkMe = () => axios.get('/api/users/me', { validateStatus: () => true })

export const logout = () => axios.post('/api/users/logout')

export const validateId = (id) =>
  axios.get('/api/users/register/validate', { params: { id } })
