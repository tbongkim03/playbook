# 개발 계획서

> 작성일: 2026-05-28  
> 브랜치: 0.2/refactor

---

## 현황 분석

| 기능 | 상태 | 메모 |
|------|------|------|
| 유저/어드민 개인정보 수정 | 부분 구현 | 어드민은 완전 구현, **학생 수정 API/UI 없음** |
| 아이디/비번 찾기 | 미구현 | 이메일 없음 → **디스코드 채널 기반**으로 설계 |
| CI/CD 자동화 | 부분 구현 | Docker Compose 완성, **GitHub Actions + GHCR 없음** |
| 모바일 디자인 최적화 | 구현됨 | 24개+ 컴포넌트 media query 적용, 세부 점검 필요 |

---

## 진행 순서

```
P1(개인정보 수정) → P2(아이디/비번 찾기) → P3(모바일 최적화) → P4(CI/CD)
```

1. **P1 먼저**: 기존 어드민 구현 참고해 빠르게 완성. 학생 UX 즉시 개선
2. **P2 다음**: 디스코드 이미 연동됨 — 채널/봇 기반이라 이메일 서버 불필요
3. **P3 세 번째**: 기반 있음. P1/P2 완료 후 전체 UI 한 번에 점검
4. **P4 마지막**: 인프라 작업, 서버 환경 준비 후 진행

---

## P1 — 유저/어드민 개인정보 수정 `[✓ 완료]`

> 커밋: `8d78914` — 2026-05-28

**작업 완료**:
- [x] 백엔드: `PUT /users/update` — 학생 이름·디스코드 ID 자기 수정
- [x] 백엔드: `PUT /users/admin/reset-password` — 어드민의 학생 비밀번호 초기화
- [x] `UpdateUserRequestDto`, `ResetUserPasswordRequestDto` 생성
- [x] `MyPage.vue` — 이름·디스코드 ID 변경 모달 추가, 디스코드 항목 표시
- [x] `UserAccountManagement.vue` — 학생 비밀번호 초기화 버튼·모달 추가

---

## P2 — 아이디/비번 찾기 (디스코드 기반) `[✓ 완료]`

> 커밋: (아래 참조) — 2026-05-28

**완료 내용** (옵션 A — Discord Bot 자동화):
- [x] `PlaybookListener.java` — `/findid`, `/resetpw` 슬래시 커맨드 추가
- [x] `onReady` 에서 커맨드 자동 등록
- [x] `/findid` — Discord ID(snowflake 또는 username) 매칭 → ephemeral 응답으로 아이디 안내
- [x] `/resetpw id:[아이디]` — 본인 확인 후 임시 비밀번호 생성·저장 → ephemeral 응답
- [x] `DiscordBotInitializer` — `GUILD_MESSAGES` intent 추가
- [x] `PageLogin.vue` — "아이디·비밀번호 찾기" 링크 + 명령어 안내 힌트 추가
- [x] `front/.env` — `VITE_DISCORD_SERVER_URL` 환경변수 추가

**⚠️ 운영 시 설정 필요**:
- `front/.env`의 `VITE_DISCORD_SERVER_URL`에 실제 디스코드 서버 초대 링크 입력
- 디스코드 봇이 서버에서 슬래시 커맨드 권한 보유 확인

**방식**: 이미 디스코드 계정 연동이 구현됨 → Discord ID를 신원 확인 수단으로 활용

### 흐름 설계

```
[아이디 찾기]
  사용자 → 플북 디스코드 서버 #계정-찾기 채널에서 봇 명령 입력
  → 봇이 Discord ID로 DB 조회 → 계정 아이디를 DM 발송

[비번 찾기]
  사용자 → #계정-찾기 채널에서 봇 명령 입력 (아이디 입력)
  → 봇이 Discord ID 매칭 확인 → 임시 비밀번호 생성 → DM 발송
  → 사용자가 로그인 후 마이페이지에서 비밀번호 변경
```

### 옵션 A — Discord Bot 자동화 (권장)

**장점**: 24/7 자동화, 어드민 개입 불필요  
**단점**: JDA 또는 Discord4J 라이브러리 추가, 봇 토큰 관리 필요

