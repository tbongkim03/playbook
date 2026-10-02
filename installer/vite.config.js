import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// 렌더러(Vue 3)만 vite 로 번들한다.
// 메인 프로세스(src/main/**)는 Node 내장 모듈 + electron 만 쓰므로 번들하지 않고
// electron-builder 가 그대로 패키징한다 (번들러 설정 표면을 줄이기 위한 의도적 선택).
export default defineConfig({
  root: fileURLToPath(new URL('./src/renderer', import.meta.url)),
  base: './',
  plugins: [vue()],
  build: {
    outDir: fileURLToPath(new URL('./dist/renderer', import.meta.url)),
    emptyOutDir: true,
    target: 'chrome128',
    // 시크릿이 소스맵을 통해 흘러나갈 여지를 없앤다
    sourcemap: false
  },
  server: {
    port: 5180,
    strictPort: true
  }
})
