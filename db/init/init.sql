-- ============================================
-- Playbook 초기 스키마 (신규 설치 전용)
-- ============================================
-- 이 파일은 db/Dockerfile 이 docker-entrypoint-initdb.d/ 로 복사한다.
-- MySQL 은 데이터 디렉터리가 비어 있을 때만 이 스크립트를 실행하므로,
-- 기존 DB(운영·개발)에는 절대 적용되지 않는다. 신규 볼륨 전용이다.
--
-- 2026-08-04: 전 테이블에 BaseAuditEntity 감사 컬럼 7종 추가 + tb_terms 신규 +
--             tb_user/tb_admin 의 created_at 을 DATE → DATETIME 으로 교정.
--             (엔티티는 LocalDateTime 이라 DATE 면 ddl-auto=validate 기동이 실패한다.)
--             use_yn 의 DEFAULT 는 반드시 'Y' 여야 한다 — 전 엔티티에
--             @Where(clause = "use_yn = 'Y'") 가 걸려 있어 'N'/NULL 이면 조회에서 사라진다.
-- ============================================

CREATE TABLE tb_campus (
    seq_campus       INT             NOT NULL AUTO_INCREMENT,
    name_campus      VARCHAR(50)     NOT NULL UNIQUE,
    location_campus  VARCHAR(100)    NULL,
    is_active        TINYINT(1)      NOT NULL DEFAULT 1,
    use_yn           CHAR(1)         NOT NULL DEFAULT 'Y' COMMENT '사용 여부 (Y:사용, N:삭제)',
    created_by_type  VARCHAR(20)     NULL COMMENT '생성 주체 유형 (ADMIN/USER/SYSTEM)',
    created_at       DATETIME        NULL COMMENT '생성 일시',
    created_by       BIGINT          NULL COMMENT '생성자 식별자',
    updated_by_type  VARCHAR(20)     NULL COMMENT '수정 주체 유형 (ADMIN/USER/SYSTEM)',
    updated_at       DATETIME        NULL COMMENT '수정 일시',
    updated_by       BIGINT          NULL COMMENT '수정자 식별자',
    PRIMARY KEY (seq_campus)
);

CREATE TABLE tb_book (
    seq_book        INT             NOT NULL AUTO_INCREMENT,
    seq_campus      INT             NOT NULL,
    seq_sort_second INT             NOT NULL,
    isbn_book       VARCHAR(20)     NOT NULL,
    title_book      VARCHAR(255)    NOT NULL,
    author_book     VARCHAR(20)     NOT NULL,
    publisher_book  VARCHAR(20)     NOT NULL,
    publish_date_book DATE          NOT NULL,
    img_url_book    VARCHAR(255)    NOT NULL,
    barcode_book    VARCHAR(30)     NULL,
    cnt_book        INT             NULL,
    print_check_book TINYINT(1)     NOT NULL DEFAULT 0,
    is_book_borrowed TINYINT(1)     NOT NULL DEFAULT 0,
    use_yn          CHAR(1)         NOT NULL DEFAULT 'Y' COMMENT '사용 여부 (Y:사용, N:삭제)',
    created_by_type VARCHAR(20)     NULL COMMENT '생성 주체 유형 (ADMIN/USER/SYSTEM)',
    created_at      DATETIME        NULL COMMENT '생성 일시',
    created_by      BIGINT          NULL COMMENT '생성자 식별자',
    updated_by_type VARCHAR(20)     NULL COMMENT '수정 주체 유형 (ADMIN/USER/SYSTEM)',
    updated_at      DATETIME        NULL COMMENT '수정 일시',
    updated_by      BIGINT          NULL COMMENT '수정자 식별자',
    PRIMARY KEY (seq_book)
);