**작업 목록**:
- [ ] Spring Boot에 Discord4J 또는 JDA 의존성 추가
- [ ] `DiscordBotService` 생성 — 봇 명령 리스닝
- [ ] 백엔드: `POST /account/find-id` — discordId로 userId 조회 (봇 내부 호출용)
- [ ] 백엔드: `POST /account/reset-password` — discordId 검증 후 임시 비밀번호 발급
- [ ] 봇 명령어 정의: `!아이디찾기`, `!비번찾기 [아이디]`
- [ ] Discord 서버 #계정-찾기 채널 설정 (봇 전용 채널)
- [ ] 프론트: 로그인 페이지에 "아이디/비번 찾기" 안내 링크 추가 (Discord 채널 링크)

### 옵션 B — 어드민 중재 방식 (간단)

**장점**: 추가 개발 없음, 즉시 운영 가능  
**단점**: 어드민이 직접 처리해야 함

**작업**:
- [ ] 프론트: 로그인 페이지에 "계정 찾기 → 디스코드 채널로 문의" 안내 추가
- [ ] 어드민 페이지: 어드민이 학생 비밀번호를 직접 초기화할 수 있는 버튼 추가
- [ ] Discord 서버 #계정-찾기 채널 생성 및 안내

> **추천**: 먼저 옵션 B로 빠르게 운영 가능하게 하고, 이후 옵션 A로 자동화

**예상 소요**: 옵션 B — 반나절 / 옵션 A — 1~2일

---

## P3 — 모바일 디자인 최적화 `[ ]`

**목표**: 실제 모바일 기기 기준 UX 점검 및 개선

**현황**: media query는 모든 컴포넌트에 적용됨, 세부 UX 이슈 점검 필요

**작업 목록**:
- [ ] Chrome DevTools 모바일 에뮬레이션으로 주요 페이지 점검
  - `HomePage.vue` — 도서 목록 그리드
  - `MyPage.vue` — 마이페이지 레이아웃
  - `PageLogin.vue` — 로그인 폼
  - `AdminPage.vue` — 어드민 패널 (모바일 최소화 고려)
- [ ] 터치 타겟 크기 점검 (버튼 최소 44px)
- [ ] 모바일 네비게이션 (Header) UX 점검
- [ ] 폰트 사이즈, 간격 세부 조정

**예상 소요**: 반나절 ~ 1일

---

## P4 — CI/CD 자동화 `[ ]`

**목표**: GitHub Actions + GHCR 기반 자동 빌드·배포 파이프라인 구축

**현황 분석**:
- Docker Compose 파일: `docker-compose.dev.yml`, `docker-compose.prod.yml` 분리됨
- Dockerfile: back (멀티스테이지, JDK), front (멀티스테이지, Nginx)
- prod compose: `build:` 방식 → **`image:` 방식(GHCR)으로 전환 필요**
- CI/CD 파이프라인: 없음

### 최종 구조 목표

```
GitHub Push/PR
    ↓
GitHub Actions CI (테스트·빌드)
    ↓
Docker Image Build → GHCR Push
    ↓
서버 SSH → docker compose pull → docker compose up -d
```

```
서버 Docker Compose (prod)
  ├── frontend   (ghcr.io/.../playbook-front:latest)
  ├── backend    (ghcr.io/.../playbook-back:latest)
  ├── mysql      (custom DB Dockerfile)
  ├── redis      (redis:7-alpine)
  └── nginx      (향후: reverse proxy — 현재는 front Nginx가 담당)
  ─── prometheus / grafana (향후 예정, 슬롯만 확보)
```

---

### 단계별 작업 계획

#### 단계 1 — Dockerfile 개선 `[ ]`

**back/Dockerfile 개선사항**:
- JDK → JRE로 런타임 이미지 교체 (이미지 사이즈 약 30% 절감)
- `.jar` 레이어 분리로 캐시 최적화 (의존성 레이어 별도 분리)

```dockerfile
# 변경 전
FROM eclipse-temurin:17-jdk
# 변경 후
FROM eclipse-temurin:17-jre-alpine
```

