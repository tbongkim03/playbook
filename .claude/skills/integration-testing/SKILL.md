---
name: integration-testing
description: "Playbook 통합 테스트를 작성·실행하고 백엔드↔프론트 경계면을 검증할 때 반드시 사용하는 스킬. BaseIntegrationTest 상속, 세션 로그인 헬퍼, test 프로파일(실DB 3307 + Embedded Redis 6381), test_data_setup/teardown 픽스처, 케이스 ID 체계, TEST_PLAN.md 동시 갱신, 응답 shape 교차 비교를 다룬다. 테스트 작성·실행 요청, 테스트 실패 원인 분석, 검증·QA·정합성 확인 요청, API가 프론트와 맞는지 확인, 그리고 테스트를 다시 돌리거나 보완할 때도 이 스킬을 사용할 것."
---

# Playbook 통합 테스트·검증 규약

이 프로젝트의 테스트는 목이 아니라 **실제 DB + 실제 HTTP 요청**으로 검증한다. 세션 인증·인터셉터·AOP·트랜잭션이 모두 붙어 있어, 목으로는 정작 깨지는 지점을 잡지 못하기 때문이다.

경계면 교차 검증 절차는 `references/boundary-qa.md`를 읽는다 — 통합 테스트로 잡히지 않는 프론트↔백엔드 불일치를 다룬다.

## 1. 테스트 환경

| 항목 | 값 |
|------|-----|
| 프로파일 | `test` (`application-test.properties`) |
| DB | 실제 MariaDB, 포트 **3307** |
| Redis | Embedded Redis, 포트 **6381** (`EmbeddedRedisConfig`) |
| 웹 환경 | `RANDOM_PORT` + `TestRestTemplate` |
| 환경변수 | `EnvTestInitializer`가 주입 |

로컬 개발 환경(앱 8090 / DB 3307 / Redis 6380)과 Redis 포트를 분리해 충돌을 피한다.

## 2. BaseIntegrationTest 상속

`back/src/test/java/playbook/encore/back/common/BaseIntegrationTest.java`를 상속하면 세션 인증 보일러플레이트가 해결된다.

```java
class BookControllerTest extends BaseIntegrationTest {

    @Test
    @DisplayName("B1: 관리자가 도서 목록을 조회한다")
    void getBooks_asAdmin() {
        HttpHeaders session = loginAsAdmin("test_admin", "Test1234!");

        ResponseEntity<String> res = getWithSession("/books", session);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("\"code\":\"0000\"");
    }
}
```

제공되는 헬퍼:

| 헬퍼 | 용도 |
|------|------|
| `loginAsAdmin(id, pw)` | 관리자 로그인 → 세션 쿠키 `HttpHeaders` 반환 |
| `loginAsUser(id, pw)` | 사용자 로그인 → 세션 쿠키 반환 |
| `getWithSession(url, headers)` | 인증 GET |
| `postWithSession(url, body, headers)` | 인증 POST |
| `putWithSession(url, body, headers)` | 인증 PUT |
| `deleteWithSession(url, body, headers)` | 인증 DELETE (바디 지원) |
| `baseUrl()` | `http://localhost:{port}/api` |

`baseUrl()`이 `/api`를 포함하므로 헬퍼에 넘기는 경로는 `/books`처럼 context-path를 뺀 값이다.

인증 없는 요청은 `restTemplate`을 직접 쓴다 — 비로그인 접근 차단을 검증할 때 필요하다.

## 3. 픽스처

| 파일 | 용도 |
|------|------|
| `src/test/resources/sql/test_data_setup.sql` | campus/sort/course/book 기본 + admin/user/favor/history 테스트 데이터 |
| `src/test/resources/sql/test_data_teardown.sql` | `test_`/`TEST_` 접두사 기반 삭제 |

규약:

- **테스트 데이터는 반드시 `test_` 또는 `TEST_` 접두사를 쓴다.** teardown이 접두사로 삭제하므로, 접두사가 없으면 정리되지 않고 다음 실행을 오염시킨다
- 비밀번호는 BCrypt 해시로 넣는다. 평문 `Test1234!`에 대응하는 해시가 이미 픽스처에 있다
- 새 도메인을 테스트하면 setup에 데이터를, teardown에 삭제 구문을 **양쪽 다** 추가한다
- 스키마가 바뀌어 NOT NULL 컬럼이 생기면 기존 INSERT가 전부 깨진다 — 픽스처를 먼저 갱신한다