CREATE TABLE tb_user (
    seq_user        INT             NOT NULL AUTO_INCREMENT,
    seq_course      INT             NULL,
    id_user         VARCHAR(30)     NOT NULL UNIQUE,
    pw_user         VARCHAR(255)    NOT NULL,
    name_user       VARCHAR(20)     NOT NULL,
    dc_user         VARCHAR(30)     NOT NULL,
    agree_terms_user TINYINT(1)     NOT NULL DEFAULT 0,
    agree_info_user  TINYINT(1)     NOT NULL DEFAULT 0,
    agree_discord_alarm_user TINYINT(1) NOT NULL DEFAULT 0,
    status_user     ENUM('stop', 'available', 'overdue') COMMENT 'stop:정지or탈퇴, available:대여, overdue:연체' NOT NULL,
    use_yn          CHAR(1)         NOT NULL DEFAULT 'Y' COMMENT '사용 여부 (Y:사용, N:삭제)',
    created_by_type VARCHAR(20)     NULL COMMENT '생성 주체 유형 (ADMIN/USER/SYSTEM)',
    created_at      DATETIME        NOT NULL COMMENT '생성 일시',
    created_by      BIGINT          NULL COMMENT '생성자 식별자',
    updated_by_type VARCHAR(20)     NULL COMMENT '수정 주체 유형 (ADMIN/USER/SYSTEM)',
    updated_at      DATETIME        NULL COMMENT '수정 일시',
    updated_by      BIGINT          NULL COMMENT '수정자 식별자',
    PRIMARY KEY (seq_user)
);

CREATE TABLE tb_admin (
    seq_admin         INT             NOT NULL AUTO_INCREMENT,
    seq_campus        INT             NULL,
    id_admin          VARCHAR(30)     NOT NULL UNIQUE,
    pw_admin          VARCHAR(255)    NOT NULL,
    name_admin        VARCHAR(20)     NOT NULL,
    dc_admin          VARCHAR(30)     NOT NULL,
    agree_terms_admin TINYINT(1)      NOT NULL DEFAULT 0,
    agree_info_admin  TINYINT(1)      NOT NULL DEFAULT 0,
    agree_discord_alarm_admin TINYINT(1) NOT NULL DEFAULT 0,
    status_admin      ENUM('stop', 'available', 'overdue') COMMENT 'stop:정지, available:대여, overdue:연체' NOT NULL,
    use_yn            CHAR(1)         NOT NULL DEFAULT 'Y' COMMENT '사용 여부 (Y:사용, N:삭제)',
    created_by_type   VARCHAR(20)     NULL COMMENT '생성 주체 유형 (ADMIN/USER/SYSTEM)',
    created_at        DATETIME        NOT NULL COMMENT '생성 일시',
    created_by        BIGINT          NULL COMMENT '생성자 식별자',
    updated_by_type   VARCHAR(20)     NULL COMMENT '수정 주체 유형 (ADMIN/USER/SYSTEM)',
    updated_at        DATETIME        NULL COMMENT '수정 일시',
    updated_by        BIGINT          NULL COMMENT '수정자 식별자',
    PRIMARY KEY (seq_admin)
);

CREATE TABLE tb_history (
    seq_history     INT             NOT NULL AUTO_INCREMENT,
    seq_campus      INT             NOT NULL,
    seq_admin       INT             NULL,
    seq_user        INT             NULL,
    seq_course      INT             NULL,
    seq_book        INT             NULL,
    book_dt         DATE            NOT NULL,
    return_dt       DATE            NULL,
    use_yn          CHAR(1)         NOT NULL DEFAULT 'Y' COMMENT '사용 여부 (Y:사용, N:삭제)',
    created_by_type VARCHAR(20)     NULL COMMENT '생성 주체 유형 (ADMIN/USER/SYSTEM)',
    created_at      DATETIME        NULL COMMENT '생성 일시',
    created_by      BIGINT          NULL COMMENT '생성자 식별자',
    updated_by_type VARCHAR(20)     NULL COMMENT '수정 주체 유형 (ADMIN/USER/SYSTEM)',
    updated_at      DATETIME        NULL COMMENT '수정 일시',
    updated_by      BIGINT          NULL COMMENT '수정자 식별자',
    PRIMARY KEY (seq_history)
);

