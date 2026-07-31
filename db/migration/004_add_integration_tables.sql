-- ============================================
-- 외부 연동 설정 · 캠퍼스 디스코드 매핑 테이블 추가
-- ============================================
-- 목적: .env 에 흩어져 있던 연동값(디스코드 봇 토큰·채널, Work24/Naver/국립중앙도서관
--       API 키)을 DB로 옮겨 전체관리자가 화면에서 관리하고, 캠퍼스별 디스코드
--       채널·역할을 매핑해 플북 연동 시 소속 캠퍼스 채널만 해금한다.
-- 작성일: 2026-07-31
-- 관련: IntegrationConfig, CampusChannel (playbook.encore.back.integration.entity)
-- ============================================

-- ============================================
-- 1. 외부 연동 글로벌 설정 (key-value)
-- ============================================
-- is_secret = 1 인 값의 config_value 는 INTEGRATION_SECRET_KEY 기반 AES 로
-- 암호화되어 저장된다. 평문을 직접 INSERT 하지 말 것 (애플리케이션 경유 필수).
CREATE TABLE IF NOT EXISTS tb_integration_config (
    seq_config      INT          NOT NULL AUTO_INCREMENT      COMMENT '연동설정 PK',
    config_key      VARCHAR(60)  NOT NULL                     COMMENT '설정 키 (DISCORD_BOT_TOKEN, WORK24_API_KEY 등)',
    config_value    TEXT                                      COMMENT '설정 값 (is_secret=1 이면 AES 암호문)',
    is_secret       TINYINT(1)   NOT NULL DEFAULT 0           COMMENT '시크릿 여부 (1:암호화 저장·조회 시 마스킹, 0:평문)',
    category        VARCHAR(20)                               COMMENT '분류 (DISCORD/WORK24/NAVER/NL)',
    description     VARCHAR(200)                              COMMENT '설정 설명 (연동 관리 화면 표시용)',
    use_yn          CHAR(1)      NOT NULL DEFAULT 'Y'         COMMENT '사용 여부 (Y:사용, N:삭제)',
    created_by_type VARCHAR(20)                               COMMENT '생성 주체 유형 (ADMIN/USER/SYSTEM)',
    created_at      DATETIME                                  COMMENT '생성 일시',
    created_by      BIGINT                                    COMMENT '생성자 식별자',
    updated_by_type VARCHAR(20)                               COMMENT '수정 주체 유형 (ADMIN/USER/SYSTEM)',
    updated_at      DATETIME                                  COMMENT '수정 일시',
    updated_by      BIGINT                                    COMMENT '수정자 식별자',
    PRIMARY KEY (seq_config),
    UNIQUE KEY uk_integration_config_key (config_key)
) COMMENT '외부 연동 글로벌 설정 (key-value)';

-- ============================================
-- 2. 캠퍼스별 디스코드 매핑
-- ============================================
-- discord_channel_id : 대여·반납 알림이 발송될 캠퍼스 채널
-- discord_role_id    : 플북 계정 연동 시 부여되어 캠퍼스 채널을 해금하는 역할
CREATE TABLE IF NOT EXISTS tb_campus_channel (
    seq_campus_channel INT         NOT NULL AUTO_INCREMENT    COMMENT '캠퍼스채널 PK',
    seq_campus         INT         NOT NULL                   COMMENT '캠퍼스 PK (tb_campus)',
    discord_channel_id VARCHAR(40)                            COMMENT '디스코드 채널 ID (알림 발송 대상)',
    discord_role_id    VARCHAR(40)                            COMMENT '디스코드 역할 ID (연동 시 부여, 채널 해금용)',
    use_yn             CHAR(1)     NOT NULL DEFAULT 'Y'       COMMENT '사용 여부 (Y:사용, N:삭제)',
    created_by_type    VARCHAR(20)                            COMMENT '생성 주체 유형 (ADMIN/USER/SYSTEM)',
    created_at         DATETIME                               COMMENT '생성 일시',
    created_by         BIGINT                                 COMMENT '생성자 식별자',
    updated_by_type    VARCHAR(20)                            COMMENT '수정 주체 유형 (ADMIN/USER/SYSTEM)',
    updated_at         DATETIME                               COMMENT '수정 일시',
    updated_by         BIGINT                                 COMMENT '수정자 식별자',
    PRIMARY KEY (seq_campus_channel),
    UNIQUE KEY uk_campus_channel_campus (seq_campus)
) COMMENT '캠퍼스별 디스코드 채널·역할 매핑 (캠퍼스 1:1)';

ALTER TABLE tb_campus_channel ADD CONSTRAINT FK_tb_campus_TO_tb_campus_channel
    FOREIGN KEY (seq_campus) REFERENCES tb_campus(seq_campus);

-- ============================================
-- 초기 데이터
-- ============================================
-- tb_integration_config 의 기준 행(7종 키)은 IntegrationService 가 애플리케이션
-- 기동 시 @PostConstruct 로 .env 값을 읽어 시드한다. 시크릿은 그 과정에서
-- 암호화되므로 여기서 INSERT 하지 않는다 (평문 유출·중복키 방지).
--
-- tb_campus_channel 은 캠퍼스별 채널·역할 ID를 운영자가 연동 관리 화면에서
-- 직접 입력한다. 디스코드 서버마다 ID가 다르므로 기본값을 넣지 않는다.

-- ============================================
-- 적용 확인
-- ============================================
-- 테이블 2개가 생성되었는지 확인 (2 이어야 정상)
SELECT COUNT(*) AS created_tables
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN ('tb_integration_config', 'tb_campus_channel');

-- 애플리케이션 기동 후 실행 — 연동 설정 7종이 시드되었는지 확인 (7 이어야 정상)
-- SELECT COUNT(*) AS seeded_configs FROM tb_integration_config WHERE use_yn = 'Y';

-- 시크릿 값이 평문으로 들어가 있지 않은지 육안 확인 (암호문이어야 정상)
-- SELECT config_key, category, is_secret, LEFT(config_value, 12) AS value_head
-- FROM tb_integration_config WHERE use_yn = 'Y' ORDER BY category, config_key;

-- ============================================
-- 롤백
-- ============================================
-- 주의: 연동값이 DB에만 존재하므로, 삭제 전 config_value 를 백업해야 복구 가능하다.
--       scripts/backup-tables.sh 로 두 테이블을 먼저 백업할 것.
-- ALTER TABLE tb_campus_channel DROP FOREIGN KEY FK_tb_campus_TO_tb_campus_channel;
-- DROP TABLE IF EXISTS tb_campus_channel;
-- DROP TABLE IF EXISTS tb_integration_config;
