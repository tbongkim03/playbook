---
name: spring-domain-convention
description: "Playbook 백엔드(Spring Boot 3.1.12) 코드를 작성·수정할 때 반드시 사용하는 규약 스킬. 도메인 패키지 구조(entity/controller/dao/service/dto), ResponseHandler 통일 응답, AuthUtil 권한 게이트, LoginCheckInterceptor 화이트리스트, BaseAuditEntity Soft Delete, @AuditAction 감사로그, DTO @Valid 검증, ResponseCode 코드 체계를 다룬다. back/ 하위 Java 파일을 만들거나 고칠 때, API 엔드포인트를 추가·수정할 때, 엔티티·DTO·서비스·레포지토리를 작성할 때, 외부 API 연동이나 스케줄러를 붙일 때, 그리고 그 결과를 다시 고치거나 보완할 때도 이 스킬을 사용할 것."
---

# Playbook 백엔드 도메인 규약

`back/src/main/java/playbook/encore/back/` 하위 코드는 아래 규약을 따른다. 규약이 강하게 확립된 코드베이스이므로, 동작하더라도 규약을 벗어난 코드는 되돌려진다. 판단이 애매하면 최신 도메인(`integration`, `course`, `auditlog`)을 기준으로 삼는다.

## 1. 도메인 패키지 구조

도메인 하나가 패키지 하나다. 계층은 5개로 고정한다.

```
{domain}/
├── entity/       {Domain}.java              — JPA 엔티티
├── dto/          {Domain}RequestDto.java    — 요청 (@Valid 검증)
│                 {Domain}ResponseDto.java   — 응답
├── dao/          {Domain}DAO.java           — 인터페이스
│                 {Domain}DAOImpl.java       — @Component 구현
│                 {Domain}Repository.java    — JpaRepository
├── service/      {Domain}Service.java       — 인터페이스
│                 {Domain}ServiceImpl.java   — @Service 구현
└── controller/   {Domain}Controller.java    — @RestController
```

호출 방향은 `Controller → Service → DAO → Repository`다. 컨트롤러가 Repository를 직접 부르지 않는다.

**인터페이스 + Impl 쌍을 왜 지키는가:** 모든 기존 도메인이 이 형태이고, 통합 테스트에서 목 주입 지점이 되기 때문이다. 서비스가 단순 위임뿐이어도 쌍을 만든다.

의존성 주입은 **생성자 주입 + `@Autowired`** 명시 형태를 쓴다 (`@RequiredArgsConstructor`가 아니라 기존 스타일 유지).

## 2. 통일 응답 — ResponseHandler

컨트롤러는 예외 없이 `ResponseEntity<Response>`를 반환한다.

```java
@GetMapping
public ResponseEntity<Response> getFavor(HttpServletRequest request) throws Exception {
    BookUser user = AuthUtil.getUser(request);
    List<FavorResponseDto> data = favorService.getFavorList(user);
    return ResponseEntity.ok(ResponseHandler.success(data));
}
```

| 상황 | 호출 |
|------|------|
| 성공 (데이터 없음) | `ResponseHandler.success()` |
| 성공 (데이터 있음) | `ResponseHandler.success(data)` |
| 성공 (메시지 포함) | `ResponseHandler.success(data, "메시지")` |
| 실패 | `ResponseHandler.error(ResponseCode.X)` |
| 실패 (동적 메시지) | `ResponseHandler.error(ResponseCode.X, "상세")` |

최종 JSON은 항상 `{ code, msg, data }`로 감싸진다. **프론트는 `res.data.data`로 실제 데이터에 접근한다** — 응답 shape을 설계할 때 이 이중 래핑을 전제로 기록한다.

### ResponseCode 체계

새 에러 상황이 필요하면 `common/response/ResponseCode.java`에 코드 체계에 맞춰 추가한다. 기존 코드를 재활용할 수 있으면 추가하지 않는다.

| 대역 | 의미 | 예시 |
|------|------|------|
| `0000` | 성공 | `SUCCESS` |
| `1xxx` | 데이터 상태 | `NO_DATA`, `EXIST_INFO`, `TIMEOUT`, `TARGET_DISABLED` |
| `2xxx` | 파라미터 오류 | `INVALID_PARAM`, `INVALID_PARAM_LEN`, `INVALID_PARAM_PATTERN` |
| `3xxx` | 인증·권한 | `NOT_AUTHENTICATED`, `NOT_AUTHORIZED`, `NO_SESSION` |
| `4xxx` | 처리 실패 | `FAIL_INSERT`, `FAIL_UPDATE`, `FAIL_DELETE`, `FAIL_PROCESS` |
| `9xxx` | 기타 | `UNKNOWN`, `NOT_IMPLEMENTED` |

