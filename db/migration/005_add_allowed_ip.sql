-- ============================================
-- 서비스 접속 허용 IP 목록 테이블 추가
-- ============================================
-- 목적: 캠퍼스 내부망 운영 시 허용된 IP·대역에서만 서비스(프론트 + /api 전체)에
--       접속할 수 있도록, 허용 규칙을 DB에 저장하고 관리자탭에서 CRUD 한다.
--       IpAllowlistFilter 가 이 테이블의 활성 규칙을 메모리에 캐싱해 차단 판정에 쓴다.
-- 작성일: 2026-08-04
-- 관련: AllowedIp (playbook.encore.back.allowip.entity), IpAllowlistFilter
-- ============================================
-- 주의: 이 마이그레이션은 신규 테이블 1개만 생성한다.
--       기존 테이블의 스키마·데이터를 일절 건드리지 않는다 (DROP/TRUNCATE/DELETE 없음).
-- ============================================

-- ============================================
-- 1. 허용 IP 목록
-- ============================================
-- ip_type   : ip_value 표기로부터 서버가 판정해 저장한다 (프리픽스 '/' 유무).
-- seq_campus: 관리 편의용 라벨이다. 차단 판정은 활성 규칙 전체를 OR 로 본다
--             (필터가 인터셉터보다 앞이라 요청 시점에 세션·캠퍼스를 알 수 없다).
-- is_system : 설치 마법사·부트스트랩이 심은 규칙. 삭제 불가, 비활성만 허용한다.
CREATE TABLE IF NOT EXISTS tb_allowed_ip (
    seq_allowed_ip  INT          NOT NULL AUTO_INCREMENT     COMMENT '허용IP PK',
    ip_value        VARCHAR(64)  NOT NULL                    COMMENT '허용 IP 또는 CIDR (예: 192.168.0.15, 192.168.0.0/24, IPv6 가능)',
    ip_type         VARCHAR(10)  NOT NULL                    COMMENT 'IP 표기 유형 (SINGLE:단일 IP, CIDR:대역)',
    seq_campus      INT                                      COMMENT '캠퍼스 PK (tb_campus, NULL=전역 규칙·모든 캠퍼스 적용)',
    description     VARCHAR(200)                             COMMENT '설명 (예: 서초 라운지 LAN 대역)',
    is_active       TINYINT(1)   NOT NULL DEFAULT 1          COMMENT '활성 여부 (1:차단 판정에 사용, 0:규칙 유지하되 판정 제외)',
    is_system       TINYINT(1)   NOT NULL DEFAULT 0          COMMENT '설치 마법사 등록 여부 (1:삭제 불가·비활성만 가능, 0:일반 규칙)',
    use_yn          CHAR(1)      NOT NULL DEFAULT 'Y'        COMMENT '사용 여부 (Y:사용, N:삭제) — Soft Delete',
    created_by_type VARCHAR(20)                              COMMENT '생성 주체 유형 (ADMIN/USER/SYSTEM)',
    created_at      DATETIME                                 COMMENT '생성 일시',
    created_by      BIGINT                                   COMMENT '생성자 식별자',
    updated_by_type VARCHAR(20)                              COMMENT '수정 주체 유형 (ADMIN/USER/SYSTEM)',
    updated_at      DATETIME                                 COMMENT '수정 일시',
    updated_by      BIGINT                                   COMMENT '수정자 식별자',
    PRIMARY KEY (seq_allowed_ip),
    UNIQUE KEY uk_allowed_ip_value_campus (ip_value, seq_campus),
    KEY idx_allowed_ip_active (is_active, use_yn)
) COMMENT '서비스 접속 허용 IP 목록';

ALTER TABLE tb_allowed_ip ADD CONSTRAINT FK_tb_campus_TO_tb_allowed_ip
    FOREIGN KEY (seq_campus) REFERENCES tb_campus(seq_campus);

-- ============================================
-- 초기 데이터
-- ============================================
-- INSERT 하지 않는다. 의도된 결정이다.
--
-- 1) 활성 규칙이 0건이면 IpAllowlistFilter 는 모든 요청을 통과시키고 WARN 로그만 남긴다
--    (계약서 "통과 순서" 4번). 따라서 이 마이그레이션을 적용한 직후에도 잠김이 발생하지 않는다.
--    반대로 여기서 임의의 대역을 넣으면 그 순간부터 "그 대역 외 전면 차단"이 되어,
--    값이 틀렸을 때 관리자가 화면으로 복구할 수 없는 잠김 사고가 된다.
-- 2) 실제 허용 대역은 설치 마법사 6단계가 설치 PC IP·LAN 대역을 감지해
--    is_system = 1 로 등록한다. 캠퍼스마다 대역이 다르므로 기본값을 둘 수 없다.
-- 3) 루프백(127.0.0.1, ::1)·Docker 내부망은 필터에 하드코딩된 상시 허용이라
--    DB 규칙으로 넣을 필요가 없다.

-- ============================================
-- 적용 확인
-- ============================================
-- 테이블이 생성되었는지 확인 (1 이어야 정상)
SELECT COUNT(*) AS created_table
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name = 'tb_allowed_ip';

-- 컬럼 14개가 모두 생성되었는지 확인 (14 이어야 정상)
SELECT COUNT(*) AS created_columns
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'tb_allowed_ip';

-- 제약·인덱스 확인 (PRIMARY / uk_allowed_ip_value_campus / idx_allowed_ip_active / FK_tb_campus_TO_tb_allowed_ip)
-- SELECT index_name, seq_in_index, column_name, non_unique
-- FROM information_schema.statistics
-- WHERE table_schema = DATABASE() AND table_name = 'tb_allowed_ip'
-- ORDER BY index_name, seq_in_index;

-- 규칙 0건 = 전면 허용 상태인지 확인 (적용 직후 0 이어야 정상)
SELECT COUNT(*) AS active_rules
FROM tb_allowed_ip
WHERE use_yn = 'Y' AND is_active = 1;

-- ============================================
-- 롤백
-- ============================================
-- 주의: 아래는 실행 SQL이 아니라 주석이다. 필요할 때만 사람이 직접 해제해 실행한다.
--       규칙을 되살릴 수 없게 되므로, 반드시 scripts/backup-tables.sh 로
--       tb_allowed_ip 를 먼저 백업한 뒤 실행할 것.
--       테이블만 남기고 차단 기능을 끄고 싶은 경우라면 롤백 대신
--       playbook.ip-allowlist.enabled=false (또는 IP_ALLOWLIST_BYPASS) 로 대응한다.
-- ALTER TABLE tb_allowed_ip DROP FOREIGN KEY FK_tb_campus_TO_tb_allowed_ip;
-- DROP TABLE IF EXISTS tb_allowed_ip;
