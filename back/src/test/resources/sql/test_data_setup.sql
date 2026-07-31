-- =====================================================
-- 테스트 데이터 셋업
-- 비밀번호: Test1234! (BCrypt $2b$10$)
-- =====================================================

-- 1. 기본 캠퍼스 데이터 (없으면 삽입)
INSERT IGNORE INTO tb_campus (seq_campus, name_campus, location_campus, is_active, use_yn)
VALUES (1, '서초', '서울시 서초구', 1, 'Y'),
       (2, 'G밸리', '서울시 구로구', 1, 'Y'),
       (3, '동작', '서울시 동작구', 1, 'Y');

-- 2. 분류 데이터 (없으면 삽입)
INSERT IGNORE INTO tb_sort_first (seq_sort_first, name_sort_first, kor_sort_first, use_yn)
VALUES (1, 'A', '일반', 'Y'),
       (2, 'B', '컴퓨터일반', 'Y'),
       (3, 'C', '웹 / 앱', 'Y');

INSERT IGNORE INTO tb_sort_second (seq_sort_second, seq_sort_first, name_sort_second, kor_sort_second, use_yn)
VALUES (2, 2, '00', '일반', 'Y'),
       (4, 2, '02', '자바', 'Y'),
       (3, 2, '01', '파이썬', 'Y'),
       (12, 3, '01', '프론트엔드', 'Y');

-- 3. 테스트용 코스 (TEST_ 접두사로 teardown 대상 식별)
-- 날짜는 CURDATE() 상대 표현 — 리터럴을 쓰면 시간이 지나며 "활성 → 종료"로 의미가 뒤집힌다.
-- 세 코스 모두 **활성**(finish_dt_course 가 미래)이 전제다.
--   · HistoryServiceImpl.checkCourseFinished() 가 finishDtCourse.isBefore(now) 로 판정하고,
--     과정이 종료된 유저는 연체 예외 대신 stop 처리되어 H6 의 "연체" 단정이 깨진다.
--   · 종료까지 30일 이상 남기므로 CourseEndReturnReminderScheduler(D-7/3/1)에도 걸리지 않는다.
INSERT IGNORE INTO tb_course (seq_course, seq_campus, name_course, start_dt_course, finish_dt_course, use_yn)
VALUES (9001, 1, 'TEST_Java풀스택', DATE_SUB(CURDATE(), INTERVAL 180 DAY), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'Y'),
       (9002, 1, 'TEST_Python데이터분석', DATE_SUB(CURDATE(), INTERVAL 150 DAY), DATE_ADD(CURDATE(), INTERVAL 60 DAY), 'Y'),
       (9003, 2, 'TEST_Java백엔드', DATE_SUB(CURDATE(), INTERVAL 180 DAY), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'Y');

-- 4. 테스트용 도서 (TEST_BC 바코드로 teardown 대상 식별)
INSERT IGNORE INTO tb_book (seq_book, seq_campus, seq_sort_second, isbn_book, title_book, author_book,
    publisher_book, publish_date_book, img_url_book, barcode_book, cnt_book, print_check_book, is_book_borrowed, use_yn)
VALUES
    (9001, 1, 4, '9791162241820', '클린코드', '로버트마틴', '인사이트', '2013-12-24', 'https://test.img/1.jpg', 'TEST_BC001', 1, 0, 0, 'Y'),
    (9002, 1, 4, '9788994492032', '자바의정석', '남궁성', 'EASYSPUB', '2016-01-01', 'https://test.img/2.jpg', 'TEST_BC002', 1, 0, 0, 'Y'),
    (9003, 1, 3, '9791162241509', '파이썬데이터분석', '홍길동', '한빛미디어', '2020-01-01', 'https://test.img/3.jpg', 'TEST_BC003', 1, 0, 0, 'Y'),
    (9004, 2, 4, '9791162243060', '스프링부트실전', '김부산', '위키북스', '2021-01-01', 'https://test.img/4.jpg', 'TEST_BC004', 1, 0, 0, 'Y');

-- 5. 테스트 관리자 (id_admin LIKE 'test_%')
INSERT IGNORE INTO tb_admin (seq_campus, id_admin, pw_admin, name_admin, dc_admin,
    agree_terms_admin, agree_info_admin, agree_discord_alarm_admin, status_admin, use_yn)
VALUES
    (1, 'test_admin01', '$2a$10$Lma7juxYdZ/RTKontE5bcO/cHeO45GeHwAHEH0jMB4XSq6fGb3CRC', '서울관리자', 'test_seoul_admin', 1, 1, 0, 'available', 'Y'),
    (2, 'test_admin02', '$2a$10$Lma7juxYdZ/RTKontE5bcO/cHeO45GeHwAHEH0jMB4XSq6fGb3CRC', '부산관리자', 'test_busan_admin', 1, 1, 0, 'available', 'Y'),
    (1, 'test_admin_stop', '$2a$10$Lma7juxYdZ/RTKontE5bcO/cHeO45GeHwAHEH0jMB4XSq6fGb3CRC', '정지관리자', 'test_stop_admin', 1, 1, 0, 'stop', 'Y');

