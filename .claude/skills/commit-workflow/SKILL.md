---
name: commit-workflow
description: "Playbook 작업 결과를 커밋할 때 반드시 사용하는 스킬. .gitignore 확인 → 파일별 개별 스테이징 → 한국어 커밋 메시지(feat:/fix:/refactor:/test:/docs: + 요구사항 번호) 절차와 커밋 제외 대상(PLAN.md, 계획서, .env, _workspace/)을 다룬다. 커밋해달라는 요청, 스테이징, 커밋 메시지 작성, 변경사항 정리 요청 시, 그리고 커밋 메시지를 다시 쓰거나 스테이징을 고칠 때도 이 스킬을 사용할 것."
---

# Playbook 커밋 워크플로우

커밋은 4단계 순서를 지킨다. `git add .`나 `git commit -am`은 사용하지 않는다 — 이 저장소에는 커밋해서는 안 되는 파일이 상시 존재한다.

## 1단계: .gitignore 확인

작업으로 생긴 파일 중 추적 대상이 아닌 것이 있는지 먼저 확인한다.

```bash
git status --short
```

**커밋하지 않는 것:**

| 대상 | 이유 |
|------|------|
| `docs/` 전체 (`docs/active/`, `docs/archive/`) | 계획서·테스트계획 등 로컬 작업 문서, gitignore 대상 |
| `_workspace/`, `_workspace_*/` | 하네스 중간 산출물 |
| `.env`, `db/.env.*` | 시크릿 |
| `back/build/`, `front/dist/`, `node_modules/`, `backups/` | 빌드·백업 산출물 |
| `scripts/.oci-notify.env` | SMTP 자격증명 |

**커밋해야 하는 것 중 놓치기 쉬운 것:**

| 대상 | 이유 |
|------|------|
| `db/migration/NNN_*.sql` | 스키마 변경 이력. **스키마를 바꾼 코드와 같은 커밋에 넣는다.** 빠지면 다른 환경에서 마이그레이션이 누락된다 |
| `db/init/init.sql`, `db/data/*.sql` | 초기화·기준 데이터 |
| `.gitignore` | 새 산출물 유형이 생겨 갱신했다면 함께 커밋 |

`.gitignore`에 없는 새 산출물 유형이 생겼으면 `.gitignore`를 먼저 갱신하고 그 변경도 커밋에 포함한다.

**시크릿이 스테이징에 들어갔는지 반드시 확인한다.** 토큰·API 키·비밀번호가 포함된 파일은 절대 커밋하지 않는다. 이미 커밋됐다면 즉시 사용자에게 알리고 키 로테이션을 제안한다.

## 2단계: 파일별 개별 스테이징

의도한 파일만 명시적으로 추가한다.

```bash
git add back/src/main/java/playbook/encore/back/book/controller/BookController.java
git add back/src/main/java/playbook/encore/back/book/service/BookServiceImpl.java
git add front/src/api/book.js
git add front/src/components/BooksTable.vue
```

무관한 변경이 섞여 있으면 커밋을 나눈다. 하나의 커밋은 하나의 논리적 변경이다.

스테이징 결과를 커밋 전에 확인한다.

```bash
git diff --cached --stat
```

## 3단계: 커밋 메시지

**한국어**로 작성한다. 형식: `타입: 요구사항번호 요약`

```
feat: X7 도서 엑셀 일괄 등록 기능 추가
fix: 비로그인 도서 목록·검색·캠퍼스 조회 401 차단 회귀 수정
refactor: X1 Barcode·BookPrintBatch raw fetch → bookApi 모듈 통합
test: AL7 접속이력·감사로그 통합 테스트 (AL_1~AL_10, 11개 전체 통과)
docs: AL9 접속이력·감사로그 README·GUIDE 업데이트
```

| 타입 | 용도 |
|------|------|
| `feat` | 신규 기능 |
| `fix` | 버그·회귀 수정 |
| `refactor` | 동작 변경 없는 구조 개선 |
| `test` | 테스트 추가·수정 |
| `docs` | 문서 |
| `chore` | 빌드·설정·스크립트 |

규약:

- **요구사항 번호가 있으면 앞에 붙인다** (`X7`, `AL8`, `P4-1` 등). 리팩토링 계획서·테스트 계획서의 항목 번호와 대응시켜 추적 가능하게 한다
- 요약은 명사형으로 끝낸다 ("추가", "수정", "통합")
- 여러 항목이면 `·`로 연결한다 (`목록·검색·캠퍼스 조회`)
- 테스트 커밋은 케이스 ID 범위와 통과 결과를 괄호로 덧붙인다
- 본문이 필요하면 빈 줄 후 한국어로 상세를 적는다. 대부분은 한 줄로 충분하다
- Co-Authored-By 트레일러는 이 저장소 커밋 이력에 없다. 기존 스타일을 유지해 붙이지 않는다

## 4단계: 커밋 후 문서·메모리 동기화

커밋 자체로 끝나지 않는 항목이 있다.

- **테스트를 추가했다면** `docs/active/TEST_PLAN.md`의 케이스 목록과 진행 상태(✅)를 함께 갱신한다. 코드와 계획서를 동시에 반영하는 것이 이 프로젝트의 규약이다
- **기능을 완료했다면** 관련 계획서(`docs/archive/리팩토링_계획서.md`, `docs/active/모니터링_계획서.md`)의 해당 항목 상태를 갱신한다
- **README·GUIDE에 반영할 사용자 기능이면** `docs:` 커밋을 별도로 만든다

## 브랜치 정책

- 현재 작업 브랜치는 `0.2/refactor`, 기본 브랜치는 `main`
- **`main`에 직접 커밋하지 않는다.** `main`에 있다면 브랜치를 먼저 만든다
- 푸시와 PR 생성은 **사용자가 요청할 때만** 한다. 커밋까지가 기본 범위다
- CI는 `main` 대상 PR과 `main` 외 브랜치 푸시에서 돌아간다 (백엔드 테스트 → 빌드, 프론트 빌드). 푸시 전 로컬에서 최소한 빌드가 통과하는지 확인한다

## 커밋 전 체크

```bash
cd back && ./gradlew compileJava --no-daemon   # 백엔드 변경 시
cd front && npm run build                      # 프론트 변경 시
```

백엔드 로직을 바꿨다면 관련 통합 테스트도 돌린다. 실패한 상태로 커밋하지 않는다. 부득이하게 실패를 남긴다면 커밋 메시지 본문에 그 사실과 이유를 명시한다.
