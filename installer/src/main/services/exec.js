'use strict'

const { spawn } = require('node:child_process')
const { scrubLine } = require('../util/mask')

/**
 * 외부 명령 실행 공통 모듈 (docker / wsl / mysql 등).
 * - shell 을 쓰지 않는다 (인자 배열 그대로 전달 → 셸 인젝션 표면 제거)
 * - stdout/stderr 를 줄 단위로 스트리밍하며, 알려진 시크릿은 스크럽 후 내보낸다
 */

const isWin = process.platform === 'win32'

function runCapture(cmd, args, { cwd, env, timeout = 60000, input } = {}) {
  return new Promise((resolve) => {
    let child
    try {
      child = spawn(cmd, args, {
        cwd,
        env: { ...process.env, ...(env || {}) },
        windowsHide: true,
        shell: false
      })
    } catch (e) {
      resolve({ ok: false, code: -1, stdout: '', stderr: String(e.message || e), spawnError: true })
      return
    }

    let stdout = ''
    let stderr = ''
    let finished = false

    const timer = setTimeout(() => {
      if (!finished) {
        try {
          child.kill()
        } catch {
          /* noop */
        }
        finished = true
        resolve({ ok: false, code: -1, stdout, stderr: stderr + '\n[시간 초과]', timedOut: true })
      }
    }, timeout)

    child.stdout && child.stdout.on('data', (d) => (stdout += d.toString()))
    child.stderr && child.stderr.on('data', (d) => (stderr += d.toString()))

    child.on('error', (e) => {
      if (finished) return
      finished = true
      clearTimeout(timer)
      resolve({ ok: false, code: -1, stdout, stderr: String(e.message || e), spawnError: true })
    })

    child.on('close', (code) => {
      if (finished) return
      finished = true
      clearTimeout(timer)
      resolve({ ok: code === 0, code, stdout, stderr })
    })

    if (input !== undefined && child.stdin) {
      child.stdin.end(input)
    }
  })
}

/**
 * 스트리밍 실행. onLine(line, stream) 으로 한 줄씩 넘긴다.
 * secretValues 에 든 값은 출력 전에 마스킹된다.
 */
function runStream(cmd, args, { cwd, env, input, onLine, onLog, secretValues = [], timeout = 0 } = {}) {
  // 호출부(compose·migration)는 onLog 로 넘긴다. 예전엔 onLine 만 받아 docker 출력이
  // 통째로 버려졌고, pull 실패 시 원인 없이 안내 문구만 남았다.
  const sink = onLine || onLog
  return new Promise((resolve) => {
    const emit = (raw, stream) => {
      if (!sink) return
      for (const line of String(raw).split(/\r?\n/)) {
        if (line === '') continue
        sink(scrubLine(line, secretValues), stream)
      }
    }

    let child
    try {
      child = spawn(cmd, args, {
        cwd,
        env: { ...process.env, ...(env || {}) },
        windowsHide: true,
        shell: false
      })
    } catch (e) {
      emit(`실행 실패: ${e.message}`, 'stderr')
      resolve({ ok: false, code: -1, spawnError: true, error: String(e.message || e) })
      return
    }

    let finished = false
    let timer = null
    if (timeout > 0) {
      timer = setTimeout(() => {
        if (finished) return
        try {
          child.kill()
        } catch {
          /* noop */
        }
      }, timeout)
    }

    let tailErr = ''
    child.stdout && child.stdout.on('data', (d) => emit(d, 'stdout'))
    child.stderr &&
      child.stderr.on('data', (d) => {
        tailErr = (tailErr + d.toString()).slice(-4000)
        emit(d, 'stderr')
      })

    child.on('error', (e) => {
      if (finished) return
      finished = true
      timer && clearTimeout(timer)
      emit(`실행 실패: ${e.message}`, 'stderr')
      resolve({ ok: false, code: -1, spawnError: true, error: String(e.message || e) })
    })

    child.on('close', (code) => {
      if (finished) return
      finished = true
      timer && clearTimeout(timer)
      resolve({ ok: code === 0, code, stderr: scrubLine(tailErr, secretValues) })
    })

    if (input !== undefined && child.stdin) {
      child.stdin.end(input)
    }
  })
}

module.exports = { runCapture, runStream, isWin }
