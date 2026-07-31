---
name: deploy-ops
description: "Playbook 배포·운영 인프라를 다룰 때 반드시 사용하는 스킬. docker-compose.{dev,prod}.yml, GitHub Actions CI/CD(GHCR 이미지·태깅 전략·SSH 배포 비활성 상태), scripts/의 deploy·rollback·migrate-db·backup 스크립트, OCI 부트스트랩, Actuator/Prometheus 중앙 모니터링(단계 C Alloy 사이드카 남음)을 다룬다. 배포·롤백·이미지 빌드·컨테이너·CI 실패·워크플로우 수정·환경변수·모니터링·서버 구성 요청 시, 그리고 배포 절차를 다시 정리하거나 보완할 때도 이 스킬을 사용할 것."
---

# Playbook 배포·운영 규약

## 1. 실행 안전 원칙

**운영에 영향을 주는 명령은 사용자 승인 후에만 실행한다.** 설정·스크립트 작성과 로컬 검증까지가 기본 범위다.

승인 없이 실행하지 않는 것: 원격 서버 배포, 운영 컨테이너 재시작, 운영 DB 마이그레이션, 이미지 푸시, `docker image prune`, 태그 푸시(CD 트리거).

로컬에서 검증만 하는 명령은 자유롭게 쓴다.

```bash
docker compose -f docker-compose.prod.yml config    # 문법 검증
bash -n scripts/deploy.sh                            # 스크립트 문법 검증
```

## 2. 환경 구성

| 환경 | 파일 | 앱 | DB | Redis | 비고 |
|------|------|-----|-----|-------|------|
| local | `bootRun` (`--spring.profiles.active=local`) | 8090 | 3307 | 6380 | dev와 충돌 방지용 분리 |
| test | `application-test.properties` | RANDOM | 3307 | 6381 (Embedded) | 통합 테스트 |
| dev | `docker-compose.dev.yml` | — | — | — | 개발 서버 |
| prod | `docker-compose.prod.yml` | back 8080 / front 80 | 6603→3306 | 내부 | GHCR 이미지 |

**포트 값을 임의로 바꾸지 않는다.** 로컬과 dev를 분리해 둔 의도적 설정이다.

prod 서비스 구성: `back` (GHCR 이미지) / `db` (`./db` 빌드, healthcheck 후 back 기동) / `redis` (redis:7-alpine) / `front` (GHCR 이미지, 80). nginx·prometheus·grafana는 주석 처리된 상태다.

## 3. CI — .github/workflows/ci.yml

트리거: `main` 대상 PR, `main` 외 브랜치 푸시.

```
backend-test (./gradlew test)  →  backend (./gradlew build -x test)
frontend (npm ci && npm run build)   ← 병렬
```

**`backend-test` → `backend` 의존 관계를 유지한다.** 테스트를 건너뛰는 변경을 임의로 넣지 않는다. 테스트 리포트는 `back/build/reports/tests/test/`가 아티팩트로 업로드된다.

Java 17 (temurin), Node 18, Gradle·npm 캐시 사용.

## 4. CD — .github/workflows/cd.yml

트리거: `main` 푸시, `v*.*.*` 태그 푸시, 수동(`workflow_dispatch`).

```
test (./gradlew test)  →  build-and-push (GHCR)  →  deploy (if: false — 비활성)
```

이미지:

- `ghcr.io/tbongkim03/playbook-back` — 컨텍스트 `./back`, `back/Dockerfile`
- `ghcr.io/tbongkim03/playbook-front` — 컨텍스트 `./front`, `front/ProdDockerfile`

태깅 전략:

| 트리거 | 태그 |
|--------|------|
| `main` 푸시 | `sha-{7자리}`, `latest` |
| `v*.*.*` 태그 | `sha-{7자리}`, `{version}` (latest 제외) |

**SSH 배포 잡은 `if: false`로 비활성 상태다.** 활성화 조건이 워크플로우 주석에 적혀 있다: `if`를 `needs.build-and-push.result == 'success'`로 바꾸고, Secrets에 `SSH_PRIVATE_KEY` / `SERVER_HOST` / `SERVER_USER` / `SERVER_DEPLOY_PATH`를 등록한다. **활성화는 사용자 결정 사항이므로 임의로 켜지 않는다.**

## 5. 운영 스크립트 — scripts/

| 스크립트 | 용도 |
|---------|------|
| `deploy.sh` | GHCR `latest` pull → `docker compose -f docker-compose.prod.yml up -d --remove-orphans back front` → 미사용 이미지 정리 |
| `rollback.sh <sha-태그>` | 특정 sha 태그로 back/front 롤백 |
| `migrate-db.sh` | DB 마이그레이션 실행 |
| `backup-tables.sh` / `restore-tables.sh` | 테이블 백업 / 복구 |
| `oci-discover.sh` | OCI 리소스 조회 |
| `oci-a1-launch-retry.sh` | OCI A1 인스턴스 생성 재시도 |
| `oracle-bootstrap.sh` | 서버 초기 부트스트랩 |
| `oci-deploy-notify.sh` | 배포 알림 |

