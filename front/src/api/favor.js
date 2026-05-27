import axios from 'axios'

export const getAll = () => axios.get('/api/favor')

export const add = (seqBook) =>
  axios.post('/api/favor', seqBook, { headers: { 'Content-Type': 'application/json' } })

export const remove = (seqBook) =>
  axios.delete('/api/favor', { headers: { 'Content-Type': 'application/json' }, data: seqBook })
