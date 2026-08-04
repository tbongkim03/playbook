# Playbook 설치 마법사 (`installer/`)

캠퍼스 라운지 도서관리 시스템(Playbook)을 **캠퍼스 내부 PC 한 대에 통째로 설치**하는 Windows 마법사입니다.
Electron + Vue 3 로 만들었고, `electron-builder` 로 NSIS 단일 `.exe` 를 만듭니다.

담당자는 `.exe` 하나만 받아 실행하면 외부 API 키 설정부터 디스코드 봇 연결, 컨테이너 기동,
DB 마이그레이션까지 한 번에 끝냅니다.

---

## 1. 마법사가 하는 일 (10단계)

| # | 단계 | 내용 |
|---|------|------|
| 1 | 환경 점검 | Docker Desktop 설치·기동, Compose v2, WSL2, 포트 80/8080 충돌, 디스크 여유, 설치 경로 쓰기 권한 |
| 2 | 캠퍼스 · 시크릿 | 캠퍼스 지정. DB 비밀번호·`INTEGRATION_SECRET_KEY` 를 암호학적 난수로 자동 생성하고 백업 파일 저장 안내 |
| 3 | 외부 API 키 | 네이버 / 국립중앙도서관 / Work24 키 입력 + **실제 API 호출로 즉시 검증** |
| 4 | 디스코드 봇 | 포털 안내 → 토큰 검증 → 초대 URL 자동 생성 → 서버 선택 → 채널·역할 자동 생성 |
| 5 | 마스터 관리자 | `MASTER_ID/PW/NAME/DISCORD`. 비밀번호 강도 검사, 기본값(`admin/admin1234`) 차단 |
| 6 | 접속 허용 IP | 설치 PC IPv4·/24 대역 자동 감지 → `IP_ALLOWLIST_BOOTSTRAP` 생성 |
| 7 | 중앙 모니터링 | 엔드포인트·캠퍼스 라벨·부트스트랩 시크릿. **"사용 안 함" 으로 건너뛸 수 있음** |
| 8 | 설정 확인 · 배포 (1/2) | 요약 → 페이로드 전개 → `.env` 생성 → `pull` → **DB 컨테이너만 기동** |
| 9 | DB 마이그레이션 (2/2) | 미적용 SQL 탐지 → **테이블별 체크박스** 확인 → 적용 → **나머지 서비스 기동** |
| 10 | 완료 | 헬스체크, 접속 URL, 바탕화면 바로가기, 설정 백업 |

### ⚠ 8·9단계 순서가 뒤집혀 있는 이유

운영 백엔드는 `spring.jpa.hibernate.ddl-auto=validate` (application-prod.properties) 로 동작합니다.
마이그레이션이 적용되지 않은 스키마에서는 **백엔드가 기동조차 하지 못합니다.**
그래서 마법사는 반드시 다음 순서를 지킵니다.

```
DB 컨테이너만 기동  →  마이그레이션 적용  →  백엔드 · 프론트 · Redis (· Alloy) 기동
```

### ⚠ `scripts/migrate-db.sh` 는 호출하지 않습니다

저장소의 `scripts/migrate-db.sh` 는 이름과 달리 마이그레이션 실행기가 **아닙니다.**
Dev DB 를 `mysqldump` 해서 **Prod DB 를 통째로 덮어쓰는** 도구입니다(스크립트 내부에도
"Prod 데이터가 Dev 데이터로 덮어써집니다" 경고가 있습니다).
마법사가 이걸 부르면 캠퍼스 운영 DB 가 날아가므로, 대신 `db/migration/*.sql` 을
파일명 순번대로 `docker exec -i db mysql` 에 직접 먹입니다.

### ⚠ DROP / TRUNCATE / DELETE 는 실행하지 않습니다