-- 6. 테스트 유저 (id_user LIKE 'test_%')
INSERT IGNORE INTO tb_user (seq_course, id_user, pw_user, name_user, dc_user,
    agree_terms_user, agree_info_user, agree_discord_alarm_user, status_user, use_yn)
VALUES
    (9001, 'test_user01', '$2a$10$Lma7juxYdZ/RTKontE5bcO/cHeO45GeHwAHEH0jMB4XSq6fGb3CRC', '김철수', 'test_chulsoo', 1, 1, 0, 'available', 'Y'),
    (9001, 'test_user02', '$2a$10$Lma7juxYdZ/RTKontE5bcO/cHeO45GeHwAHEH0jMB4XSq6fGb3CRC', '이영희', 'test_younghee', 1, 1, 0, 'available', 'Y'),
    (9002, 'test_user03', '$2a$10$Lma7juxYdZ/RTKontE5bcO/cHeO45GeHwAHEH0jMB4XSq6fGb3CRC', '박민수', 'test_minsoo', 1, 1, 0, 'available', 'Y'),
    (9003, 'test_user04', '$2a$10$Lma7juxYdZ/RTKontE5bcO/cHeO45GeHwAHEH0jMB4XSq6fGb3CRC', '정지영', 'test_jiyoung', 1, 1, 0, 'overdue', 'Y');

-- 7. 즐겨찾기
INSERT IGNORE INTO tb_favor (seq_user, seq_book, use_yn)
SELECT u.seq_user, 9001, 'Y' FROM tb_user u WHERE u.id_user = 'test_user01'
UNION ALL
SELECT u.seq_user, 9002, 'Y' FROM tb_user u WHERE u.id_user = 'test_user01'
UNION ALL
SELECT u.seq_user, 9001, 'Y' FROM tb_user u WHERE u.id_user = 'test_user02'
UNION ALL
SELECT u.seq_user, 9003, 'Y' FROM tb_user u WHERE u.id_user = 'test_user03';

-- 8. 대출 이력
-- 날짜는 CURDATE() 상대 표현. 연체 판정은 HistoryServiceImpl.isOverdue():
--   return_dt IS NULL AND book_dt < (오늘 - 7일)  →  연체
-- 각 행의 의도는 아래 주석대로 시간이 지나도 고정된다.
INSERT IGNORE INTO tb_history (seq_campus, seq_user, seq_course, seq_book, book_dt, return_dt, use_yn)
-- test_user01 / 9001: 반납 완료 (H9 삭제 대상 = return_dt IS NOT NULL 인 행)
SELECT 1, u.seq_user, 9001, 9001, DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_SUB(CURDATE(), INTERVAL 25 DAY), 'Y' FROM tb_user u WHERE u.id_user = 'test_user01'
UNION ALL
-- test_user01 / 9002: 대출 중, 미연체 (status_user='available' 및 H7 주석 "대출중"과 일치)
SELECT 1, u.seq_user, 9001, 9002, DATE_SUB(CURDATE(), INTERVAL 3 DAY), NULL, 'Y' FROM tb_user u WHERE u.id_user = 'test_user01'
UNION ALL
-- test_user02 / 9001: 반납 완료 → 활성 대출 없음 (U12 본인 삭제의 전제)
SELECT 1, u.seq_user, 9001, 9001, DATE_SUB(CURDATE(), INTERVAL 28 DAY), DATE_SUB(CURDATE(), INTERVAL 21 DAY), 'Y' FROM tb_user u WHERE u.id_user = 'test_user02'
UNION ALL
-- test_user03 / 9003: 대출 중, 미연체 (status_user='available'과 일치)
SELECT 1, u.seq_user, 9002, 9003, DATE_SUB(CURDATE(), INTERVAL 3 DAY), NULL, 'Y' FROM tb_user u WHERE u.id_user = 'test_user03'
UNION ALL
-- test_user04 / 9004: **연체 대출** — H6(반납 시 "연체" 메시지)의 전제.
-- 58일 전 대출 → 반납예정일(대출+7일) 기준 연체 51일 (H6 주석의 "연체 51일"과 일치)
SELECT 2, u.seq_user, 9003, 9004, DATE_SUB(CURDATE(), INTERVAL 58 DAY), NULL, 'Y' FROM tb_user u WHERE u.id_user = 'test_user04';

-- 9. 대출 중인 도서 상태 반영
UPDATE tb_book SET is_book_borrowed = 1 WHERE seq_book IN (9002, 9003, 9004);