- [ ] `back/Dockerfile` 런타임 이미지 `17-jre-alpine`으로 변경
- [ ] 레이어 캐시 최적화 (COPY *.jar → COPY --from=build)
- [ ] `front/ProdDockerfile` 현재 OK (node:18 → nginx:alpine 멀티스테이지 유지)
- [ ] `front/DevDockerfile` 확인 및 필요시 개선

#### 단계 2 — docker-compose.prod.yml 전환 `[ ]`

현재 `build:` 방식 → GHCR 이미지 pull 방식으로 전환

```yaml
# 변경 전
back:
  build:
    context: ./back
    dockerfile: Dockerfile

# 변경 후
back:
  image: ghcr.io/OWNER/playbook-back:latest
```

- [ ] `docker-compose.prod.yml` back/front를 `image:` 방식으로 변경
- [ ] `docker-compose.dev.yml` 는 `build:` 방식 유지 (로컬 개발용)
- [ ] Nginx reverse proxy 서비스 추가 슬롯 확보 (주석 처리로 준비)
- [ ] Prometheus / Grafana 서비스 슬롯 주석으로 준비

#### 단계 3 — GitHub Actions CI 워크플로우 `[ ]`

파일: `.github/workflows/ci.yml`

**트리거**: PR 생성 / push (main 제외)

```yaml
jobs:
  backend-ci:
    - JDK 17 설정
    - Gradle 빌드 + 테스트 (./gradlew build)
    - 빌드 결과 아티팩트 저장

  frontend-ci:
    - Node 18 설정
    - npm install + npm run build
    - 빌드 결과 확인
```

- [ ] `.github/workflows/ci.yml` 작성

#### 단계 4 — GitHub Actions CD 워크플로우 `[ ]`

파일: `.github/workflows/cd.yml`

**트리거**: main 브랜치 push (또는 수동 `workflow_dispatch`)

```yaml
jobs:
  build-and-push:
    - GHCR 로그인 (GITHUB_TOKEN)
    - Docker Buildx 설정 (멀티플랫폼 빌드)
    - back 이미지 빌드 → ghcr.io/OWNER/playbook-back:{sha}, :latest
    - front 이미지 빌드 → ghcr.io/OWNER/playbook-front:{sha}, :latest
    - GHCR push

  deploy:
    needs: build-and-push
    - SSH로 서버 접속
    - docker compose pull
    - docker compose up -d --no-build
    - Discord Webhook으로 배포 결과 알림
```

- [ ] `.github/workflows/cd.yml` 작성
- [ ] Discord Webhook 배포 알림 추가 (기존 디스코드 연동 활용)

#### 단계 5 — GHCR 설정 `[ ]`

**이미지 태깅 전략**:

| 태그 | 설명 | 용도 |
|------|------|------|
| `latest` | main 최신 | 자동 배포 |
| `sha-{7자리}` | 커밋 해시 | 롤백용 |
| `v1.2.3` | Semantic version | 릴리즈 |

- [ ] GHCR 패키지 공개/비공개 설정 결정 (`ghcr.io/tbongkim03/playbook-back`)
- [ ] 이미지 보존 정책 설정 (최근 10개 유지)

#### 단계 6 — Secret 및 환경변수 관리 `[ ]`

**GitHub Secrets 등록 목록**:

```
SSH_PRIVATE_KEY     — 서버 SSH 개인키
SERVER_HOST         — 배포 서버 IP/도메인
SERVER_USER         — SSH 접속 사용자명
SERVER_DEPLOY_PATH  — 서버 내 프로젝트 경로 (예: /home/user/playbook)
DISCORD_WEBHOOK_URL — 배포 알림 Webhook
```

> `GITHUB_TOKEN`은 GHCR 접근에 자동 제공됨 (별도 설정 불필요)

- [ ] GitHub 저장소 Settings → Secrets 등록
- [ ] `.env.prod` 파일 서버에서만 관리 (Git 제외, `.gitignore` 확인)
- [ ] 환경변수 목록을 `docs/SECRETS.md`에 문서화 (값 제외, 키만)

#### 단계 7 — 배포 스크립트 `[ ]`

**배포 방식 비교**:

