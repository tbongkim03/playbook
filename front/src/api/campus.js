import axios from 'axios'

export const getAll = () => axios.get('/api/campus')

export const getAllIncludeInactive = () => axios.get('/api/campus/all')
