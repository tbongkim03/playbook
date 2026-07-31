---
name: playbook-dev
description: "Playbook(캠퍼스 라운지 도서 관리 시스템) 개발 작업을 전문 에이전트 팀으로 수행하는 오케스트레이터. 기능 추가·수정, 버그·회귀 수정, 리팩토링, API·화면 구현, DB 스키마 변경, 통합 테스트, 보안·권한 감사, 배포·모니터링 작업을 조율한다. 백엔드와 프론트가 함께 바뀌는 작업, 여러 도메인에 걸친 작업, 요구사항 번호(X7·AL9·P5 등)가 붙은 작업 요청 시 이 스킬을 사용할 것. 후속 작업 — 이전 결과 수정·보완·재실행, '방금 만든 기능 고쳐줘', '테스트만 다시', 'QA 다시 돌려줘', '검증 결과 반영해줘', 부분 재실행 요청에도 반드시 이 스킬을 사용할 것. 단순 질문·단일 파일 조회·설명 요청은 이 스킬 없이 직접 응답한다."
---

# Playbook 개발 오케스트레이터

Playbook의 개발 작업을 전문 에이전트에게 분배하고 조율한다. 리더(이 스킬을 실행하는 메인 에이전트)가 허브가 되어 계약을 확정하고, 구현을 병렬로 돌리고, 검증 결과를 반영한다.

## 실행 모드: 하이브리드 (서브 에이전트 + 리더 허브 통신)

이 환경에는 `TeamCreate`가 없다. 따라서 팀은 **리더가 `Agent` 도구로 스폰하고 `SendMessage`로 후속 조율하는 감독자(supervisor) 구조**로 동작한다. 에이전트끼리 직접 통신하지 않고 모든 조율이 리더를 경유한다.

| Phase | 모드 | 이유 |
|-------|------|------|
| 1 계약 확정 | 리더 직접 | 경계면 계약은 한 사람이 확정해야 어긋나지 않는다 |
| 2 스키마 | 단일 서브 | 후속 구현의 선행 조건 |
| 3 구현 | 서브 병렬 | 백엔드·프론트가 계약서만 보고 독립 진행 |
| 4 검증 | 서브 병렬 | QA와 보안 감사는 서로 독립 |
| 5 반영 | SendMessage | 기존 에이전트의 컨텍스트를 살려 수정 |
| 6 마감 | 리더 직접 (+devops 필요 시) | 테스트·커밋·문서 동기화 |

**모든 `Agent` 호출에 `model: "opus"`를 명시한다.**

`subagent_type`에는 `.claude/agents/`에 정의된 이름(`spring-backend` 등)을 넣는다. 그 타입이 인식되지 않으면 `general-purpose`로 스폰하고 프롬프트 첫 줄에 `먼저 .claude/agents/{이름}.md를 읽고 그 역할·원칙·프로토콜을 그대로 따르라`를 넣는다.

## 에이전트 구성

| 에이전트 | 역할 | 사용 스킬 | 산출물 |
|---------|------|----------|--------|
| `spring-backend` | Spring Boot 도메인 구현 | `spring-domain-convention` | `_workspace/03_backend_report.md` |
| `vue-frontend` | Vue 3 화면·api 모듈 | `vue-page-convention` | `_workspace/03_frontend_report.md` |
| `db-migrator` | MariaDB 스키마·마이그레이션 | `db-migration` | `_workspace/03_db_report.md` |
| `integration-qa` | 경계면 교차 검증 + 통합 테스트 | `integration-testing` | `_workspace/04_qa_report.md` |
| `security-auditor` | 권한·시크릿·개인정보 감사 | (에이전트 정의 내 체크리스트) | `_workspace/04_security_report.md` |
| `devops-operator` | Docker·CI/CD·배포·모니터링 | `deploy-ops` | `_workspace/05_devops_report.md` |

## 라우팅 — 필요한 에이전트만 투입

모든 작업에 6명을 다 쓰지 않는다. 조율 오버헤드가 이득을 넘는다. 요청 성격으로 판단한다.

| 작업 유형 | 투입 | 비고 |
|----------|------|------|
| 백엔드+프론트 기능 추가 | backend, frontend, qa (+db-migrator: 스키마 변경 시) (+security: 권한·개인정보 관련 시) | 표준 흐름 |
| 백엔드만 (API·스케줄러·연동) | backend, qa | 프론트 영향 없음 확인 후 |
| 프론트만 (화면·스타일) | frontend | 계약 변경 없으면 QA 생략 가능 |
| 스키마 변경 | db-migrator, backend, qa | 마이그레이션 목록을 devops에 전달 |
| 버그·회귀 수정 | qa(원인 규명) → 해당 구현자 → qa(재검증) | QA를 먼저 투입해 원인을 특정 |
| 테스트 작성·실행 | qa | 단독 |
| 보안·권한 점검 | security-auditor | 단독. 발견 시 backend 투입 |
| 배포·CI·모니터링 | devops-operator | 단독 |
| 리팩토링 | 해당 구현자 + qa | 동작 불변을 QA가 확인 |

