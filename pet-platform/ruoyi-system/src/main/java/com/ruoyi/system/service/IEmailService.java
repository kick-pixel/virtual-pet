package com.ruoyi.system.service;

import java.util.Map;

/**
 * Email service interface
 */
public interface IEmailService {
    /**
     * Send email using template
     *
     * @param to           recipient email
     * @param templateType email template type (e.g. "verification_code", "welcome")
     * @param locale       language code (e.g. "en-US", "zh-CN")
     * @param variables    template variables
     * @param userId       optional user ID for logging
     */
    public void sendTemplateEmail(String to, String templateType, String locale, Map<String, Object> variables,
            Long userId);

    /**
     * Send verification code email
     */
    public void sendVerificationCode(String to, String code, String locale, Long userId);

    /**
     * Send welcome email
     */
    public void sendWelcomeEmail(String to, String username, String locale, Long userId);

    /**
     * Send task completion notification
     */
    public void sendTaskCompletedEmail(String to, String taskTitle, String videoUrl, String locale, Long userId);

    /**
     * Send task failure notification
     */
    public void sendTaskFailedEmail(String to, String taskTitle, String reason, String locale, Long userId);

    /**
     * Send recharge success notification
     */
    public void sendRechargeSuccessEmail(String to, Long creditsAmount, String txHash, String locale, Long userId);

    /**
     * Send low credits warning
     */
    public void sendLowCreditsWarning(String to, Long currentBalance, String locale, Long userId);
}
