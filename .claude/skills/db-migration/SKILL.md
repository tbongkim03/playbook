---
name: db-migration
description: "Playbook DB 스키마를 변경할 때 반드시 사용하는 스킬. db/migration/NNN_*.sql 순번 마이그레이션 작성, ddl-auto=update가 처리하지 못하는 변경 식별, 롤백 SQL, 엔티티↔컬럼 매핑 검증, 테스트 픽스처 SQL 갱신을 다룬다. 테이블·컬럼을 추가·변경·삭제할 때, 엔티티 필드를 바꿀 때, 인덱스·제약을 걸 때, 초기·기준 데이터를 넣을 때, 운영 배포용 SQL이 필요할 때, 마이그레이션을 다시 작성하거나 보완할 때도 이 스킬을 사용할 것."
---

# Playbook DB 마이그레이션 규약

MariaDB 스키마 변경은 `db/migration/` 순번 SQL로 남긴다. `back/src/main/resources/application.properties`에 `spring.jpa.hibernate.ddl-auto=update`가 설정돼 있지만, **여기에 의존해 마이그레이션을 생략하면 운영 장애가 된다.** 이 프로젝트의 알려진 리스크 1순위가 "마이그레이션 SQL 미실행"이다.

## 1. ddl-auto=update의 한계 — 반드시 SQL이 필요한 변경

Hibernate `update`는 **추가만** 한다. 아래는 자동 반영되지 않거나 실패한다.

| 변경 | ddl-auto 동작 | 필요한 조치 |
|------|--------------|-----------|
| 컬럼 추가 (nullable) | 반영됨 | 그래도 SQL을 남긴다 (운영은 자동 반영을 신뢰하지 않음) |
| 컬럼 추가 (NOT NULL, 기존 행 있음) | **실패** | DEFAULT와 함께 추가하거나, nullable 추가 → 백필 → NOT NULL 전환 3단계 |
| 컬럼 삭제 | **무시됨** | `DROP COLUMN` SQL 필수 |
| 컬럼명 변경 | **새 컬럼 생성 + 기존 잔존** | `CHANGE COLUMN` + 데이터 이관 SQL |
| 타입 축소 (VARCHAR 100→50 등) | **무시됨** | `MODIFY COLUMN` + 초과 데이터 처리 |
| UNIQUE 제약 추가 | 반영 시도 후 중복 있으면 **실패** | 중복 정리 SQL 선행 |
| 인덱스 삭제 | **무시됨** | `DROP INDEX` SQL |
| 테이블 삭제 | **무시됨** | `DROP TABLE` SQL |

**판단 기준:** 엔티티에서 무언가를 지우거나 좁히거나 조인다면 반드시 SQL이 필요하다. 넓히거나 더한다면 SQL은 안전망이다. 어느 쪽이든 남긴다.

## 2. 파일 규약

`db/migration/`의 최대 번호 다음 3자리를 붙인다. 현재까지: `001_add_campus.sql`, `002_add_campus_columns.sql`, `003_add_access_audit_log.sql` → 다음은 `004_`.

번호를 건너뛰거나 중복하지 않는다. 번호가 곧 적용 순서다.

```sql
-- ============================================
-- {변경 제목}
-- ============================================
-- 목적: {왜 이 변경이 필요한가}
-- 작성일: {YYYY-MM-DD}
-- 관련: {기능명 / 엔티티 클래스}
-- ============================================

ALTER TABLE tb_book
    ADD COLUMN import_source VARCHAR(20) NULL COMMENT '도서 등록 경로 (MANUAL:수동, EXCEL:엑셀, ISBN:ISBN조회)';

CREATE INDEX idx_book_import_source ON tb_book (import_source);

-- 적용 확인
SELECT COUNT(*) FROM tb_book WHERE import_source IS NULL;

-- ============================================
-- 롤백
-- ============================================
-- DROP INDEX idx_book_import_source ON tb_book;
-- ALTER TABLE tb_book DROP COLUMN import_source;
```