메시지에 `%s`가 있는 코드는 포맷 인자를 넘긴다. 예외는 `GlobalExceptionHandler`가 잡아 응답으로 변환하므로, 서비스 계층에서는 `IllegalArgumentException` 등을 던져도 된다.

## 3. 권한 게이트 — AuthUtil

컨트롤러 진입부에서 권한을 확정한다. 세션을 직접 뒤지거나 `request.getAttribute("ROLE")`을 수동 비교하지 않는다.

| 호출 | 통과 조건 | 반환 |
|------|----------|------|
| `AuthUtil.getUser(request)` | 로그인 사용자 | `BookUser` |
| `AuthUtil.getAdmin(request)` | 관리자 | `Admin` |
| `AuthUtil.requireAdmin(request)` | 관리자 | void |
| `AuthUtil.requireSuperAdmin(request)` | 전체관리자 (`seqCampus == null`) | void |
| `AuthUtil.isAdmin/isUser/isSuperAdmin` | 분기 판단용 | boolean |
| `AuthUtil.getCampusId(request, param)` | — | 파라미터 우선, 없으면 세션 캠퍼스 |

**전역 설정 기능(연동 설정, 마스터 계정, 모니터링 토큰)은 반드시 `requireSuperAdmin`을 쓴다.** 캠퍼스 관리자가 전역 설정을 만지면 권한 상승이다.

**캠퍼스 스코프 주의:** `getCampusId(request, requestParam)`은 파라미터를 우선한다. 캠퍼스 관리자가 남의 캠퍼스 ID를 파라미터로 보내면 통과하므로, 캠퍼스 관리자 경로에서는 세션 캠퍼스와 일치하는지 별도 검증한다.

## 4. 인터셉터 화이트리스트

`interceptor/LoginCheckInterceptor.java`가 인증을 일괄 처리한다. **비로그인 접근을 허용하는 엔드포인트를 추가하려면 여기를 반드시 함께 수정한다.**

현재 공개 경로: `/campus`, `/books`, `/books/`로 시작하는 조회 경로.

과거 이 누락으로 비로그인 도서 목록·검색·캠퍼스 조회가 401로 막히는 회귀가 발생했다(커밋 `09c744d`). 반대로 `startsWith` 패턴을 넓게 열면 하위 변경 엔드포인트까지 인증 없이 통과하므로, 패턴을 추가할 때는 그 경로 아래 POST/PUT/DELETE가 생길 가능성을 검토한다.

`server.servlet.context-path=/api`가 설정돼 있어, 컨트롤러 매핑이 `/favor`면 실제 URL은 `/api/favor`다. 인터셉터 경로 비교는 context-path가 제거된 URI로 이뤄진다.

## 5. 엔티티 — BaseAuditEntity + Soft Delete

```java
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Where(clause = "use_yn = 'Y'")
@Table(name = "tb_favor")
public class Favor extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_favor", nullable = false)
    private Integer seqFavor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seq_user", nullable = false)
    private BookUser seqUser;
}
```

규약:

- `BaseAuditEntity` 상속 — `use_yn`, `created_at/by/by_type`, `updated_at/by/by_type`가 `AuditEntityListener`로 자동 기록된다
- `@Where(clause = "use_yn = 'Y'")` — 조회 시 삭제 행 자동 제외
- **삭제는 `setUseYn("N")` + `save()`**. `delete()` 물리 삭제 금지
- 테이블 `tb_`, 컬럼 snake_case, PK는 `seq_{domain}`
- 연관관계는 `FetchType.LAZY` (`open-in-view=false`이므로 서비스 계층에서 필요한 필드를 미리 로드하거나 DTO 프로젝션을 쓴다)

## 6. Repository — DTO 프로젝션과 Soft Delete 쿼리