에이전트 1명으로 충분한 작업은 오케스트레이션을 생략하고 그 에이전트만 호출한다. **단일 파일 수정·조회·설명은 리더가 직접 처리한다** — 그 경우 이 스킬을 계속 따를 필요가 없다.

## 워크플로우

### Phase 0: 컨텍스트 확인

1. `_workspace/` 존재 여부를 확인한다
2. 실행 모드를 결정한다:
   - **미존재** → 초기 실행. Phase 1로
   - **존재 + 사용자가 이전 결과의 수정·보완을 요청** → **부분 재실행.** 기존 산출물을 읽고, 해당 에이전트만 재호출한다. `_workspace/`를 지우거나 이동하지 않는다
   - **존재 + 새로운 작업 요청** → **새 실행.** 기존 `_workspace/`를 `_workspace_{YYYYMMDD_HHMMSS}/`로 이동한 뒤 Phase 1로
3. 부분 재실행이면 이전 리포트 경로를 에이전트 프롬프트에 포함해, 기존 결과를 읽고 지정 범위만 고치도록 지시한다

판별이 애매하면 사용자에게 묻지 말고 **부분 재실행으로 처리**한다 — 산출물이 보존되므로 되돌릴 수 있고, 새 실행은 이전 컨텍스트를 잃는다.

### Phase 1: 요구 분석과 계약 확정

**실행 모드: 리더 직접**

1. 요청을 분석해 영향 범위를 파악한다 — 어느 도메인, 어느 계층, 프론트 영향 여부, 스키마 변경 여부
2. 필요하면 관련 코드를 직접 읽거나 `Explore`로 넓게 탐색한다. 요구사항 번호가 있으면 `docs/archive/리팩토링_계획서.md`(R·N·F·X·AL 시리즈) / `docs/active/TEST_PLAN.md` / `docs/active/모니터링_계획서.md`에서 해당 항목을 찾아 맥락을 확보한다
3. `_workspace/` 생성 후 `_workspace/01_requirement.md`에 요구 정리
4. **`_workspace/02_contract.md`에 API 계약서를 작성한다.** 이것이 이 하네스의 핵심 산출물이다

계약서 형식:

```markdown
# API 계약서 — {기능명}

## 엔드포인트
| # | 메서드 | 경로 (context-path /api 제외) | 권한 | 설명 |
|---|--------|------------------------------|------|------|
| 1 | GET | /books/import-history | ADMIN | 엑셀 등록 이력 조회 |

## 1. GET /books/import-history
### 요청
| 위치 | 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|------|
| query | seqCampus | Integer | N | 미지정 시 세션 캠퍼스 |

### 응답 (ResponseHandler.success(data) 래핑 후)
```json
{ "code": "0000", "msg": "성공",
  "data": [ { "seqImport": 1, "fileName": "books.xlsx", "successCount": 12, "importedAt": "2026-07-30T10:00:00" } ] }
```
프론트 접근 경로: `res.data.data` → 배열

### 권한 상세
- ADMIN 필요. 캠퍼스 관리자는 자기 캠퍼스만. 전체관리자는 전 캠퍼스
- 인터셉터 화이트리스트 추가: 불필요

## 스키마 변경
- `tb_book_import` 신규 (db-migrator 담당)

## 상태 코드
- 실패 시 `1001 NO_DATA` / `3002 NOT_AUTHORIZED`
```

계약서를 왜 리더가 쓰는가: 백엔드와 프론트가 각자 "합리적으로" 구현하면 필드명·래핑·권한이 어긋난다. 이 프로젝트에서 가장 자주 나는 버그가 그 경계면이다. 계약을 먼저 못 박으면 병렬 구현이 불가능하다.

**계약서 없이 Phase 3으로 넘어가지 않는다.** 단독 에이전트 작업(프론트만, 배포만)은 예외다.

### Phase 2: 스키마 (해당 시)

**실행 모드: 단일 서브 에이전트**

스키마 변경이 있으면 `db-migrator`를 먼저 호출한다. 구현이 스키마에 의존하므로 선행이다.

```
Agent(subagent_type: "db-migrator", model: "opus",
      description: "스키마 마이그레이션 작성",
      prompt: "계약서 _workspace/02_contract.md의 '스키마 변경' 항목을 구현하라.
               산출물은 db/migration/NNN_*.sql과 _workspace/03_db_report.md.
               운영 배포 시 필수 실행 SQL 목록을 반드시 포함하라.")
```

