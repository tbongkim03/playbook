import axios from 'axios'

export const searchByIsbn = (isbn) => axios.post('/api/national-library/isbn', isbn)

export const searchKakao = (data) => axios.post('/api/kakao/book-search', data)
