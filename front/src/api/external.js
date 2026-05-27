import axios from 'axios'

export const searchByIsbn = (isbn) => axios.post('/api/national-library/isbn', isbn)

export const searchNaver = (data) => axios.post('/api/naver/book-search', data)