사용자 최우선 제약입니다. 마이그레이션 SQL 을 문장 단위로 파싱해
`DROP` · `TRUNCATE` · `DELETE FROM` · `ALTER ... DROP ...` · `RENAME TABLE` 이 포함된 문장은
**실행 대상에서 완전히 제외**하고 화면에 경고로만 보여줍니다.
사용자가 체크해도 실행되지 않습니다 — UI 가 아니라 `src/main/services/migration.js` 에서 걸러집니다.

판정은 **주석을 걷어낸 실행문**에 대해서만 합니다. 마이그레이션 SQL 하단의 주석 처리된
롤백 DDL 때문에 멀쩡한 파일이 통째로 막히는 오탐은 발생하지 않습니다.

---

## 2. 빌드

### 사전 준비

- Node.js 20 이상 (개발은 24.x 에서 확인)
- **`.exe` 생성은 Windows 에서 하세요.** Linux/macOS 에서는 NSIS 코드서명 단계가 `wine` 을 요구합니다.

### 명령

```bash
cd installer
npm install

npm run check        # 자체 점검 (Electron 없이 Node 로 실행 — CI 용)
npm run build        # payload 준비 + 렌더러 번들
npm run build:win    # 위 + electron-builder --win --x64  → release/*.exe
```

산출물: `installer/release/Playbook-Installer-0.2.0-x64.exe`

### 그 외 명령

| 명령 | 설명 |
|------|------|
| `npm run dev` | vite dev 서버 + Electron 개발 실행 (핫리로드) |
| `npm run prepare:payload` | 저장소 루트 자산을 `installer/payload/` 로 복사만 |
| `npm run build:renderer` | 렌더러(Vue)만 번들 |
| `npm run pack:dir` | 설치 파일 없이 앱 디렉터리만 생성 (`release/win-unpacked`) |

### 동봉되는 배포 페이로드

`npm run prepare:payload` 가 저장소 루트에서 아래를 `installer/payload/` 로 복사하고,
`electron-builder` 가 이를 `resources/payload` 에 넣습니다. 마법사는 8단계에서 이걸
사용자가 고른 설치 경로로 풀어놓고 그 경로에서 `docker compose` 를 실행합니다.

```
docker-compose.prod.yml
db/Dockerfile, db/init, db/data, db/conf, db/migration
monitoring/            (있을 때만 — Alloy 사이드카 설정)
front/nginx-prod.conf  (참고용)
```

`.env.prod` 파일은 **복사 대상에서 제외**됩니다 (개발자 PC 의 시크릿이 배포물에 섞이는 사고 방지).
설치 경로에 이미 `.env.prod` 가 있으면 **덮어쓰지 않고 보존**합니다 (재설치 시 시크릿 유실 방지).

---

## 3. 배포 절차

1. **빌드** — Windows PC 에서 `npm run build:win`
2. **서명(선택)** — 코드서명 인증서가 있으면 `CSC_LINK` / `CSC_KEY_PASSWORD` 환경변수를 설정한 뒤 빌드하세요.
   서명하지 않으면 실행 시 SmartScreen "Windows의 PC 보호" 경고가 뜹니다 → [추가 정보] → [실행]
3. **전달** — `release/Playbook-Installer-*.exe` 를 캠퍼스 담당자에게 전달
4. **설치 PC 사전 준비** (담당자 안내 사항)
   - Docker Desktop 설치 + WSL2 백엔드 활성화 + 재부팅
   - 포트 80 / 8080 을 쓰는 다른 프로그램 종료
   - 디스크 여유 20GB 이상
   - 인터넷 연결 (이미지 다운로드 · 외부 API 검증 · 디스코드 API)
5. **실행** — `.exe` 실행 → 마법사 안내대로 10단계 진행
6. **설치 후 확인**
   - `http://localhost` 접속
   - 관리자 로그인 → **연동 탭에서 캠퍼스 역할 ID 등록** (아래 4.3 참고)
   - **접속 허용 IP 탭에서 규칙 확인**

---

## 4. 알아둬야 할 제약

