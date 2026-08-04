package playbook.encore.back.integration.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import playbook.encore.back.campus.dao.CampusRepository;
import playbook.encore.back.campus.entity.Campus;
import playbook.encore.back.common.util.IntegrationCrypto;
import playbook.encore.back.integration.dao.CampusChannelRepository;
import playbook.encore.back.integration.dao.IntegrationConfigRepository;
import playbook.encore.back.integration.dto.CampusChannelDto;
import playbook.encore.back.integration.dto.IntegrationConfigDto;
import playbook.encore.back.integration.entity.CampusChannel;
import playbook.encore.back.integration.entity.IntegrationConfig;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 외부 연동 설정의 단일 진입점.
 * .env 가 아닌 DB(tb_integration_config / tb_campus_channel)에서 값을 읽고,
 * 최초 기동 시 .env 값으로 시드한다(기존 배포 무중단 마이그레이션).
 * 시크릿 값은 IntegrationCrypto 로 암복호화한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntegrationService {

    // ===== config key 상수 =====
    public static final String KEY_DISCORD_BOT_TOKEN = "DISCORD_BOT_TOKEN";
    public static final String KEY_DISCORD_LINK_CHANNEL_ID = "DISCORD_LINK_CHANNEL_ID";
    public static final String KEY_DISCORD_APPLICATION_ID = "DISCORD_APPLICATION_ID";
    public static final String KEY_WORK24_API_KEY = "WORK24_API_KEY";
    public static final String KEY_NAVER_CLIENT_ID = "NAVER_CLIENT_ID";
    public static final String KEY_NAVER_CLIENT_SECRET = "NAVER_CLIENT_SECRET";
    public static final String KEY_NL_API_KEY = "NL_API_KEY";

    public static final String CAT_DISCORD = "DISCORD";
    public static final String CAT_WORK24 = "WORK24";
    public static final String CAT_NAVER = "NAVER";
    public static final String CAT_NL = "NL";

    private final IntegrationConfigRepository configRepository;
    private final CampusChannelRepository campusChannelRepository;
    private final CampusRepository campusRepository;
    private final IntegrationCrypto crypto;

    // 복호화된 평문 캐시 (config_key -> plain value)
    private final Map<String, String> configCache = new ConcurrentHashMap<>();
    // campusId -> {channelId, roleId}
    private final Map<Integer, String[]> campusChannelCache = new ConcurrentHashMap<>();

    // ===== .env 폴백/시드 값 =====
    @Value("${DISCORD_BOT_TOKEN:}") private String envBotToken;
    @Value("${DISCORD_LINK_CHANNEL_ID:}") private String envLinkChannelId;
    @Value("${DISCORD_APPLICATION_ID:}") private String envApplicationId;
    @Value("${WORK24_API_KEY:}") private String envWork24Key;
    @Value("${CLIENT_ID:}") private String envNaverClientId;
    @Value("${CLIENT_SECRET:}") private String envNaverClientSecret;
    @Value("${NL_API_KEY:}") private String envNlApiKey;
    @Value("${DISCORD_CHANNEL_SEOCHO:}") private String envChannelSeocho;
    @Value("${DISCORD_CHANNEL_GVALLEY:}") private String envChannelGvalley;
    @Value("${DISCORD_CHANNEL_DONGJAK:}") private String envChannelDongjak;
    // 설치 마법사가 디스코드 서버에 자동 생성한 캠퍼스 역할 ID (플북 연동 시 부여되어 채널 해금)
    @Value("${DISCORD_ROLE_SEOCHO:}") private String envRoleSeocho;
    @Value("${DISCORD_ROLE_GVALLEY:}") private String envRoleGvalley;
    @Value("${DISCORD_ROLE_DONGJAK:}") private String envRoleDongjak;

    @PostConstruct
    public void init() {
        seedConfigs();
        seedCampusChannels();
        reloadCache();
        log.info("[IntegrationService] 연동 설정 초기화 완료 - config {}건, campusChannel {}건",
                configCache.size(), campusChannelCache.size());
    }

    // ===== 시드 =====
    private void seedConfigs() {
        seedConfig(KEY_DISCORD_BOT_TOKEN, envBotToken, true, CAT_DISCORD, "디스코드 봇 토큰");
        seedConfig(KEY_DISCORD_LINK_CHANNEL_ID, envLinkChannelId, false, CAT_DISCORD, "디스코드 연동 안내 채널 ID");
        seedConfig(KEY_DISCORD_APPLICATION_ID, envApplicationId, false, CAT_DISCORD, "디스코드 애플리케이션(클라이언트) ID");
        seedConfig(KEY_WORK24_API_KEY, envWork24Key, true, CAT_WORK24, "Work24 훈련과정 API 키");
        seedConfig(KEY_NAVER_CLIENT_ID, envNaverClientId, false, CAT_NAVER, "네이버 책 검색 API Client ID");
        seedConfig(KEY_NAVER_CLIENT_SECRET, envNaverClientSecret, true, CAT_NAVER, "네이버 책 검색 API Client Secret");
        seedConfig(KEY_NL_API_KEY, envNlApiKey, true, CAT_NL, "국립중앙도서관 ISBN API 인증키");
    }

    private void seedConfig(String key, String envValue, boolean isSecret, String category, String description) {
        if (configRepository.existsByConfigKey(key)) {
            return;
        }
        String stored = (envValue != null && !envValue.isBlank() && isSecret)
                ? crypto.encrypt(envValue)
                : envValue;
        IntegrationConfig config = IntegrationConfig.builder()
                .configKey(key)
                .configValue(stored)
                .isSecret(isSecret)
                .category(category)
                .description(description)
                .build();
        configRepository.save(config);
        log.info("[IntegrationService] 설정 시드: {}", key);
    }

    /**
     * 기존 하드코딩 매핑(campusId 1=서초,2=지밸리,3=동작)을 .env 채널·역할값으로 시드.
     *
     * <p>신규 설치(캠퍼스 1개)에서는 설치 마법사가 만든 캠퍼스가 campusId=1 이 되므로
     * {@code DISCORD_CHANNEL_SEOCHO} / {@code DISCORD_ROLE_SEOCHO} 슬롯이 그대로 쓰인다.
     * 캠퍼스가 없는 id 는 {@code seedCampusChannel} 이 조용히 건너뛴다.
     */
    private void seedCampusChannels() {
        seedCampusChannel(1, envChannelSeocho, envRoleSeocho);
        seedCampusChannel(2, envChannelGvalley, envRoleGvalley);
        seedCampusChannel(3, envChannelDongjak, envRoleDongjak);
    }

    private void seedCampusChannel(Integer campusId, String envChannelId) {
        seedCampusChannel(campusId, envChannelId, null);
    }

    /**
     * 캠퍼스별 디스코드 채널·역할 매핑을 최초 1회만 시드한다.
     *
     * <p>이미 매핑 행이 있으면 <b>아무것도 하지 않는다.</b> 재기동할 때마다 env 값으로
     * 덮어쓰면 운영자가 연동 화면에서 고친 값이 되돌아가기 때문이다.
     *
     * <p>{@code envRoleId} 는 설치 마법사가 디스코드 서버에 캠퍼스 역할을 자동 생성한 뒤
     * 그 ID 를 {@code .env} 로 넘겨준 값이다. 이 슬롯이 없던 동안에는 마법사가 역할을
     * 만들어 놓고도 운영자가 연동 탭에서 역할 ID 를 손으로 옮겨 적어야 했다.
     */
    private void seedCampusChannel(Integer campusId, String envChannelId, String envRoleId) {
        if (campusChannelRepository.findBySeqCampus_SeqCampus(campusId).isPresent()) {
            return;
        }
        Optional<Campus> campus = campusRepository.findById(campusId);
        if (campus.isEmpty()) {
            return;
        }
        CampusChannel cc = CampusChannel.builder()
                .seqCampus(campus.get())
                .discordChannelId(envChannelId != null && !envChannelId.isBlank() ? envChannelId : null)
                .discordRoleId(envRoleId != null && !envRoleId.isBlank() ? envRoleId : null)
                .build();
        campusChannelRepository.save(cc);
        log.info("[IntegrationService] 캠퍼스 채널 시드: campusId={}, roleId={}",
                campusId, envRoleId != null && !envRoleId.isBlank() ? "설정됨" : "미설정");
    }

    // ===== 캐시 =====
    public void reloadCache() {
        configCache.clear();
        for (IntegrationConfig c : configRepository.findAll()) {
            String plain = c.isSecret() ? safeDecrypt(c.getConfigValue()) : c.getConfigValue();
            configCache.put(c.getConfigKey(), plain == null ? "" : plain);
        }
        campusChannelCache.clear();
        // 캠퍼스(LAZY)를 페치 조인으로 즉시 로딩 → 트랜잭션 밖(@PostConstruct)에서도 안전
        for (CampusChannel cc : campusChannelRepository.findAllWithCampus()) {
            if (cc.getSeqCampus() != null) {
                campusChannelCache.put(cc.getSeqCampus().getSeqCampus(),
                        new String[]{cc.getDiscordChannelId(), cc.getDiscordRoleId()});
            }
        }
    }

    private String safeDecrypt(String stored) {
        try {
            return crypto.decrypt(stored);
        } catch (Exception e) {
            log.error("[IntegrationService] 시크릿 복호화 실패, 빈 값 사용");
            return "";
        }
    }

    // ===== 게터 (앱 내부 사용) =====
    public String getConfigValue(String key) {
        return configCache.getOrDefault(key, "");
    }

    public String getDiscordBotToken() { return getConfigValue(KEY_DISCORD_BOT_TOKEN); }
    public String getLinkChannelId() { return getConfigValue(KEY_DISCORD_LINK_CHANNEL_ID); }
    public String getDiscordApplicationId() { return getConfigValue(KEY_DISCORD_APPLICATION_ID); }
    public String getWork24Key() { return getConfigValue(KEY_WORK24_API_KEY); }
    public String getNaverClientId() { return getConfigValue(KEY_NAVER_CLIENT_ID); }
    public String getNaverClientSecret() { return getConfigValue(KEY_NAVER_CLIENT_SECRET); }
    public String getNlApiKey() { return getConfigValue(KEY_NL_API_KEY); }

    public String getChannelIdByCampus(Integer campusId) {
        if (campusId == null) return null;
        String[] v = campusChannelCache.get(campusId);
        return v != null ? v[0] : null;
    }

    public String getRoleIdByCampus(Integer campusId) {
        if (campusId == null) return null;
        String[] v = campusChannelCache.get(campusId);
        return v != null ? v[1] : null;
    }

    // ===== CRUD (운영자 연동탭) =====
    @Transactional(readOnly = true)
    public List<IntegrationConfigDto> getAllConfigs() {
        return configRepository.findAll().stream()
                .map(this::toConfigDto)
                .collect(Collectors.toList());
    }

    private IntegrationConfigDto toConfigDto(IntegrationConfig c) {
        boolean configured = c.getConfigValue() != null && !c.getConfigValue().isBlank();
        String shown = c.isSecret()
                ? (configured ? "********" : "")
                : c.getConfigValue();
        return IntegrationConfigDto.builder()
                .configKey(c.getConfigKey())
                .configValue(shown)
                .isSecret(c.isSecret())
                .configured(configured)
                .category(c.getCategory())
                .description(c.getDescription())
                .build();
    }

    @Transactional
    public IntegrationConfigDto updateConfig(String key, String newValue) {
        IntegrationConfig config = configRepository.findByConfigKey(key)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 설정 키입니다: " + key));
        String stored = (newValue != null && !newValue.isBlank() && config.isSecret())
                ? crypto.encrypt(newValue)
                : newValue;
        config.setConfigValue(stored);
        configRepository.save(config);
        reloadCache();
        return toConfigDto(config);
    }

    @Transactional(readOnly = true)
    public List<CampusChannelDto> getAllCampusChannels() {
        return campusChannelRepository.findAllWithCampus().stream()
                .map(cc -> CampusChannelDto.builder()
                        .seqCampus(cc.getSeqCampus().getSeqCampus())
                        .campusName(cc.getSeqCampus().getNameCampus())
                        .discordChannelId(cc.getDiscordChannelId())
                        .discordRoleId(cc.getDiscordRoleId())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public CampusChannelDto upsertCampusChannel(Integer campusId, String channelId, String roleId) {
        CampusChannel cc = campusChannelRepository.findBySeqCampus_SeqCampus(campusId)
                .orElseGet(() -> {
                    Campus campus = campusRepository.findById(campusId)
                            .orElseThrow(() -> new IllegalArgumentException("해당 캠퍼스는 존재하지 않습니다."));
                    return CampusChannel.builder().seqCampus(campus).build();
                });
        cc.setDiscordChannelId(channelId);
        cc.setDiscordRoleId(roleId);
        CampusChannel saved = campusChannelRepository.save(cc);
        reloadCache();
        return CampusChannelDto.builder()
                .seqCampus(saved.getSeqCampus().getSeqCampus())
                .campusName(saved.getSeqCampus().getNameCampus())
                .discordChannelId(saved.getDiscordChannelId())
                .discordRoleId(saved.getDiscordRoleId())
                .build();
    }
}
