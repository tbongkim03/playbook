-- ============================================
-- Campus 테이블 생성 및 초기 데이터 삽입
-- ============================================
-- 목적: 멀티캠퍼스 지원을 위한 캠퍼스 테이블 생성
-- 작성일: 2026-02-11
-- ============================================

-- Campus 테이블 생성
CREATE TABLE tb_campus (
    seq_campus       INT             NOT NULL AUTO_INCREMENT,
    name_campus      VARCHAR(50)     NOT NULL UNIQUE COMMENT '캠퍼스 이름 (서초, G밸리, 동작)',
    location_campus  VARCHAR(100)    NULL COMMENT '캠퍼스 위치 (서울시 서초구 등)',
    is_active        TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '활성 상태 (1:활성, 0:비활성)',
    PRIMARY KEY (seq_campus),
    INDEX idx_active (is_active)
) COMMENT='캠퍼스 정보 테이블';

-- 기본 캠퍼스 데이터 삽입
INSERT INTO tb_campus (seq_campus, name_campus, location_campus, is_active) VALUES
(1, '서초', '서울시 서초구', 1),
(2, 'G밸리', '서울시 구로구', 1),
(3, '동작', '서울시 동작구', 1);

-- 삽입 확인
SELECT * FROM tb_campus;