```java
public interface FavorRepository extends JpaRepository<Favor, Integer> {

    /** 사용자의 즐겨찾기 목록 조회 (도서 정보 포함 DTO 반환) */
    @Query("SELECT new playbook.encore.back.favor.dto.FavorResponseDto(f.seqFavor, b.seqBook, b.titleBook) " +
            "FROM Favor f JOIN f.seqBook b WHERE f.seqUser = :user")
    List<FavorResponseDto> findFavorsByUser(@Param("user") BookUser user);

    @Modifying
    @Query("UPDATE Favor f SET f.useYn = 'N' WHERE f.seqUser = :user")
    void softDeleteBySeqUser(@Param("user") BookUser user);
}
```

- 조회 성능이 필요하면 `new` 생성자 표현식으로 DTO를 직접 프로젝션한다 (LAZY + `open-in-view=false` 우회)
- 벌크 소프트 삭제는 `@Modifying` + `@Query`
- 쿼리 메서드마다 한국어 Javadoc 한 줄
- **문자열 연결로 JPQL을 만들지 않는다.** 반드시 `@Param` 바인딩

## 7. DTO

- 요청은 `{Domain}RequestDto`, 응답은 `{Domain}ResponseDto`. 목적이 구체적이면 `AdminPasswordUpdateRequestDto`처럼 세분화한다
- 요청 DTO에 `@NotBlank`, `@Size`, `@Pattern` 등 제약을 걸고 컨트롤러에서 `@Valid`로 검증한다. 검증 실패는 `GlobalExceptionHandler`가 `2xxx` 코드로 변환한다
- **엔티티를 컨트롤러 응답에 직접 반환하지 않는다.** 개인정보 과다 노출과 LAZY 직렬화 오류의 원인이다
- `@RequestBody int bookId`처럼 원시 타입을 받는 기존 엔드포인트가 있다. 프론트가 `Content-Type: application/json`을 명시해야 동작하므로, 신규 엔드포인트는 가급적 DTO로 감싼다

## 8. 감사 로그 — @AuditAction

관리자의 변경 작업은 **ServiceImpl 메서드**에 애노테이션을 붙여 `tb_audit_log`에 자동 기록한다.

```java
@AuditAction(action = "BOOK_CREATE", targetType = "BOOK")
public void createBook(...) { ... }
```

- `action`: `{대상}_{동작}` 대문자 (`BOOK_CREATE`, `CAMPUS_UPDATE`, `USER_DELETE`)
- `targetType`: 대상 유형 (`BOOK`, `USER`, `ADMIN`, `CAMPUS`, `COURSE`)
- 컨트롤러가 아니라 ServiceImpl에 붙인다 (AOP 대상이 서비스 계층)
- 조회에는 붙이지 않는다. 생성·수정·삭제에만

접속 이력은 별개다 — 로그인 이벤트가 Spring Event로 `accesslog`에 기록되며, 수집 항목을 늘리면 약관 고지 내용도 함께 갱신해야 한다.

## 9. 설정과 프로파일

- 공통은 `application.properties`, 환경별은 `application-{local,dev,prod,test}.properties`
- 외부 값은 `${ENV_NAME:기본값}` 형태로 주입. **시크릿의 기본값은 개발 편의용이며 운영에서 쓰이면 안 된다**
- 연동 시크릿(봇 토큰·API 키)은 DB에 저장하며 `common/util/IntegrationCrypto`로 AES 암호화한다. 평문 저장·로그 출력·응답 반환 금지 (반환 시 마스킹)
- `bootRun`은 `local` 프로파일. 로컬 포트는 앱 8090 / DB 3307 / Redis 6380

## 10. 스케줄러·이벤트·비동기

- 스케줄러는 도메인 하위 또는 전용 클래스(`CourseSyncScheduler` 등)로 두고, 활성화 조건을 프로퍼티로 제어한다 (`monitoring.token.refresh.enabled` 패턴). 미설정 시 빈이 생성되지 않게 해 로컬 실행을 방해하지 않는다
- 접속 이력 등 부수 기록은 Spring Event + `AsyncConfig`의 비동기 실행으로 본 요청을 막지 않는다

## 검증

작업 후 컴파일을 확인한다.

```bash
cd back && ./gradlew compileJava --no-daemon
```

엔드포인트를 추가·변경했다면 `integration-testing` 스킬로 통합 테스트를 함께 갱신한다. 스키마가 바뀌었다면 `db-migration` 스킬로 마이그레이션 SQL을 남긴다 — `ddl-auto=update`가 로컬에서 통과시켜도 운영에는 반영되지 않는다.