스크립트를 추가하면 `scripts/README.md`에 사용법(사용법·동작 단계)을 같은 형식으로 등록한다.

## 6. 배포 절차 (스키마 변경 포함)

**DB 스키마 변경이 있는 배포는 마이그레이션 단계를 반드시 절차에 명시한다.** `db-migrator`가 전달한 "운영 배포 시 필수 실행 SQL 목록"이 절차에 반영되지 않으면 운영 장애가 된다 — 이 프로젝트의 알려진 리스크 1순위다. `ddl-auto=update`는 컬럼 삭제·타입 축소·제약 추가를 반영하지 않는다.

표준 순서:

```
1. 사전 확인 — 대상 이미지 태그, 마이그레이션 SQL 목록, 현재 가동 버전(롤백 대상 sha)
2. 백업 — scripts/backup-tables.sh 로 변경 대상 테이블 백업
3. 마이그레이션 — scripts/migrate-db.sh (또는 SQL 직접 실행). 각 SQL의 확인 쿼리 결과를 확인
4. 배포 — scripts/deploy.sh
5. 검증 — 헬스체크 + 주요 화면 동작 + 로그 확인
6. 실패 시 — scripts/rollback.sh <이전-sha> + 마이그레이션 롤백 SQL
```

마이그레이션과 배포의 순서는 변경 성격에 따라 다르다. 컬럼 **추가**는 마이그레이션 선행이 안전하고(구버전 앱이 새 컬럼을 몰라도 동작), 컬럼 **삭제**는 배포 선행이 안전하다(구버전 앱이 삭제된 컬럼을 참조하면 즉시 장애). 절차서에 이 판단 근거를 남긴다.

## 7. 검증

```bash
curl -s http://{host}/api/actuator/health          # 애플리케이션 상태
docker compose -f docker-compose.prod.yml ps        # 컨테이너 상태
docker compose -f docker-compose.prod.yml logs -f --tail=100 back
```

DB healthcheck가 통과해야 `back`이 기동한다 — `back`이 안 뜨면 `db` 상태를 먼저 본다.

## 8. 시크릿

- `.env`, `db/.env.dev`, `db/.env.prod`는 **커밋하지 않는다.** 작업 후 `git status`로 확인한다
- CI/CD 시크릿은 GitHub Secrets로 주입한다. 워크플로우 파일에 값을 적지 않는다
- 애플리케이션 시크릿은 `${ENV_NAME:기본값}` 형태로 주입된다. **`application.properties`의 기본값은 개발용이며 운영에서 쓰이면 안 된다** — 특히 `INTEGRATION_SECRET_KEY`, `MASTER_PW`
- 필요한 환경변수를 문서화할 때는 **키 이름만** 적는다

주요 환경변수 키: `INTEGRATION_SECRET_KEY`, `MASTER_ID/PW/NAME/DISCORD`, `CLIENT_ID/CLIENT_SECRET`(네이버), `NL_API_KEY`, `WORK24_API_KEY`, `DISCORD_CHANNEL_*`, `MONITORING_*`, `BACK_IMAGE`/`FRONT_IMAGE`.

시크릿이 이미 커밋됐다면 즉시 사용자에게 보고하고 키 로테이션 절차를 함께 제시한다. 히스토리 재작성은 사용자 승인 사항이다.

## 9. 모니터링

구조: 캠퍼스측 백엔드가 `/actuator/prometheus`를 노출 → Alloy 사이드카가 scrape → AWS 중앙 모니터링으로 remote_write.

현재 상태:

| 단계 | 내용 | 상태 |
|------|------|------|
| AWS Phase 2·3 | 중앙 모니터링 EC2 배포 | 완료 |
| 캠퍼스 단계 A | 모니터링 토큰 자동 회전 스케줄러 | 완료 (커밋 `73fd7b6`) |
| 캠퍼스 단계 B | Actuator + Prometheus 메트릭 노출 | 완료 (커밋 `91ed6fa`) |
| 캠퍼스 단계 C | **Alloy 사이드카 컨테이너 + 캠퍼스측 `.env`** | **남음** |

모니터링 작업 요청을 받으면 `docs/active/모니터링_계획서.md`를 먼저 읽어 현재 단계를 확인한다.

규약:

- **Actuator를 외부에 직접 노출하지 않는다.** `management.endpoints.web.exposure.include=health,prometheus`만 유지하고, 프로파일별 `management.server.port`로 격리한다
- 토큰 회전은 `monitoring.token.refresh.enabled`로 제어되며 캠퍼스 배포에서만 `true`다. 미설정 시 빈이 생성되지 않아 로컬 실행을 방해하지 않는다 — 이 패턴을 유지한다

## 산출물

작업 후 다음을 보고한다:

1. 변경 파일 목록과 요약
2. **배포 절차** — 실행 순서, 명령, 사전 조건
3. **마이그레이션 실행 단계** (해당 시) — 어느 시점에 어떤 SQL을
4. **롤백 절차**
5. 필요한 환경변수·시크릿 **키 이름** 목록
6. 검증 방법 — 헬스체크 URL, 확인할 로그, 메트릭
7. 사용자가 직접 실행해야 하는 항목 (승인 필요 명령)