규약:

- 헤더 주석 형식(구분선 + 목적 + 작성일)을 유지한다. 기존 파일이 모두 이 형태다
- **모든 컬럼에 `COMMENT '설명'`을 붙인다.** 코드 값이 있는 컬럼은 값의 의미까지 적는다
- 하단에 **롤백 SQL을 주석으로** 남긴다. 파괴적 변경일수록 필수
- 적용 확인용 `SELECT`를 넣어 실행자가 결과를 눈으로 볼 수 있게 한다
- 하나의 파일은 하나의 논리적 변경. 무관한 변경을 묶지 않는다

## 3. 네이밍 규약

| 대상 | 규칙 | 예시 |
|------|------|------|
| 테이블 | `tb_` + 단수 snake_case | `tb_book`, `tb_audit_log` |
| PK | `seq_{도메인}` INT AUTO_INCREMENT | `seq_campus`, `seq_favor` |
| FK 컬럼 | 참조 테이블의 PK명 그대로 | `seq_user`, `seq_book` |
| 일반 컬럼 | snake_case | `name_campus`, `location_campus` |
| 불리언 | `TINYINT(1)` 또는 `CHAR(1)` Y/N | `is_active`, `use_yn` |
| 인덱스 | `idx_{의미}` | `idx_active`, `idx_book_import_source` |

엔티티의 camelCase 필드는 Hibernate 기본 전략상 snake_case 컬럼과 매핑되지만, 이 프로젝트는 `@Column(name = "...")`으로 **명시**하는 스타일이다. SQL 컬럼명과 엔티티 `@Column` 값이 정확히 일치하는지 확인한다.

## 4. 감사 컬럼

`BaseAuditEntity`를 상속하는 엔티티의 테이블에는 아래 컬럼이 있어야 한다. 새 테이블을 만들 때 빠뜨리면 엔티티 저장이 실패한다.

```sql
use_yn           CHAR(1)      NOT NULL DEFAULT 'Y' COMMENT '사용 여부 (Y:사용, N:삭제)',
created_by_type  VARCHAR(20)  NULL COMMENT '생성 주체 유형 (ADMIN/USER/SYSTEM)',
created_at       DATETIME     NULL COMMENT '생성 일시',
created_by       BIGINT       NULL COMMENT '생성자 식별자',
updated_by_type  VARCHAR(20)  NULL COMMENT '수정 주체 유형',
updated_at       DATETIME     NULL COMMENT '수정 일시',
updated_by       BIGINT       NULL COMMENT '수정자 식별자'
```

삭제는 Soft Delete(`use_yn = 'N'`)이므로, 마이그레이션에서 데이터를 지울 때도 `DELETE`보다 `UPDATE ... SET use_yn = 'N'`을 우선 검토한다. 단, 개인정보 파기 요구나 테스트 데이터 정리는 실제 `DELETE`가 맞다.

## 5. 데이터 백필

NOT NULL 컬럼을 추가하거나 제약을 조일 때는 3단계로 나눈다. 한 파일 안에서 순서대로 배치하고, 각 단계 사이에 확인 쿼리를 넣는다.

```sql
-- 1단계: nullable로 추가
ALTER TABLE tb_book ADD COLUMN campus_scope VARCHAR(20) NULL COMMENT '...';

-- 2단계: 기존 행 백필
UPDATE tb_book SET campus_scope = 'ALL' WHERE campus_scope IS NULL;

-- 확인 (0이어야 다음 단계 진행 가능)
SELECT COUNT(*) AS remaining_null FROM tb_book WHERE campus_scope IS NULL;

-- 3단계: NOT NULL 전환
ALTER TABLE tb_book MODIFY COLUMN campus_scope VARCHAR(20) NOT NULL COMMENT '...';
```

