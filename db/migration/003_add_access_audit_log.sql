-- ============================================
-- 접속 이력 · 어드민 감사 로그 테이블 추가
-- ============================================
-- 목적: 유저/관리자 접속 이력, 관리자 작업 감사 로그 저장
-- 작성일: 2026-06-05
-- ============================================

-- ============================================
-- 1. 접속 이력 테이블
-- ============================================
CREATE TABLE IF NOT EXISTS tb_access_log (
    seq_access_log BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '접속이력 PK',
    actor_type     VARCHAR(10)  NOT NULL                   COMMENT '접속자 유형 (ADMIN/USER)',
    actor_id       BIGINT       NOT NULL                   COMMENT '접속자 PK (seq_admin 또는 seq_user)',
    actor_name     VARCHAR(30)  NOT NULL                   COMMENT '접속자 로그인 ID',
    ip_address     VARCHAR(45)  NOT NULL                   COMMENT '접속 IP (IPv6 지원)',
    result         VARCHAR(10)  NOT NULL                   COMMENT '결과 (SUCCESS/FAIL)',
    fail_reason    VARCHAR(100)                            COMMENT '실패 사유 (로그인 실패 시)',
    accessed_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '접속 일시'
);

CREATE INDEX idx_access_log_actor  ON tb_access_log(actor_type, actor_id, accessed_at);
CREATE INDEX idx_access_log_result ON tb_access_log(result, accessed_at);

-- ============================================
-- 2. 어드민 작업 감사 로그 테이블
-- ============================================
CREATE TABLE IF NOT EXISTS tb_audit_log (
    seq_audit_log BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '감사로그 PK',
    actor_id      BIGINT       NOT NULL                   COMMENT '작업자 PK (seq_admin)',
    actor_name    VARCHAR(30)  NOT NULL                   COMMENT '작업자 로그인 ID',
    action        VARCHAR(30)  NOT NULL                   COMMENT '작업 유형 (BOOK_CREATE 등)',
    target_type   VARCHAR(20)  NOT NULL                   COMMENT '작업 대상 유형 (BOOK/USER/ADMIN/COURSE 등)',
    target_id     VARCHAR(50)                             COMMENT '작업 대상 PK (문자열)',
    detail        VARCHAR(255)                            COMMENT '추가 정보 (변경 요약 등)',
    result        VARCHAR(10)  NOT NULL                   COMMENT '결과 (SUCCESS/FAIL)',
    fail_reason   VARCHAR(100)                            COMMENT '실패 사유',
    audited_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '작업 일시'
);

CREATE INDEX idx_audit_log_actor  ON tb_audit_log(actor_id, audited_at);
CREATE INDEX idx_audit_log_action ON tb_audit_log(action, target_type, audited_at);
