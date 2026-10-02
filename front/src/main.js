import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'

import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap'

import router from '@/router'
import axios from 'axios'

// 세션 쿠키를 모든 요청에 자동 포함
axios.defaults.withCredentials = true

// Axios Response Interceptor - 세션 만료 처리
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      sessionStorage.removeItem('userType')
      sessionStorage.removeItem('campusId')

      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

createApp(App).use(router).mount('#app')