## 4. 케이스 ID 체계

도메인 머리글자 + 번호로 케이스를 식별하고, `@DisplayName`에 `ID: 설명` 형태로 적는다.

| 접두사 | 도메인 | 파일 |
|--------|--------|------|
| `A` | 관리자 | `AdminControllerTest` |
| `U` | 사용자 | `BookUserControllerTest` |
| `F` | 즐겨찾기 | `FavorControllerTest` |
| `H` | 대출 이력 | `HistoryControllerTest` |
| `D` | 디스코드 알림 (단위) | `DiscordNotificationServiceTest` |
| `AL_` | 접속이력·감사로그 | `AccessAuditLogControllerTest` |
| `M` | 모니터링 토큰 | `MonitoringTokenServiceTest` |

새 도메인은 충돌하지 않는 머리글자를 골라 `docs/active/TEST_PLAN.md`에 등록한다.

## 5. 필수 케이스 — 권한 차단

기능 검증만 하고 권한 차단을 빼면 이 프로젝트의 핵심 리스크를 놓친다. **도메인마다 최소 3개를 반드시 포함한다.**

1. **비로그인 접근** → 401 (또는 공개 경로면 200 — 어느 쪽이 의도인지 명시)
2. **타권한 접근** — 사용자 세션으로 관리자 API 호출 → 403
3. **캠퍼스 스코프** — 캠퍼스 관리자가 다른 캠퍼스 데이터를 요청 → 차단되거나 자기 캠퍼스로 한정

특히 인터셉터 화이트리스트가 걸린 경로(`/campus`, `/books`, `/books/*`)는 **비로그인 200이 정상**이다. 과거 이 경로가 401로 막히는 회귀가 있었으므로(커밋 `09c744d`), 공개 경로에 대한 비로그인 성공 케이스를 명시적으로 둔다.

## 6. 단정(assertion)

응답 바디는 `{ code, msg, data }` 래핑이다. 최소한 코드와 데이터 형태를 함께 확인한다.

```java
assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
assertThat(res.getBody()).contains("\"code\":\"0000\"");
```

문자열 `contains`만으로는 shape을 검증하기 어렵다. 필드명·중첩 구조가 중요한 경우 `ObjectMapper`로 파싱해 `data`의 타입(배열/객체)과 필드 존재를 확인한다. **프론트가 `res.data.data`로 무엇을 기대하는지가 판정 기준이다.**

권한 실패는 상태 코드와 `ResponseCode`를 같이 본다 (`3001` 인증 필요 / `3002` 권한 없음).

## 7. 실행

```bash
cd back
./gradlew test --no-daemon                                    # 전체
./gradlew test --no-daemon --tests '*FavorControllerTest'      # 단일 클래스
./gradlew test --no-daemon --tests '*FavorControllerTest.addFavor*'
```

리포트: `back/build/reports/tests/test/index.html`

**DB(3307)가 기동돼 있어야 한다.** 미기동이면 컨텍스트 로딩부터 실패한다. 이 경우 테스트를 "통과"로 처리하지 말고 미실행 사유를 명시한다.

CI(`.github/workflows/ci.yml`)에서도 `./gradlew test`가 돌고, 실패하면 빌드 잡으로 진행되지 않는다.

## 8. TEST_PLAN.md 동시 갱신

**테스트 코드와 `docs/active/TEST_PLAN.md`는 항상 함께 갱신한다.** 이 프로젝트의 규약이다.

- `4. 테스트 케이스 목록`에 도메인별 케이스 ID와 설명 추가
- `5. 구현 순서`의 해당 항목에 ✅ + 통과 결과 + 날짜 기록 (`✅ HistoryControllerTest.java (H1~H16) — 전체 통과 (2026-05-29)`)
- 새 픽스처 데이터는 `3. 가짜 데이터 명세`에 반영

계획서만 ✅로 바꾸고 실제로 통과하지 않은 상태를 남기지 않는다. 실패가 있으면 실패 케이스와 원인을 그대로 적는다.

## 9. 검증 결과 보고

테스트 실행 결과는 숫자로 보고한다: `통과 N / 실패 M / 스킵 K`. 실패가 있으면 케이스 ID, 기대값, 실제값, 추정 원인을 함께 낸다. 실패를 요약에서 감추지 않는다.

경계면 검증까지 수행했다면 `references/boundary-qa.md`의 리포트 형식을 따른다.