### 4.1 마법사는 DB 에 직접 쓰지 않습니다

봇 토큰·API 키는 `.env` 에만 씁니다. 백엔드의 `IntegrationService` 가 기동 시
`@PostConstruct` 로 `.env` 값을 읽어 `tb_integration_config` 에 시드하고, 시크릿은
`IntegrationCrypto` (AES/GCM, `INTEGRATION_SECRET_KEY` 의 SHA-256 파생키) 로 암호화합니다.

**시드는 해당 키가 DB 에 없을 때만 일어납니다.** 즉 이미 한 번 기동한 시스템에 마법사로
새 값을 넣어도 `.env` 만 바뀌고 DB 값은 그대로입니다. 값 변경은 관리자 화면의 연동 탭에서 하세요.

### 4.2 `INTEGRATION_SECRET_KEY` 를 잃으면 복구할 수 없습니다

이 키로 DB 의 시크릿을 복호화합니다. 키가 바뀌면 저장된 봇 토큰·API 키를 전부 다시 입력해야 합니다.
10단계의 설정 백업 파일을 반드시 오프라인 보관하세요.

### 4.3 캠퍼스 역할 ID 는 수동 등록이 필요합니다

마법사는 디스코드 캠퍼스 역할을 **자동으로 만들지만**, 백엔드는 역할 ID 를 환경변수가 아니라
DB(`tb_campus_channel.discord_role_id`)에서만 읽습니다. `IntegrationService.seedCampusChannel()` 은
채널 ID 만 시드하고 역할 ID 는 `null` 로 둡니다.

→ 설치 후 **관리자 화면 > 연동 탭 > 캠퍼스 채널 매핑**에 마법사가 만든 역할 ID 를 넣어야
연동 시 캠퍼스 채널 해금이 동작합니다. 역할 ID 는 4단계 화면과 10단계 백업 파일에 표시됩니다.

### 4.4 기본 3개 캠퍼스 외에는 env 슬롯이 없습니다

`application.properties` 는 `DISCORD_CHANNEL_SEOCHO` / `_GVALLEY` / `_DONGJAK` 세 개만 읽습니다
(각각 `seq_campus` 1·2·3). 캠퍼스명을 직접 입력한 경우 채널 ID 도 연동 탭에서 수동 등록해야 합니다.

### 4.5 Actuator 헬스체크는 호스트에서 직접 조회되지 않습니다

`management.server.port=8081` 로 분리돼 있고 compose 가 호스트에 매핑하지 않습니다.
그래서 10단계 헬스체크는 ① 컨테이너 상태 ② 프론트 200 ③ `/api/*` 응답이 502/504 가 아님 으로 판정합니다.

### 4.6 접속 허용 IP 가 프론트까지 막습니다

`front/nginx-prod.conf` 가 정적 요청마다 `auth_request` 로 `/api/ip-gate` 판정을 받습니다.
설치 PC 가 허용 규칙에 없으면 **화면 자체가 403** 이 됩니다. 잠겼을 때 복구 방법:

```
설치경로\back\.env.prod 에서
  IP_ALLOWLIST_BYPASS=192.168.0.0/24      ← 내 대역 추가
  또는 IP_ALLOWLIST_ENABLED=false          ← 차단 일시 해제
후 docker compose -f docker-compose.prod.yml restart back
```

---

## 5. 보안 설계

