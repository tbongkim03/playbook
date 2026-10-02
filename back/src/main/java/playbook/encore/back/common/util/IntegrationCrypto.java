package playbook.encore.back.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 연동 시크릿(봇 토큰·API 키) 암복호화 유틸.
 * AES/GCM/NoPadding, 키는 env INTEGRATION_SECRET_KEY 의 SHA-256(256bit).
 * 저장 포맷: base64( iv(12B) || ciphertext+tag ), 식별을 위해 "enc:" 접두사를 붙인다.
 */
@Slf4j
@Component
public class IntegrationCrypto {

    private static final String PREFIX = "enc:";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKeySpec keySpec;
    private final SecureRandom secureRandom = new SecureRandom();

    public IntegrationCrypto(
            @Value("${integration.secret-key}") String integrationKey) {
        this.keySpec = deriveKey(integrationKey);
    }

    private SecretKeySpec deriveKey(String source) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha256.digest(source.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(hash, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("암호화 키 파생 실패", e);
        }
    }

    /** 평문 → "enc:base64". null/blank 는 그대로 반환. 이미 암호문이면 재암호화하지 않음. */
    public String encrypt(String plain) {
        if (plain == null || plain.isEmpty() || isEncrypted(plain)) {
            return plain;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] cipherText = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);
            return PREFIX + Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new IllegalStateException("연동 시크릿 암호화 실패", e);
        }
    }

    /** "enc:base64" → 평문. 접두사 없으면(레거시 평문) 그대로 반환. */
    public String decrypt(String stored) {
        if (stored == null || stored.isEmpty() || !isEncrypted(stored)) {
            return stored;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(stored.substring(PREFIX.length()));
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            byte[] cipherText = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, IV_LENGTH, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("연동 시크릿 복호화 실패 (키 불일치 또는 손상): {}", e.getMessage());
            throw new IllegalStateException("연동 시크릿 복호화 실패", e);
        }
    }

    public boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }
}