CREATE TABLE tb_course (
    seq_course      INT             NOT NULL AUTO_INCREMENT,
    seq_campus      INT             NOT NULL,
    name_course     VARCHAR(30)     NOT NULL UNIQUE,
    start_dt_course DATE            NOT NULL,
    finish_dt_course DATE           NOT NULL,
    use_yn          CHAR(1)         NOT NULL DEFAULT 'Y' COMMENT '사용 여부 (Y:사용, N:삭제)',
    created_by_type VARCHAR(20)     NULL COMMENT '생성 주체 유형 (ADMIN/USER/SYSTEM)',
    created_at      DATETIME        NULL COMMENT '생성 일시',
    created_by      BIGINT          NULL COMMENT '생성자 식별자',
    updated_by_type VARCHAR(20)     NULL COMMENT '수정 주체 유형 (ADMIN/USER/SYSTEM)',
    updated_at      DATETIME        NULL COMMENT '수정 일시',
    updated_by      BIGINT          NULL COMMENT '수정자 식별자',
    PRIMARY KEY (seq_course)
);

CREATE TABLE tb_sort_first (
    seq_sort_first  INT             NOT NULL,
    kor_sort_first  VARCHAR(255)    NOT NULL,
    name_sort_first VARCHAR(255)    NOT NULL,
    use_yn          CHAR(1)         NOT NULL DEFAULT 'Y' COMMENT '사용 여부 (Y:사용, N:삭제)',
    created_by_type VARCHAR(20)     NULL COMMENT '생성 주체 유형 (ADMIN/USER/SYSTEM)',
    created_at      DATETIME        NULL COMMENT '생성 일시',
    created_by      BIGINT          NULL COMMENT '생성자 식별자',
    updated_by_type VARCHAR(20)     NULL COMMENT '수정 주체 유형 (ADMIN/USER/SYSTEM)',
    updated_at      DATETIME        NULL COMMENT '수정 일시',
    updated_by      BIGINT          NULL COMMENT '수정자 식별자',
    PRIMARY KEY (seq_sort_first)
);

CREATE TABLE tb_sort_second (
    seq_sort_second  INT             NOT NULL,
    seq_sort_first   INT             NOT NULL,
    kor_sort_second  VARCHAR(255)    NOT NULL,
    name_sort_second VARCHAR(255)    NOT NULL,
    use_yn           CHAR(1)         NOT NULL DEFAULT 'Y' COMMENT '사용 여부 (Y:사용, N:삭제)',
    created_by_type  VARCHAR(20)     NULL COMMENT '생성 주체 유형 (ADMIN/USER/SYSTEM)',
    created_at       DATETIME        NULL COMMENT '생성 일시',
    created_by       BIGINT          NULL COMMENT '생성자 식별자',
    updated_by_type  VARCHAR(20)     NULL COMMENT '수정 주체 유형 (ADMIN/USER/SYSTEM)',
    updated_at       DATETIME        NULL COMMENT '수정 일시',
    updated_by       BIGINT          NULL COMMENT '수정자 식별자',
    PRIMARY KEY (seq_sort_second)
);

CREATE TABLE tb_favor (
    seq_favor    INT               NOT NULL AUTO_INCREMENT,
    seq_user     INT               NOT NULL,
    seq_book     INT               NOT NULL,
    use_yn          CHAR(1)        NOT NULL DEFAULT 'Y' COMMENT '사용 여부 (Y:사용, N:삭제)',
    created_by_type VARCHAR(20)    NULL COMMENT '생성 주체 유형 (ADMIN/USER/SYSTEM)',
    created_at      DATETIME       NULL COMMENT '생성 일시',
    created_by      BIGINT         NULL COMMENT '생성자 식별자',
    updated_by_type VARCHAR(20)    NULL COMMENT '수정 주체 유형 (ADMIN/USER/SYSTEM)',
    updated_at      DATETIME       NULL COMMENT '수정 일시',
    updated_by      BIGINT         NULL COMMENT '수정자 식별자',
    PRIMARY KEY (seq_favor)
);

