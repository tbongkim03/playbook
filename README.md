# 플레이북

<div align="left">

<img alt="Java" src="https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk&logoColor=white" />
<img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot&logoColor=white" />
<img alt="Vue.js" src="https://img.shields.io/badge/Vue.js-3-42B883?logo=vuedotjs&logoColor=white" />
<img alt="MySQL" src="https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white" />
<img alt="Docker" src="https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white" />

</div>

## 캠퍼스 라운지 도서 관리 시스템

---

### 프로젝트 한 줄 소개 (Problem → Solution → Impact)
- **문제**: 라운지 도서가 수기 관리로 인해 분실·연체·현황 파악의 비효율 발생
- **해결**: 바코드 스캔 기반 대여/반납, 대여 이력/통계 대시보드, 반납 알림 자동화
- **임팩트**: 대여/반납 처리 시간을 단축하고, 연체 사전 안내로 분실 감소에 기여



### 목차
- [프로젝트 한눈에 보기](#프로젝트-한눈에-보기)
- [아키텍처](#아키텍처)
- [핵심 설계 포인트](#핵심-설계-포인트)
- [기술 스택](#기술-스택)
- [주요 기능](#주요-기능)
- [기능 체크리스트](#기능-체크리스트)
- [폴더 구조](#폴더-구조)
- [빠른 시작](#빠른-시작)
- [환경 변수](#환경-변수)
- [개발 가이드](#개발-가이드)
- [테스트와 품질](#테스트와-품질)
- [DB 스키마 개요](#db-스키마-개요)
- [보안과 권한](#보안과-권한)
- [향후 개선 계획](#향후-개선-계획)
- [참고 자료](#참고-자료)
- [라이선스](#라이선스)

---

### 프로젝트 한눈에 보기
- **목표**: 라운지 도서의 등록/분류/대여/반납과 연체 알림을 간편하게 운영
- **역할 분리**: 운영자/일반 사용자 권한 구분, 관리자 대시보드 제공
- **기간**: 2025-04-07 ~ 진행 중
- **핵심 포인트**: 실무형 백엔드 아키텍처, Docker 기반 실행, 바코드/Discord 연동

---

### 아키텍처
```
Docker Compose
 ├─ front  (Vue.js, Vite, Chart.js)
 ├─ back   (Spring Boot, JPA, JWT)
 └─ db     (MySQL 8)

외부 연계: 국립중앙도서관 ISBN API, 네이버 도서 검색 API, 고용노동부 고용24 API, Discord Bot 알림
```

---

### 핵심 설계 포인트
- **계층형 아키텍처**: `Controller → Service → Repository/DAO`로 관심사 분리 및 테스트 용이성 확보
- **DTO/Entity 분리**: API 스펙과 영속 모델의 결합 최소화
- **스케줄러 기반 운영 자동화**: 반납 기한/과정 종료에 맞춰 Discord 알림 발송
- **인터셉터 기반 인증 체크**: 공통 인증 로직 재사용으로 컨트롤러 단 단순화
- **도메인 중심 설계**: 대분류/소분류, 과정, 대여 이력 등 핵심 개념을 엔터티로 모델링

---

### 기술 스택
- **Backend**: Java, Spring Boot, Spring Web, Spring Scheduler, JPA(Hibernate), JWT
- **Frontend**: Vue 3, Vue Router, Vite, Chart.js
- **Database**: MySQL 8
- **Infra/Dev**: Docker, Docker Compose, Gradle, Node.js

---

### 주요 기능
- **인증/인가**: 로그인, 운영자/일반 사용자 권한 구분, 로그인 체크 인터셉터
- **도서 관리**: 등록/수정/삭제, 바코드/ISBN 조회, 분류(대분류/소분류)
- **대여/반납**: 바코드 스캔 기반 처리, 대여 현황/연체 관리
- **대시보드/통계**: 대여 비율, 인기 도서, 기간/분야별 통계(Chart.js)
- **알림**: 반납 기한 전 Discord 멘션 알림, 과정 종료 시 반납 리마인더
- **관리 자동화**: 최초 실행 시 마스터 관리자 계정/설정 초기화 스크립트

---

### 기능 체크리스트
- [x] 로그인 및 권한 구분(운영자/사용자)
- [x] 도서 등록/수정/삭제 (바코드/ISBN)
- [x] 도서 대여/반납 및 이력 관리
- [x] 통계 대시보드(Chart.js)
- [x] 반납 기한/코스 종료 알림(Discord)
- [x] Docker Compose로 로컬 실행
- [ ] OpenAPI 문서 자동화
- [ ] Spring Security 전환 및 RBAC
- [ ] 테스트 자동화/CI 구축

### 폴더 구조
```
playbook/
 ├─ back/          # Spring Boot 애플리케이션
 ├─ front/         # Vue.js 프론트엔드
 ├─ db/            # MySQL 설정 및 초기 스크립트
 ├─ docker-compose.yml
 └─ README.md
```

---

### 빠른 시작
사전 요구사항: Docker, Docker Compose 설치

```bash
# 1) 레포지토리 클론
git clone <this-repo-url>
cd playbook

# 2) (선택) .env 파일 준비 – 아래 [환경 변수] 참고

# 3) 컨테이너 실행
docker-compose up -d --build

# 4) 접속
# Front:  http://localhost:5173 (기본 Vite 포트 기준)
# Back:   http://localhost:8080
# MySQL:  localhost:3306
```

---

### 환경 변수
`playbook/back/.env`에서 다음 값을 입력합니다. (예시 : DB_USERNAME=tbongkim03 (쉼표 없이 엔터 키로 구분))
- `DB_USERNAME`, `DB_PASSWORD`, 
- `MASTER_ID`, `MASTER_PW`, `MASTER_NAME`, `MASTER_DISCORD`
- `BASE_KEY`
- `CLIENT_ID`, `CLIENT_SECRET` [네이버 책검색 API](https://developers.naver.com/docs/serviceapi/search/book/book.md)
- `NL_API_KEY`, `WORK24_API_KEY` [국립중앙도서관 API](https://www.nl.go.kr/NL/contents/N31101030500.do), [고용노동부 고용24 API](https://m.work24.go.kr/cm/e/a/0110/selectOpenApiSvcInfo.do?apiSvcId=&upprApiSvcId=&fullApiSvcId=000000000000000000000000000004) 
- `DISCORD_BOT_TOKEN`, `DISCORD_CHANNEL_ID` (tbongkim03@gmail.com 으로 email 부탁드립니다)

권장 보안 수칙
- 비밀 값은 `playbook/back/.env` 파일을 사용하여 커밋에서 분리
- 로컬/운영 환경 분리(`application-{profile}.properties`)와 최소 권한 DB 계정 사용

---

### 개발 가이드
- 백엔드 소스: `back/src/main/java/playbook/encore/back/`
  - 계층 구조: `controller` → `service` → `repository`/`dao` → `entity`/`dto`
  - 스케줄러: `config/BookReminderScheduler.java`, `CourseEndReturnReminderScheduler.java`
  - 인증 토큰 유틸: `jwt/jwtUtil.java`
  - 전역 설정: `config/WebConfig.java`, 인터셉터: `interceptor/LoginCheckInterceptor.java`
- 프론트 소스: `front/src`
  - 라우팅: `front/src/router/index.js`
  - 주요 화면: `components`/`views` 디렉터리 참조

---

### 테스트와 품질
- 수동 테스트 진행
- 추후 계획: Controller 통합 테스트, JPA 슬라이스 테스트, Testcontainers 도입.

품질 기준(로드맵)
- OpenAPI 기반 API 문서 자동화, 전역 예외 처리 및 표준 에러 응답
- Micrometer/Actuator로 헬스/메트릭/로그 표준화
- GitHub Actions로 빌드/테스트/컨테이너 스캔 자동화

---

### DB 스키마 개요
핵심 테이블
- `tb_book`: 도서 정보(ISBN, 제목, 저자, 분류, 바코드, 수량, 대여 여부)
- `tb_user`/`tb_admin`: 사용자/운영자 계정, 약관 동의, 상태
- `tb_history`: 대여/반납 이력(도서/사용자/운영자/과정/일시)
- `tb_sort_first`/`tb_sort_second`: 대분류/소분류 체계
- `tb_favor`: 관심 도서(찜)

---

### 보안과 권한
- 현재: 인터셉터 기반 로그인 체크 + JWT 유틸 사용
- 로드맵: Spring Security + RBAC, `@Valid`/전역 예외 처리, 토큰 만료/리프레시, CORS 정책 정교화

---

### 향후 개선 계획
- Spring Security 전환 및 표준 RBAC 적용
- OpenAPI(swagger) 문서 자동화, 예외/검증 응답 표준화
- Micrometer/Actuator 기반 헬스/메트릭/로그 표준화
- GitHub Actions CI, 멀티스테이지 Docker, 취약점 스캔

---

### 라이선스
본 저장소의 코드는 저작권자의 허가 없이 복제, 배포, 수정할 수 없습니다.
본 코드는 [엔코아] 플레이데이터 캠퍼스 내 사용을 목적으로 작성되었으며, 외부 사용을 금지합니다.

---

### 문의
프로젝트 관련 문의는 이메일로 연락 주세요: tbongkim03@gmail.com

