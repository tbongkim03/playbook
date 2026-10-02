import axios from 'axios'

// 허용 IP 관리 (관리자)
// 전체관리자: 전 캠퍼스 + 전역 규칙 / 캠퍼스 관리자: 자기 캠퍼스 규칙

// 목록 조회 — params: { seqCampus?: number, includeInactive?: boolean }
export const getList = (params = {}) => axios.get('/api/allowed-ips', { params })

// 등록 — data: { ipValue, seqCampus?, description?, isActive? }
export const create = (data) => axios.post('/api/allowed-ips', data)

// 수정 — data: { ipValue, seqCampus?, description?, isActive? }
export const update = (seqAllowedIp, data) => axios.put(`/api/allowed-ips/${seqAllowedIp}`, data)

// 삭제 (Soft Delete)
export const remove = (seqAllowedIp) => axios.delete(`/api/allowed-ips/${seqAllowedIp}`)

// 활성/비활성 토글
export const setActive = (seqAllowedIp, isActive) =>
  axios.patch(`/api/allowed-ips/${seqAllowedIp}/active`, { isActive })

// 요청자 IP + 현재 허용 여부
export const getMyIp = () => axios.get('/api/allowed-ips/my-ip')
