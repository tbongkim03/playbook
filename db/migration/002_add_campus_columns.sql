-- ============================================
-- 기존 테이블에 캠퍼스 컬럼 추가 및 데이터 마이그레이션
-- ============================================
-- 목적: Course, Book, Admin, History 테이블에 seq_campus 추가
-- 작성일: 2026-02-11
-- ============================================

-- ============================================
-- 1. 컬럼 추가 (nullable로 시작)
-- ============================================

-- Course 테이블에 캠퍼스 추가
ALTER TABLE tb_course ADD COLUMN seq_campus INT NULL COMMENT '소속 캠퍼스 ID' AFTER seq_course;
ALTER TABLE tb_course ADD CONSTRAINT FK_tb_campus_TO_tb_course
    FOREIGN KEY (seq_campus) REFERENCES tb_campus(seq_campus);

-- Book 테이블에 캠퍼스 추가
ALTER TABLE tb_book ADD COLUMN seq_campus INT NULL COMMENT '소속 캠퍼스 ID' AFTER seq_book;
ALTER TABLE tb_book ADD CONSTRAINT FK_tb_campus_TO_tb_book
    FOREIGN KEY (seq_campus) REFERENCES tb_campus(seq_campus);

-- Admin 테이블에 캠퍼스 추가 (null 허용 - 전체 관리자 지원)
ALTER TABLE tb_admin ADD COLUMN seq_campus INT NULL COMMENT '소속 캠퍼스 ID (NULL=전체 관리자)' AFTER seq_admin;
ALTER TABLE tb_admin ADD CONSTRAINT FK_tb_campus_TO_tb_admin
    FOREIGN KEY (seq_campus) REFERENCES tb_campus(seq_campus);

-- History 테이블에 캠퍼스 추가 (성능 최적화를 위한 비정규화)
ALTER TABLE tb_history ADD COLUMN seq_campus INT NULL COMMENT '대여 발생 캠퍼스 ID' AFTER seq_history;
ALTER TABLE tb_history ADD CONSTRAINT FK_tb_campus_TO_tb_history
    FOREIGN KEY (seq_campus) REFERENCES tb_campus(seq_campus);

-- ============================================
-- 2. 데이터 마이그레이션
-- ============================================

-- ⚠️ 중요: Course 데이터는 자동 분류 불가능
-- 현재 진행 중인 과정만 서초로 설정하고, 나머지는 수동 분류 필요

-- 서초 캠퍼스 (현재 진행 중인 과정들)
UPDATE tb_course SET seq_campus = 1
WHERE seq_course IN (
    17,  -- 한화시스템 BEYOND SW 캠프 21기
    20,  -- 한화시스템 BEYOND SW 캠프 22기
    16,  -- SK네트웍스 Family AI 캠프 20기
    24   -- SK네트웍스 Family AI 캠프 25기
);

-- 나머지 과정들은 추후 캠퍼스 배정 후 아래 SQL 실행:
-- G밸리 캠퍼스
-- UPDATE tb_course SET seq_campus = 2 WHERE seq_course IN (?, ?, ?);

-- 동작 캠퍼스
-- UPDATE tb_course SET seq_campus = 3 WHERE seq_course IN (?, ?, ?);

-- 미분류 확인
SELECT seq_course, name_course, start_dt_course, finish_dt_course, seq_campus
FROM tb_course
ORDER BY seq_campus IS NULL DESC, seq_course;

-- 기존 Book은 서초 캠퍼스로 설정 (안전한 기본값)
UPDATE tb_book SET seq_campus = 1 WHERE seq_campus IS NULL;

-- 기존 Admin은 서초 캠퍼스로 설정
-- (전체 관리자로 유지하려면 이 줄을 주석 처리)
UPDATE tb_admin SET seq_campus = 1 WHERE seq_campus IS NULL;

-- ⚠️ 아래 History 업데이트는 Course 수동 설정 완료 후 실행
-- History는 Course를 통해 캠퍼스 설정
UPDATE tb_history h
INNER JOIN tb_course c ON h.seq_course = c.seq_course
SET h.seq_campus = c.seq_campus
WHERE h.seq_campus IS NULL AND h.seq_course IS NOT NULL;

-- Course가 없는 History (Admin 대여)는 Admin의 캠퍼스로
UPDATE tb_history h
INNER JOIN tb_admin a ON h.seq_admin = a.seq_admin
SET h.seq_campus = a.seq_campus
WHERE h.seq_campus IS NULL AND a.seq_campus IS NOT NULL;

-- ============================================
-- 3. NOT NULL 제약 조건 추가
-- ============================================

-- ⚠️ 모든 Course에 seq_campus가 설정된 것을 확인한 후 실행
-- Course는 수동 분류 완료 후 실행
-- ALTER TABLE tb_course MODIFY COLUMN seq_campus INT NOT NULL;

-- Book과 History는 자동 설정되므로 바로 NOT NULL 적용
ALTER TABLE tb_book MODIFY COLUMN seq_campus INT NOT NULL;
ALTER TABLE tb_history MODIFY COLUMN seq_campus INT NOT NULL;

-- ============================================
-- 4. 성능 최적화를 위한 인덱스 추가
-- ============================================

CREATE INDEX idx_course_campus ON tb_course(seq_campus);
CREATE INDEX idx_book_campus ON tb_book(seq_campus);
CREATE INDEX idx_history_campus ON tb_history(seq_campus);
CREATE INDEX idx_admin_campus ON tb_admin(seq_campus);

-- ============================================
-- 5. 데이터 검증
-- ============================================

-- 캠퍼스 미할당 확인
SELECT 'course' as table_name, COUNT(*) as null_count
FROM tb_course WHERE seq_campus IS NULL
UNION ALL
SELECT 'book', COUNT(*) FROM tb_book WHERE seq_campus IS NULL
UNION ALL
SELECT 'history', COUNT(*) FROM tb_history WHERE seq_campus IS NULL;

-- 캠퍼스별 데이터 분포 확인
SELECT c.name_campus,
       COUNT(DISTINCT co.seq_course) as course_count,
       COUNT(DISTINCT b.seq_book) as book_count,
       COUNT(DISTINCT h.seq_history) as history_count
FROM tb_campus c
LEFT JOIN tb_course co ON c.seq_campus = co.seq_campus
LEFT JOIN tb_book b ON c.seq_campus = b.seq_campus
LEFT JOIN tb_history h ON c.seq_campus = h.seq_campus
GROUP BY c.seq_campus, c.name_campus;