### Phase 3: 병렬 구현

**실행 모드: 서브 에이전트 병렬 — 단일 메시지에서 동시 호출**

`spring-backend`와 `vue-frontend`를 한 번에 스폰한다. 둘은 계약서만 보고 독립적으로 진행하므로 서로를 기다릴 필요가 없다.

```
Agent(subagent_type: "spring-backend", model: "opus", description: "백엔드 구현",
      prompt: "_workspace/01_requirement.md와 _workspace/02_contract.md를 읽고 백엔드를 구현하라.
               스키마 리포트: _workspace/03_db_report.md (있으면).
               산출물: 코드 + _workspace/03_backend_report.md. 컴파일 확인 후 보고.")

Agent(subagent_type: "vue-frontend", model: "opus", description: "프론트 구현",
      prompt: "_workspace/01_requirement.md와 _workspace/02_contract.md를 읽고 프론트를 구현하라.
               백엔드 완성을 기다리지 말고 계약서 기준으로 진행하라.
               산출물: 코드 + _workspace/03_frontend_report.md. 빌드 확인 후 보고.")
```

에이전트가 계약 변경을 요청하면 **리더가 판정한다.** 승인하면 `02_contract.md`를 갱신하고 **양쪽 모두에게** `SendMessage`로 변경을 알린다. 한쪽만 알리면 경계면이 깨진다.

### Phase 4: 검증

**실행 모드: 서브 에이전트 병렬**

구현 리포트가 도착하면 `integration-qa`와 `security-auditor`를 동시에 스폰한다.

```
Agent(subagent_type: "integration-qa", model: "opus", description: "경계면 검증·통합 테스트",
      prompt: "_workspace/02_contract.md를 판정 기준으로, 03_backend_report.md와
               03_frontend_report.md의 변경분에 대해 경계면 교차 검증을 수행하고
               통합 테스트를 작성·실행하라. 산출물: _workspace/04_qa_report.md.
               이슈마다 수정 담당을 지정하라.")

Agent(subagent_type: "security-auditor", model: "opus", description: "보안·권한 감사",
      prompt: "이번 변경분의 권한 게이트·캠퍼스 스코프·시크릿·감사로그를 감사하라.
               기준: _workspace/02_contract.md의 권한 명세.
               산출물: _workspace/04_security_report.md.")
```

**규모가 큰 작업은 모듈 단위로 QA를 앞당긴다.** 엔드포인트 묶음이 완성될 때마다 그 범위만 검증하도록 호출하면, 초기 불일치가 후속 모듈로 전파되지 않는다. 전체 완성 후 한 번만 검증하면 수정 비용이 커진다.

### Phase 5: 검증 결과 반영

**실행 모드: SendMessage (기존 에이전트 재활용)**

1. 두 리포트를 읽고 이슈를 심각도순으로 통합한다
2. 담당별로 묶어 **`SendMessage`로 해당 에이전트에게 수정을 요청한다.** 새 `Agent` 호출보다 낫다 — 그 에이전트는 이미 코드 맥락을 갖고 있다
3. 수정 완료 후 `integration-qa`에게 재검증을 요청한다 (`SendMessage`). QA는 "수정했다"는 보고를 신뢰하지 않고 코드를 다시 열어 확인한다
4. 반복 종료 조건:
   - `BLOCKER` / `CRITICAL` / `HIGH` **0건** → 통과
   - 2회 반복 후에도 잔존 → 사용자에게 상태를 보고하고 진행 여부를 확인. 리더가 임의로 무시하지 않는다

`security-auditor`가 `CRITICAL`/`HIGH`를 남긴 상태로 Phase 6 커밋 단계로 넘어가지 않는다.

### Phase 6: 마감

**실행 모드: 리더 직접 (+ 필요 시 devops-operator)**

1. **빌드·테스트 확인**
   ```bash
   cd back && ./gradlew compileJava --no-daemon     # 백엔드 변경 시
   cd back && ./gradlew test --no-daemon            # 로직 변경 시
   cd front && npm run build                         # 프론트 변경 시
   ```
2. **배포 영향이 있으면** `devops-operator`를 호출한다. 스키마 변경이 있었다면 `03_db_report.md`의 필수 실행 SQL 목록을 프롬프트에 반드시 전달한다 — 이 전달이 끊기면 운영 장애가 된다
3. **문서 동기화** — 테스트를 추가했으면 `docs/active/TEST_PLAN.md`(케이스 목록 + ✅ + 날짜), 요구사항 항목을 완료했으면 `docs/` 하위 해당 계획서, 사용자 기능이면 `README.md`/`GUIDE.md`
4. **커밋** — `commit-workflow` 스킬을 따른다. `_workspace/`는 커밋하지 않는다
5. **결과 보고** — 아래 형식