-- 약관 (엔티티 Terms). 본문은 기동 시 TermsDataInitializer 가 시드한다.
CREATE TABLE tb_terms (
    seq_terms       INT             NOT NULL AUTO_INCREMENT,
    terms_type      VARCHAR(30)     NOT NULL UNIQUE COMMENT '약관 유형 (SERVICE/PRIVACY/DISCORD 등)',
    content         LONGTEXT        NOT NULL COMMENT '약관 본문',
    use_yn          CHAR(1)         NOT NULL DEFAULT 'Y' COMMENT '사용 여부 (Y:사용, N:삭제)',
    created_by_type VARCHAR(20)     NULL COMMENT '생성 주체 유형 (ADMIN/USER/SYSTEM)',
    created_at      DATETIME        NULL COMMENT '생성 일시',
    created_by      BIGINT          NULL COMMENT '생성자 식별자',
    updated_by_type VARCHAR(20)     NULL COMMENT '수정 주체 유형 (ADMIN/USER/SYSTEM)',
    updated_at      DATETIME        NULL COMMENT '수정 일시',
    updated_by      BIGINT          NULL COMMENT '수정자 식별자',
    PRIMARY KEY (seq_terms)
);

-- 외래키
ALTER TABLE tb_book ADD CONSTRAINT FK_tb_campus_TO_tb_book
FOREIGN KEY (seq_campus)
REFERENCES tb_campus (seq_campus);

ALTER TABLE tb_book ADD CONSTRAINT FK_tb_sort_second_TO_tb_book
FOREIGN KEY (seq_sort_second)
REFERENCES tb_sort_second (seq_sort_second);

ALTER TABLE tb_course ADD CONSTRAINT FK_tb_campus_TO_tb_course
FOREIGN KEY (seq_campus)
REFERENCES tb_campus (seq_campus);

ALTER TABLE tb_user ADD CONSTRAINT FK_tb_course_TO_tb_user
FOREIGN KEY (seq_course)
REFERENCES tb_course (seq_course) ON DELETE SET NULL;

ALTER TABLE tb_admin ADD CONSTRAINT FK_tb_campus_TO_tb_admin
FOREIGN KEY (seq_campus)
REFERENCES tb_campus (seq_campus);

ALTER TABLE tb_history ADD CONSTRAINT FK_tb_campus_TO_tb_history
FOREIGN KEY (seq_campus)
REFERENCES tb_campus (seq_campus);

ALTER TABLE tb_history ADD CONSTRAINT FK_tb_admin_TO_tb_history
FOREIGN KEY (seq_admin)
REFERENCES tb_admin (seq_admin) ON DELETE SET NULL;

ALTER TABLE tb_history ADD CONSTRAINT FK_tb_user_TO_tb_history
FOREIGN KEY (seq_user)
REFERENCES tb_user (seq_user) ON DELETE SET NULL;

ALTER TABLE tb_history ADD CONSTRAINT FK_tb_book_TO_tb_history
FOREIGN KEY (seq_book)
REFERENCES tb_book (seq_book) ON DELETE SET NULL;

ALTER TABLE tb_history ADD CONSTRAINT FK_tb_course_TO_tb_history
FOREIGN KEY (seq_course)
REFERENCES tb_course (seq_course) ON DELETE SET NULL;

ALTER TABLE tb_sort_second ADD CONSTRAINT FK_tb_sort_first_TO_tb_sort_second
FOREIGN KEY (seq_sort_first)
REFERENCES tb_sort_first(seq_sort_first);

ALTER TABLE tb_favor ADD CONSTRAINT FK_tb_user_TO_tb_favor
FOREIGN KEY (seq_user)
REFERENCES tb_user (seq_user) ON DELETE CASCADE;

ALTER TABLE tb_favor ADD CONSTRAINT FK_tb_book_TO_tb_favor
FOREIGN KEY (seq_book)
REFERENCES tb_book (seq_book) ON DELETE CASCADE;

-- 캠퍼스별 조회 성능 인덱스
-- 002_add_campus_columns.sql 96~99행과 정의가 동일하다. init.sql 이 이미 seq_campus 를
-- 포함하도록 갱신되면서 002 는 신규 설치에서 실행되지 않으므로(Duplicate column name),
-- 인덱스만 여기로 흡수한다. 기존 DB 는 002 로 이미 생성돼 있어 영향 없다.
CREATE INDEX idx_course_campus ON tb_course(seq_campus);
CREATE INDEX idx_book_campus ON tb_book(seq_campus);
CREATE INDEX idx_history_campus ON tb_history(seq_campus);
CREATE INDEX idx_admin_campus ON tb_admin(seq_campus);
