#!/usr/bin/env node
/**
 * 개발 실행 — vite dev 서버를 띄우고 electron 을 붙인다.
 * VITE_DEV_SERVER_URL 이 있으면 메인 프로세스가 파일 대신 dev 서버를 로드한다.
 */
import { spawn } from 'node:child_process'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createServer } from 'vite'

const here = path.dirname(fileURLToPath(import.meta.url))
const root = path.resolve(here, '..')

const server = await createServer({ configFile: path.join(root, 'vite.config.js') })
await server.listen()
const url = server.resolvedUrls?.local?.[0]
if (!url) {
  console.error('[dev] vite dev 서버 주소를 확인하지 못했습니다.')
  process.exit(1)
}
console.log(`[dev] vite dev server: ${url}`)

const electronBin = (await import('electron')).default
const child = spawn(electronBin, [root], {
  stdio: 'inherit',
  env: { ...process.env, VITE_DEV_SERVER_URL: url, NODE_ENV: 'development' }
})

child.on('close', async (code) => {
  await server.close()
  process.exit(code ?? 0)
})
