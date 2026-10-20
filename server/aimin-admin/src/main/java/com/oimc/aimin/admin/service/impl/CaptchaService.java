package com.oimc.aimin.admin.service.impl;

import com.oimc.aimin.admin.model.vo.CaptchaVO;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地算术验证码。
 *
 * <p>设计取舍：原实现依赖阿里云验证码 2.0（需要公网 + AccessKey），本地开发只能靠
 * “关闭校验 + 前端 5 秒兜底提交”绕过，等于没有验证码。这里改为服务端自带的
 * 一次性算术题：无外部依赖、离线可用，且验证码一次性消费，答案从不下发给客户端。
 * 需要更高强度时可替换为图形/滑块实现，接口保持不变。</p>
 */
@Service
public class CaptchaService {

    /** 验证码有效期：3 分钟 */
    private static final long TTL_MILLIS = 3 * 60 * 1000L;

    /** 待校验验证码上限，超过后清理过期项，防止内存膨胀 */
    private static final int MAX_PENDING = 10_000;

    private final SecureRandom random = new SecureRandom();

    private final Map<String, Challenge> challenges = new ConcurrentHashMap<>();

    /**
     * 生成一道新题，返回一次性 ID 与题面。
     */
    public CaptchaVO create() {
        purgeExpired();

        int a = random.nextInt(9) + 1;
        int b = random.nextInt(9) + 1;
        boolean plus = random.nextBoolean();
        if (!plus && a < b) {
            int temp = a;
            a = b;
            b = temp;
        }

        String question = plus ? a + " + " + b + " = ?" : a + " - " + b + " = ?";
        int answer = plus ? a + b : a - b;

        String captchaId = UUID.randomUUID().toString().replace("-", "");
        challenges.put(captchaId, new Challenge(answer, System.currentTimeMillis() + TTL_MILLIS));
        return new CaptchaVO(captchaId, question);
    }

    /**
     * 校验并消费验证码。无论成功失败都会移除，避免被反复尝试。
     */
    public boolean verify(String captchaId, String answer) {
        if (captchaId == null || captchaId.isBlank() || answer == null || answer.isBlank()) {
            return false;
        }
        Challenge challenge = challenges.remove(captchaId);
        if (challenge == null || challenge.expiresAt() < System.currentTimeMillis()) {
            return false;
        }
        try {
            return Integer.parseInt(answer.trim()) == challenge.answer();
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void purgeExpired() {
        if (challenges.size() < MAX_PENDING) {
            return;
        }
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Challenge>> iterator = challenges.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().expiresAt() < now) {
                iterator.remove();
            }
        }
    }

    private record Challenge(int answer, long expiresAt) {
    }
}
