# CLAUDE.md

## 하네스: Playbook 풀스택 개발

**목표:** 백엔드↔프론트 경계면 계약을 먼저 확정하고, 전문 에이전트가 병렬로 구현·검증해 규약을 벗어나지 않는 변경을 만든다.

**트리거:** Playbook 개발 작업(기능 추가·수정, 버그·회귀 수정, 리팩토링, API·화면 구현, DB 스키마 변경, 통합 테스트, 보안 감사, 배포·모니터링)을 요청받으면 `playbook-dev` 스킬을 사용하라. 이전 결과의 수정·보완·부분 재실행 요청에도 같은 스킬을 사용한다. 단순 질문·단일 파일 조회·설명은 스킬 없이 직접 응답한다.

단독 영역 작업은 해당 규약 스킬을 직접 사용해도 된다: `spring-domain-convention`(백엔드) / `vue-page-convention`(프론트) / `db-migration`(스키마) / `integration-testing`(테스트·검증) / `deploy-ops`(배포·운영) / `commit-workflow`(커밋).

**전역 스킬보다 프로젝트 스킬이 우선한다.** `front/` 작업에는 `vue-page-convention`을 쓰고 `vue-development`·`frontend-design`은 쓰지 않는다 — 전역 스킬은 TypeScript·Testing Library·MSW를 전제하지만 이 프로젝트는 순수 JS이고, 테스트는 백엔드 통합 테스트로, 스타일은 `--pb-*` 토큰으로 한다.

**변경 이력:**

| 날짜 | 변경 내용 | 대상 | 사유 |
|------|----------|------|------|
| 2026-07-30 | 초기 구성 — 에이전트 6, 스킬 7 | 전체 | 풀스택 개발·QA·배포·보안 4개 영역 커버 |
| 2026-07-30 | `db/migration/` gitignore 해제 → 커밋 대상 전환 | `.gitignore`, `db-migration`, `commit-workflow` | 마이그레이션 SQL이 저장소에 남지 않아 환경 간 누락 위험 |
