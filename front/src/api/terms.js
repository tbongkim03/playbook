import axios from 'axios'

export const getByType = (type) => axios.get(`/api/terms/${type}`)

export const update = (type, content) => axios.put(`/api/terms/${type}`, { content })
