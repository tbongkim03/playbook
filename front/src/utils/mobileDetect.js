const MOBILE_PATTERN = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i

export function isMobile() {
  return MOBILE_PATTERN.test(navigator.userAgent)
}
