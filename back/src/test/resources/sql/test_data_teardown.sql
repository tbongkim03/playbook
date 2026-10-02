-- =====================================================
-- 테스트 데이터 정리
-- =====================================================

-- 대출 중 도서 상태 복원
UPDATE tb_book SET is_book_borrowed = 0 WHERE barcode_book LIKE 'TEST_%';

-- 테스트 계정 접속이력·감사로그 삭제
DELETE FROM tb_access_log
WHERE actor_id IN (SELECT seq_admin FROM tb_admin WHERE id_admin LIKE 'test_%')
   OR actor_id IN (SELECT seq_user FROM tb_user WHERE id_user LIKE 'test_%');

DELETE FROM tb_audit_log
WHERE actor_id IN (SELECT seq_admin FROM tb_admin WHERE id_admin LIKE 'test_%');

-- 허용 IP 규칙 (AllowedIpControllerTest / IpAllowlistFilterTest)
-- description 은 NULL 로 저장되는 케이스가 있어 IP 대역으로도 함께 지운다.
-- 테스트는 RFC 5737/3849 문서화 전용 대역만 쓰므로 실데이터와 겹치지 않는다.
DELETE FROM tb_allowed_ip
WHERE description LIKE 'TEST\_%'
   OR ip_value LIKE '192.0.2.%'
   OR ip_value LIKE '198.51.100.%'
   OR ip_value LIKE '203.0.113.%'
   OR ip_value LIKE '2001:db8:%';

-- FK 역순으로 삭제
DELETE FROM tb_history
WHERE seq_user IN (SELECT seq_user FROM tb_user WHERE id_user LIKE 'test_%')
   OR seq_admin IN (SELECT seq_admin FROM tb_admin WHERE id_admin LIKE 'test_%');

DELETE FROM tb_favor
WHERE seq_user IN (SELECT seq_user FROM tb_user WHERE id_user LIKE 'test_%');

DELETE FROM tb_user WHERE id_user LIKE 'test_%';
DELETE FROM tb_admin WHERE id_admin LIKE 'test_%';
DELETE FROM tb_book WHERE barcode_book LIKE 'TEST_%';
DELETE FROM tb_course WHERE name_course LIKE 'TEST_%';
