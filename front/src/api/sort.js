import axios from 'axios'

export const getFirstCategories = () => axios.get('/api/subjects')

export const getSecondCategories = () => axios.get('/api/subtitles')