```markdown
## 완료
- {구현 내용 요약}
- 변경 파일: 백엔드 N개 / 프론트 M개 / SQL K개

## 검증
- 통합 테스트: 통과 N / 실패 M
- 경계면 검증: 이슈 N건 발견 → N건 해결
- 보안 감사: CRITICAL 0 / HIGH 0 / MEDIUM n

## 사용자 조치 필요
- {운영 마이그레이션 실행, 배포, 환경변수 등록 등}

## 미완료·보류
- {있으면 반드시 명시}
```

6. **피드백 요청** — "결과나 팀 구성에서 개선할 부분이 있나요?" 한 줄. 피드백이 오면 `harness` 스킬로 하네스를 갱신한다

## 데이터 흐름

```
[리더] 01_requirement.md → 02_contract.md
           │
           ├─→ db-migrator ──→ 03_db_report.md ──┐
           │                                      │
           ├─→ spring-backend ─→ 03_backend_report.md
           ├─→ vue-frontend ──→ 03_frontend_report.md
           │        (02_contract.md만 보고 병렬 진행)
           │                          │
           ├─→ integration-qa ───→ 04_qa_report.md
           ├─→ security-auditor →─ 04_security_report.md
           │                          │
           │←─── SendMessage 수정 요청 ─┘
           │
           └─→ devops-operator → 05_devops_report.md
                     ↓
           빌드·테스트 → 문서 동기화 → 커밋
```

전달 방식: **파일 기반**(산출물·계약서) + **SendMessage**(수정 요청·계약 변경 통보) + **반환값**(완료 보고). `_workspace/`는 삭제하지 않는다 — 사후 검증과 재실행의 근거다.

## 에러 핸들링

| 상황 | 전략 |
|------|------|
| 에이전트 1명 실패 | 1회 재시도. 재실패 시 그 영역을 누락으로 명시하고 나머지를 완성. 조용히 넘기지 않는다 |
| 백엔드·프론트가 서로 다른 계약 변경 요청 | 리더가 계약서 기준으로 판정. 양쪽에 결정을 통보 |
| QA와 보안 감사가 상충하는 수정 요구 | 보안 우선. QA 요구를 보안 제약 안에서 재해석해 전달 |
| 컴파일·빌드 실패가 반복 | 3회 후 에러 전문과 함께 사용자에게 보고. 실패를 감추고 완료 보고하지 않는다 |
| 테스트가 DB 미기동으로 실행 불가 | 정적 검증만 수행하고 "테스트 미실행"을 명시. 통과로 처리하지 않는다 |
| 이슈가 2회 반복 후에도 잔존 | 사용자에게 상태 보고 후 진행 여부 확인 |
| 요청이 단일 파일 수정 수준 | 오케스트레이션을 생략하고 리더가 직접 처리 |

## 테스트 시나리오

### 정상 흐름 — "도서 엑셀 일괄 등록 이력 화면 추가해줘"

1. Phase 0: `_workspace/` 없음 → 초기 실행
2. Phase 1: `book` 도메인 + 신규 테이블 필요 판단, 계약서 작성 (`GET /books/import-history`, ADMIN, 배열 응답)
3. Phase 2: `db-migrator` → `db/migration/004_add_book_import.sql` + 필수 실행 SQL 목록
4. Phase 3: `spring-backend`(엔드포인트) + `vue-frontend`(관리자 탭) 병렬
5. Phase 4: `integration-qa`(경계면 + `B1~B6` 테스트) + `security-auditor`(ADMIN 게이트·캠퍼스 스코프) 병렬
6. Phase 5: QA가 캠퍼스 스코프 누락 1건 발견 → `SendMessage`로 backend 수정 → 재검증 통과
7. Phase 6: 테스트 통과 확인, `docs/active/TEST_PLAN.md` 갱신, `feat: X8 도서 엑셀 등록 이력 조회 추가` 커밋, 운영 마이그레이션 실행을 사용자 조치로 안내

### 에러 흐름 — 프론트 에이전트 실패

1. Phase 3에서 `vue-frontend`가 빌드 에러 반복으로 중단
2. 리더가 1회 재시도 → 재실패
3. 백엔드 결과만으로 Phase 4 진행, QA에게 "프론트 미완성, 백엔드 단독 검증" 범위를 명시해 호출
4. Phase 6 보고서에 "프론트 화면 미완성 — {에러 내용}, 재작업 필요"를 **미완료 항목으로 명시**
5. 커밋은 백엔드 변경만 포함하고, 프론트 미완성 사실을 사용자에게 보고
