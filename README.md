# 📚플레이북
## 캠퍼스 라운지 도서 관리 시스템

### 🚀 프로젝트 한 줄 소개 (Problem → Solution → Impact)
- **문제**: 라운지 도서가 수기 관리로 인해 분실·연체·현황 파악의 비효율 발생
- **해결**: 바코드 스캔 기반 대출/반납, 대출 이력/통계 대시보드, 반납 알림 자동화
- **임팩트**: 대출/반납 처리 시간을 단축하고, 연체 사전 안내로 분실 감소에 기여

### 🧭 목차
- [프로젝트 한눈에 보기](#-프로젝트-한눈에-보기)
- [아키텍처](#-아키텍처)
- [핵심 설계 포인트](#-핵심-설계-포인트)
- [기술 스택](#%EF%B8%8F-기술-스택)
- [주요 기능](#-주요-기능)
- [기능 체크리스트](#기능-체크리스트)
- [폴더 구조](#%EF%B8%8F-폴더-구조)
- [빠른 시작](#-빠른-시작)
- [환경 변수](#-환경-변수)
- [바코드 라벨링](#%EF%B8%8F-바코드-라벨링)
- [개발 가이드](#%E2%80%8D-개발-가이드)
- [테스트와 품질](#-테스트와-품질)
- [DB 스키마 개요](#%EF%B8%8F-db-스키마-개요)
- [보안과 권한](#-보안과-권한)
- [향후 개선 계획](#%EF%B8%8F-향후-개선-계획)
- [프로젝트 문서](#-프로젝트-문서)
- [라이선스](#-라이선스)

<br/>

---

## ✨ 프로젝트 한눈에 보기
- **목표**: 라운지 도서의 등록/분류/대출/반납과 연체 알림을 간편하게 운영
- **역할 분리**: 운영자/일반 사용자 권한 구분, 관리자 대시보드 제공
- **기간**: 2025-04-01 ~ 진행 중
- **핵심 포인트**: 백엔드 아키텍처, Docker 기반 실행, 바코드/Discord 연동

<br/>

## 🧱 아키텍처
```
Docker Compose
 ├─ front  (Vue.js, Vite, Chart.js)
 ├─ back   (Spring Boot, JPA, Spring Session + Redis)
 ├─ db     (MariaDB)
 └─ redis  (Redis)

외부 연계: 국립중앙도서관 ISBN API, 네이버 도서 검색 API, 고용노동부 고용24 API, Discord Bot 알림
```

<br/>

## 🧩 핵심 설계 포인트
- **계층형 아키텍처**: `Controller → Service → Repository/DAO`로 관심사 분리 및 테스트 용이성 확보
- **DTO/Entity 분리**: API 스펙과 영속 모델의 결합 최소화, `@Valid` DTO 검증 + GlobalExceptionHandler
- **세션 기반 인증**: Spring Session + Redis, 중복 로그인 방지, 인터셉터로 공통 인증 체크
- **공통 응답 형식**: `ResponseHandler`로 전 API 응답 통일 (코드/메시지/데이터)
- **공통 감사 컬럼**: `BaseAuditEntity` — 생성자/수정자/일시 자동 기록, Soft Delete(`use_yn`)
- **접속이력·감사로그**: Spring Event + AOP(@AuditAction)로 로그인/관리자 작업 이력 자동 기록, 1년 파기 스케줄러
- **스케줄러 기반 운영 자동화**: 반납 기한/과정 종료에 맞춰 Discord 알림 발송
- **도메인 중심 설계**: 대분류/중분류, 과정, 대출 이력 등 핵심 개념을 엔터티로 모델링

<br/>

## 🛠️ 기술 스택
  <div align="left">
    <img alt="Java" src="https://img.shields.io/badge/Java-17%2B-ED8B00?logo=openjdk&logoColor=white&style=flat-square" />
    <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot&logoColor=white&style=flat-square" />
    <img alt="Vue.js" src="https://img.shields.io/badge/Vue.js-3-42B883?logo=vuedotjs&logoColor=white&style=flat-square" />
    <img alt="MariaDB" src="https://img.shields.io/badge/MariaDB-003545?logo=mariadb&logoColor=white&style=flat-square" />
    <img alt="Redis" src="https://img.shields.io/badge/Redis-DC382D?logo=redis&logoColor=white&style=flat-square" />
    <img alt="Docker Compose" src="https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white&style=flat-square" />
    <img alt="Gradle" src="https://img.shields.io/badge/Build-Gradle-02303A?logo=gradle&logoColor=white&style=flat-square" />
    <img alt="Node.js" src="https://img.shields.io/badge/Node.js-18%2B-339933?logo=node.js&logoColor=white&style=flat-square" />
  </div>

- **Backend**: Java, Spring Boot 3.1.12
- **Frontend**: Vue 3, Chart.js
- **Database**: MariaDB
- **Cache/Session**: Redis (Spring Session)
- **Infra/Dev**: Docker, Docker Compose, Gradle, Node.js

<br/>

## 📌 주요 기능
- **인증/인가**: Spring Session + Redis 기반 세션 관리, 운영자/일반 사용자 권한 구분, 로그인 체크 인터셉터, 동일 사용자 중복 로그인 방지
- **도서 관리**: 등록/수정/삭제, 바코드/ISBN 조회, 분류(대분류/중분류), Soft Delete
- **대출/반납**: 바코드 스캔 기반 처리 (PC 전용), 대출 현황/연체 관리, 모바일 접근 제한
- **대시보드/통계**: 대출 비율, 인기 도서, 기간/분야별 통계(Chart.js)
- **알림**: 반납 기한 전 Discord 멘션 알림, 과정 종료 시 반납 리마인더
- **접속이력·감사로그**: 로그인 접속이력(성공/실패), 도서·계정·캠퍼스·과정 CRUD 감사로그, 1년 자동 파기
- **약관 관리**: 관리자 약관 수정 에디터, 회원가입 약관 동의 플로우
- **관리 자동화**: 최초 실행 시 마스터 관리자 계정/설정 초기화 스크립트

<br/>

## 기능 체크리스트
- [x] 로그인 및 권한 구분(운영자/사용자)
- [x] Spring Session + Redis 세션 관리 / 중복 로그인 방지
- [x] 도서 등록/수정/삭제 (바코드/ISBN) / Soft Delete
- [x] 도서 대출/반납 및 이력 관리 (PC 전용, 모바일 차단)
- [x] 통계 대시보드(Chart.js)
- [x] 반납 기한/코스 종료 알림(Discord)
- [x] Docker Compose로 로컬 실행 (DB healthcheck 기반 실행 순서 보장)
- [x] 개발/운영 환경 분리 (docker-compose.dev.yml, docker-compose.prod.yml)
- [x] 공통 응답 형식 (ResponseHandler / Response / ResponseCode)
- [x] DTO Validation (@Valid, GlobalExceptionHandler)
- [x] 공통 감사 컬럼 (BaseAuditEntity — createdBy, updatedAt 등)
- [x] 접속이력 (로그인 성공/실패, Spring Event + @Async)
- [x] 감사로그 (관리자 주요 작업 AOP 자동 기록, @AuditAction)
- [x] 접속이력 1년 자동 파기 (매일 새벽 2시 스케줄러)
- [x] 트랜잭션 롤백 보장 (rollbackFor, readOnly)
- [x] SweetAlert2 공통 alert/confirm
- [x] 관리자 약관 수정 페이지
- [x] 통합 테스트 + GitHub Actions CI (PR·브랜치 push 시 백엔드 테스트·프론트 빌드)
- [x] GitHub Actions CD (main push·`v*.*.*` 태그 → GHCR 이미지 빌드)
- [x] 캠퍼스 내부망 설치 마법사 (`installer/`, Windows `.exe`)
- [ ] OpenAPI 문서 자동화
- [ ] Spring Security 전환 및 RBAC

## 🗂️ 폴더 구조
```
playbook/
 ├─ back/                    # Spring Boot 애플리케이션
 ├─ front/                   # Vue.js 프론트엔드
 ├─ db/                      # MariaDB 설정 · init.sql · migration/NNN_*.sql
 ├─ installer/               # 캠퍼스 PC 설치 마법사 (Electron, Windows .exe)
 ├─ monitoring/alloy/        # 중앙 모니터링 Alloy 사이드카 설정
 ├─ scripts/                 # 배포 · 롤백 · DB 백업/복원 · OCI 서버 스크립트 (scripts/README.md)
 ├─ document/                # 프로젝트 문서 (ERD, 아키텍처, 기획서)
 ├─ .github/workflows/       # CI(ci.yml) · CD(cd.yml) · 설치 마법사 빌드
 ├─ docker-compose.dev.yml   # 개발 환경 설정
 ├─ docker-compose.prod.yml  # 운영 환경 설정 (GHCR 이미지)
 ├─ GUIDE.md                 # 사용자 가이드
 └─ README.md
```

<br/>

## ⚡ 빠른 시작
사전 요구사항: Docker, Docker Compose 설치, api 키 발급

### 개발 환경 실행
```bash
# 1) 레포지토리 클론
git clone <this-repo-url>
cd playbook

# 2) 환경 변수 파일 준비 – 아래 [환경 변수] 참고
# back/.env.dev, db/.env.dev 파일 생성

# 3) 개발 환경 컨테이너 실행
docker compose -f docker-compose.dev.yml up -d --build

# 4) 접속
# Front:   http://localhost:80
# Back:    http://localhost:8080
# MariaDB: localhost:6603
# Redis:   localhost:6379
```

### 운영 환경 실행
```bash
# 1) 환경 변수 파일 준비
# back/.env.prod, db/.env.prod 파일 생성

# 2) 운영 환경 컨테이너 실행 — back/front 는 GHCR 이미지를 pull 한다
docker compose -f docker-compose.prod.yml up -d --build
# 이후 업데이트는 scripts/deploy.sh, 롤백은 scripts/rollback.sh <sha-태그>
```

- 운영 백엔드는 `ddl-auto=validate` 로 동작하므로 스키마 변경은 `db/migration/*.sql` 을 순번대로 직접 적용해야 한다.
  `scripts/sync-dev-to-prod.sh` 는 Dev 데이터로 Prod 를 덮어쓰는 도구이므로 마이그레이션에 쓰지 않는다.
- 캠퍼스 PC 한 대에 설치할 때는 설치 마법사(`installer/README.md`)가 `.env` 생성·DB 마이그레이션·기동까지 처리한다.

### 브랜치 · 릴리즈
- 기본 브랜치는 `main` 하나. 작업은 `main` 에서 브랜치를 따서 PR 로 합친다.
- `main` 에 머지되면 CD 가 GHCR `latest` · `sha-xxxxxxx` 이미지를 만들고, `v*.*.*` 태그를 push 하면 버전 태그 이미지가 추가된다.

<br/>

## 🔑 환경 변수

### 개발 환경 (`back/.env.dev`, `db/.env.dev`)
### 운영 환경 (`back/.env.prod`, `db/.env.prod`)

다음 값을 각 환경별로 입력합니다. (예시 : DB_USERNAME=tbongkim03 (쉼표 없이 엔터 키로 구분))

**백엔드 환경 변수 (back/.env.dev 또는 back/.env.prod)**
- `DB_USERNAME`, `DB_PASSWORD`, 
- `MASTER_ID`, `MASTER_PW`, `MASTER_NAME`, `MASTER_DISCORD`
- `INTEGRATION_SECRET_KEY` (연동 시크릿(봇 토큰·API 키) DB 저장 시 AES 암호화 키)
- `KAKAO_REST_API_KEY` (선택) [카카오 책 검색 API](https://developers.kakao.com/docs/latest/ko/daum-search/dev-guide#search-book) — 국립중앙도서관 결과에 표지가 없을 때 표지 보조 조회. 네이버 책 검색 API는 2026-07-31 종료되어 제거
- `NL_API_KEY`, `WORK24_API_KEY` [국립중앙도서관 API](https://www.nl.go.kr/NL/contents/N31101030500.do), [고용노동부 고용24 API](https://m.work24.go.kr/cm/e/a/0110/selectOpenApiSvcInfo.do?apiSvcId=&upprApiSvcId=&fullApiSvcId=000000000000000000000000000004) 
- `DISCORD_BOT_TOKEN`, `DISCORD_CHANNEL_ID` (tbongkim03@gmail.com 으로 email 부탁드립니다)

**데이터베이스 환경 변수 (db/.env.dev 또는 db/.env.prod)**
- `MYSQL_ROOT_PASSWORD`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`

**권장 보안 수칙**
- 비밀 값은 환경별 `.env` 파일을 사용하여 커밋에서 분리
- 개발/운영 환경 분리와 최소 권한 DB 계정 사용

<br/>

## 🏷️ 바코드 라벨링
- [폼텍 3100](https://www.formshop.co.kr/goods/view?no=18) 에 맞춰진 양식.
- [폼텍 보호용 필름라벨 3102](https://www.formshop.co.kr/goods/view?no=253)

<br/>

## 👨‍💻 개발 가이드
- 백엔드 소스: `back/src/main/java/playbook/encore/back/`
  - 계층 구조: `controller` → `service` → `repository`/`dao` → `entity`/`dto`
  - 도메인: `admin`, `book`, `bookUser`, `campus`, `course`, `favor`, `history`, `sort`, `terms`, `discord`
  - 공통: `common/response/` (ResponseCode, Response, ResponseHandler), `common/util/MobileDetectUtil`
  - 스케줄러: `book/BookReminderScheduler.java`, `course/CourseEndReturnReminderScheduler.java`
  - 전역 설정: `config/WebConfig.java`, 인터셉터: `interceptor/LoginCheckInterceptor.java`
- 프론트 소스: `front/src`
  - 라우팅: `front/src/router/index.js`
  - 주요 화면: `components`/`views` 디렉터리 참조

<br/>

## 🧪 테스트와 품질
- 백엔드 통합 테스트 (`back/src/test`) — GitHub Actions CI 에서 PR·브랜치 push 마다 실행
- 추후 계획: JPA 슬라이스 테스트, Testcontainers 도입.

**품질 기준(로드맵)**
- OpenAPI 기반 API 문서 자동화, 전역 예외 처리 및 표준 에러 응답
- Micrometer/Actuator로 헬스/메트릭/로그 표준화
- GitHub Actions로 빌드/테스트/컨테이너 스캔 자동화

<br/>

## 🗄️ DB 스키마 개요
**핵심 테이블**
- `tb_book`: 도서 정보(ISBN, 제목, 저자, 분류, 바코드, 수량, 대출 여부)
- `tb_user`/`tb_admin`: 사용자/운영자 계정, 약관 동의, 상태
- `tb_history`: 대출/반납 이력(도서/사용자/운영자/과정/일시)
- `tb_sort_first`/`tb_sort_second`: 대분류/소분류 체계
- `tb_favor`: 관심 도서(찜)
- `tb_terms`: 약관 본문 (서비스 이용약관 / 개인정보처리방침 / Discord 알림 동의)

## 🔒 보안과 권한
- 현재: Spring Session + Redis 세션 인증, 인터셉터 기반 로그인 체크, `@Valid` DTO 검증, GlobalExceptionHandler
- 로드맵: Spring Security + RBAC, CORS 정책 정교화

## 🗺️ 향후 개선 계획
- Spring Security 전환 및 표준 RBAC 적용
- OpenAPI(swagger) 문서 자동화
- 컨테이너 취약점 스캔
- Testcontainers 기반 테스트

## 📋 프로젝트 문서
`document/` 폴더에 다음 문서들이 포함되어 있습니다:
- **[플레이북] 도서 관리 프로그램 ERD.png**: 데이터베이스 ERD 다이어그램
- **[플레이북] 도서 관리 프로그램 아키텍처.png**: 시스템 아키텍처 다이어그램  
- **[플레이북] 도서 관리 프로그램 요구사항정의서.pdf**: 상세 요구사항 정의서
- **[플레이북] 도서 관리 프로그램 프로젝트 기획서.pdf**: 프로젝트 기획서

## 📄 라이선스
본 저장소의 코드는 저작권자의 허가 없이 복제, 배포, 수정할 수 없습니다.
본 코드는 [엔코아] 플레이데이터 캠퍼스 내 사용을 목적으로 작성되었으며, 외부 사용을 금지합니다.

---

### 문의
프로젝트 관련 문의는 이메일로 연락 주세요: tbongkim03@gmail.com