백필 값의 근거를 주석으로 남긴다. "왜 기존 행을 `ALL`로 보는가"가 이후 데이터 해석의 전제가 된다.

## 6. 테스트 픽스처 동기화

스키마를 바꾸면 `back/src/test/resources/sql/`의 픽스처도 함께 확인한다.

- `test_data_setup.sql` — campus/sort/course/book 기본 + admin/user/favor/history 테스트 데이터
- `test_data_teardown.sql` — `test_`/`TEST_` 접두사 기반 삭제

**NOT NULL 컬럼을 추가하면 기존 INSERT 문이 전부 깨진다.** 픽스처를 갱신하지 않으면 모든 통합 테스트가 실패한다. 새 테이블을 만들었고 테스트가 그 테이블을 쓴다면 teardown에도 삭제 구문을 추가한다.

## 7. 관련 파일

| 경로 | 용도 |
|------|------|
| `db/migration/NNN_*.sql` | 순차 적용 마이그레이션 |
| `db/init/init.sql` | 컨테이너 최초 기동 시 초기화 |
| `db/data/sort_label.sql` | 분류 라벨 기준 데이터 |
| `db/conf/default.cnf` | MariaDB 설정 |
| `scripts/migrate-db.sh` | 마이그레이션 실행 스크립트 |
| `scripts/backup-tables.sh` / `restore-tables.sh` | 적용 전 백업 / 복구 |

## 8. `db/migration/`은 커밋 대상이다

마이그레이션 SQL은 **저장소에 커밋한다.** 순번 파일이 곧 스키마 변경 이력이므로, 저장소에 없으면 다른 환경·다른 작업자가 어떤 SQL이 적용되어야 하는지 알 방법이 없다. 이것이 "마이그레이션 미실행" 리스크의 원인이었다.

따라서:

- 작성한 `db/migration/NNN_*.sql`을 **작업 커밋에 함께 포함한다.** 스키마를 바꾼 코드와 그 SQL이 같은 커밋에 있으면 배포 시 누락되지 않는다
- 커밋 메시지에 마이그레이션 포함 사실을 드러낸다 (`feat: X8 도서 등록 이력 테이블 추가 (004 마이그레이션 포함)`)
- 순번이 곧 적용 순서이므로, 이미 커밋·적용된 파일은 **수정하지 말고 새 순번을 추가한다** (아래 9절)
- SQL에 실데이터·시크릿을 넣지 않는다. 커밋되는 파일이므로 계정 비밀번호·토큰이 들어가면 저장소에 유출된다. 기준 데이터만 넣고, 환경별 값은 배포 시 주입한다

`db/init/init.sql`, `db/data/sort_label.sql`도 같은 원칙으로 커밋 대상이다. 단 `db/.env.dev`, `db/.env.prod`는 계속 gitignore 대상이다.

## 9. 실행 정책

- **로컬 검증까지가 기본 범위다.** 로컬 DB는 포트 3307
- **운영 DB에 직접 실행하지 않는다.** 운영 적용은 사용자 승인 사항이며, `scripts/migrate-db.sh`를 통해 이뤄진다
- 파괴적 변경 전에는 `backup-tables.sh`로 대상 테이블을 백업하는 단계를 절차에 포함한다
- 이미 적용됐을 가능성이 있는 마이그레이션 파일은 **수정하지 말고 새 순번을 추가한다.** 기존 파일을 고치면 환경 간 스키마가 갈라진다

## 산출물

작업 후 다음을 보고한다:

1. 작성한 SQL 경로와 변경 요약
2. 엔티티↔컬럼 매핑 표 (`엔티티 필드 | 컬럼 | 타입 | nullable`)
3. **운영 배포 시 필수 실행 목록** — `ddl-auto=update`로 자동 반영되지 않는 항목. 이 목록이 `deploy-ops`의 배포 절차로 전달되어야 한다
4. 롤백 절차
5. 픽스처 갱신 여부
