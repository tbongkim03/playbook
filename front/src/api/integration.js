import axios from 'axios'

// 연동 설정 (전체관리자 전용)
export const getConfigs = () => axios.get('/api/integration/configs')

export const updateConfig = (key, configValue) =>
  axios.put(`/api/integration/configs/${key}`, { configValue })

// 캠퍼스별 디스코드 채널/역할 매핑
export const getCampusChannels = () => axios.get('/api/integration/campus-channels')

export const updateCampusChannel = (campusId, data) =>
  axios.put(`/api/integration/campus-channels/${campusId}`, data)

// 연동 테스트
export const testDiscord = () => axios.post('/api/integration/test/discord')

export const reconnectDiscord = () => axios.post('/api/integration/discord/reconnect')

export const testWork24 = () => axios.post('/api/integration/test/work24')

export const syncWork24 = () => axios.post('/api/integration/work24/sync')

export const testNaver = () => axios.post('/api/integration/test/naver')

export const testNl = () => axios.post('/api/integration/test/nl')