| 방식 | 장점 | 단점 | 추천 |
|------|------|------|------|
| 수동 deploy script | 제어 가능, 안전, 롤백 명확 | 수동 트리거 필요 | ✅ 현재 단계 |
| Watchtower | 이미지 변경 자동 감지·재시작 | 예고 없는 재시작 위험, 디버깅 어려움 | 추후 검토 |

**추천**: `workflow_dispatch` 수동 트리거 + SSH deploy script 방식 유지  
서비스 안정화 후 Watchtower 도입 검토

파일: `scripts/deploy.sh`
```bash
#!/bin/bash
# GHCR에서 최신 이미지 pull 후 재시작
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d --no-build
docker image prune -f
```

파일: `scripts/rollback.sh`
```bash
#!/bin/bash
# 특정 태그로 롤백
TAG=${1:-"sha-이전커밋해시"}
IMAGE_BACK="ghcr.io/OWNER/playbook-back:$TAG"
IMAGE_FRONT="ghcr.io/OWNER/playbook-front:$TAG"
docker compose -f docker-compose.prod.yml \
  up -d --no-build \
  -e BACK_IMAGE=$IMAGE_BACK \
  -e FRONT_IMAGE=$IMAGE_FRONT
```

- [ ] `scripts/deploy.sh` 작성
- [ ] `scripts/rollback.sh` 작성

#### 단계 8 — Nginx Reverse Proxy 고려 `[ ]`

**현재**: front 컨테이너의 Nginx가 `/api` → back:8080 프록시 담당  
**향후**: 도메인 + HTTPS 적용 시 서버 레벨 Nginx 추가 필요

```
인터넷 → 서버 Nginx(443) → front:80 / back:8080
                         ↑ Let's Encrypt SSL
```

- [ ] HTTPS 필요 시 `nginx/nginx.conf` 추가 및 compose에 서비스 추가
- [ ] 현재는 front Nginx 유지 (포트 80 직접 노출)

#### 단계 9 — Release/Version 관리 `[ ]`

**태깅 전략**:

```bash
# 릴리즈 시
git tag v1.0.0
git push origin v1.0.0
# → GitHub Actions가 v1.0.0 태그 이미지도 빌드하여 GHCR push
```

GitHub Actions release 트리거:
```yaml
on:
  push:
    tags:
      - 'v*'
```

- [ ] `cd.yml`에 태그 push 시 버전 태그 이미지 빌드 추가
- [ ] GitHub Releases 페이지 활용 (자동 changelog)

#### 단계 10 — 운영 문서 `[ ]`

`docs/DEPLOY.md` (또는 README 섹션):
- 초기 서버 설정 방법
- 배포 순서 (GitHub Actions 트리거 → 서버 확인)
- 롤백 방법
- 환경변수 목록 (값 제외)
- 이미지 태그 조회 방법

- [ ] `docs/DEPLOY.md` 작성

---

### 적용 순서 요약

```
1. Dockerfile 개선 (back JRE, 레이어 최적화)
2. docker-compose.prod.yml image: 방식 전환
3. .github/workflows/ci.yml 작성 및 테스트
4. GHCR 이미지 첫 수동 빌드·push 확인
5. .github/workflows/cd.yml 작성
6. GitHub Secrets 등록
7. scripts/deploy.sh, rollback.sh 작성
8. 서버에서 docker-compose.prod.yml 동작 확인
9. docs/DEPLOY.md 작성
10. (선택) Nginx HTTPS, Watchtower, Prometheus/Grafana
```

**예상 소요**: 2~3일

---

## 전체 완료 체크리스트

- [x] P1: 유저/어드민 개인정보 수정 (완료 `8d78914`)
- [x] P2: 아이디/비번 찾기 (디스코드 봇 슬래시 커맨드)
- [ ] P3: 모바일 디자인 최적화
- [ ] P4: CI/CD 자동화
  - [ ] Dockerfile 개선
  - [ ] docker-compose.prod.yml 전환
  - [ ] CI 워크플로우
  - [ ] CD 워크플로우
  - [ ] GHCR 설정
  - [ ] Secret 관리
  - [ ] deploy/rollback 스크립트
  - [ ] Nginx 검토
  - [ ] Release/Version 관리
  - [ ] 운영 문서