| 항목 | 구현 |
|------|------|
| 렌더러 격리 | `nodeIntegration: false`, `contextIsolation: true`, `sandbox: true`, `webviewTag: false` |
| IPC 표면 | `contextBridge` 로 명시한 함수만 노출. 일반화된 `invoke(channel, …)` 없음 (`src/main/preload.js`) |
| 시크릿 위치 | **메인 프로세스에만** 존재. 렌더러에는 `{ __secret, set, masked }` 투영만 전달 (`src/main/state.js`) |
| 평문 노출 | 사용자가 명시적으로 누른 2곳만 — 2단계 [값 보기], 백업 파일 저장(확인 다이얼로그 포함) |
| 외부 호출 | API 키 검증·디스코드 REST 는 전부 메인에서 (`src/main/services/`) |
| 로그 마스킹 | docker/mysql 출력의 알려진 시크릿을 줄 단위로 치환 (`src/main/util/mask.js`) |
| 파일 권한 | 상태 파일·`.env.prod` 를 `0600` 으로 기록 (Windows 는 NTFS ACL 을 수동 확인 — 10단계 안내) |
| 명령 실행 | `spawn(shell: false)` — 셸 인젝션 표면 없음. DB 비밀번호는 `MYSQL_PWD` 로 전달 |
| CSP | `default-src 'self'` 강제, 원격 리소스·인라인 스크립트 차단 (`src/main/index.js`) |
| 외부 링크 | 도메인 화이트리스트를 통과한 http/https 만 기본 브라우저로 위임 |

### 진행 상태 저장 위치

```
%APPDATA%\playbook-installer\wizard-state.json
```

중단 후 다시 실행하면 이전 입력이 복구됩니다. **이 파일에는 시크릿이 평문으로 들어 있습니다.**
설치가 끝나면 삭제해도 됩니다.

---

## 6. 디렉터리 구조

```
installer/
├─ electron-builder.yml     NSIS 타깃·페이로드 동봉 설정
├─ vite.config.js           렌더러 번들 설정
├─ scripts/
│  ├─ prepare-payload.mjs   저장소 자산 → payload/ 복사
│  ├─ dev.mjs               개발 실행 (vite + electron)
│  └─ check.mjs             자체 점검 (npm run check)
├─ src/main/                ★ 메인 프로세스 — 시크릿·파일·docker·외부 API 전부 여기
│  ├─ index.js              앱 수명주기, BrowserWindow, CSP
│  ├─ preload.js            contextBridge IPC 표면
│  ├─ ipc.js                IPC 핸들러
│  ├─ state.js              상태 저장 + 시크릿 투영
│  ├─ util/                 secrets(난수) · mask(마스킹) · http
│  └─ services/
│     ├─ environment.js     1단계 환경 점검
│     ├─ apikeys.js         3단계 API 실검증
│     ├─ discord.js         4단계 디스코드 REST 자동화
│     ├─ master.js          5단계 계정 검증
│     ├─ network.js         6단계 IP 감지·검증
│     ├─ envfile.js         8단계 .env 생성 (변수명 정본 매핑)
│     ├─ compose.js         8·9단계 docker compose
│     ├─ migration.js       9단계 SQL 파서 + 파괴적 문장 제외
│     ├─ exec.js            명령 실행 공통 (스트리밍·마스킹)
│     └─ finish.js          10단계 헬스체크·바로가기·백업
└─ src/renderer/            Vue 3 화면 (시크릿 없음)
   ├─ App.vue, store.js, styles.css
   ├─ components/           StepNav · SecretField · LogView · StatusPill
   └─ steps/                Step1 ~ Step10
```

---

## 7. 자체 점검 (`npm run check`)

Electron 없이 Node 로 실행되는 검증입니다. CI 에 그대로 넣을 수 있습니다.

- 메인 프로세스 모듈 로드 / 문법 검사
- 실제 `db/migration/*.sql` 파싱 — 실행 대상에 파괴적 문장이 남지 않는지
- DROP/TRUNCATE/DELETE 판정, 주석 롤백 DROP 오탐 방지, 문자열 리터럴 내 세미콜론
- 생성되는 `.env` 가 `application*.properties` 가 참조하는 변수를 전부 덮는지 **(정본 대조)**
- 요약·compose `.env`·상태 투영에 시크릿 평문이 새지 않는지
- IP 검증/정규화, 시크릿 생성기 문자 집합, 마스킹, 디스코드 권한 비트, 마스터 계정 규칙
