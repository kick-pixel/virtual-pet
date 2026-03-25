package com.ruoyi.system.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import jakarta.mail.internet.MimeMessage;
import com.ruoyi.system.domain.VirtualEmailLog;
import com.ruoyi.system.mapper.VirtualEmailLogMapper;
import com.ruoyi.system.service.IEmailService;

/**
 * Email service implementation with i18n support
 */
@Service
public class EmailServiceImpl implements IEmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired(required = false)
    private TemplateEngine templateEngine;

    @Autowired
    private VirtualEmailLogMapper emailLogMapper;

    @Value("${spring.mail.from:noreply@petplatform.com}")
    private String fromEmail;

    @Value("${spring.mail.from-name:Pet Platform}")
    private String fromName;

    /** Subject templates by type and locale */
    private static final Map<String, Map<String, String>> SUBJECTS = new HashMap<>();

    static {
        // English subjects
        Map<String, String> enSubjects = new HashMap<>();
        enSubjects.put("verification_code", "Your Verification Code");
        enSubjects.put("welcome", "Welcome to Pet Platform!");
        enSubjects.put("task_completed", "Your Pet Video is Ready!");
        enSubjects.put("task_failed", "Video Generation Failed");
        enSubjects.put("recharge_success", "Recharge Successful");
        enSubjects.put("low_credits", "Low Credits Warning");
        SUBJECTS.put("en", enSubjects);

        // Chinese subjects
        Map<String, String> zhSubjects = new HashMap<>();
        zhSubjects.put("verification_code", "您的验证码");
        zhSubjects.put("welcome", "欢迎加入宠物平台！");
        zhSubjects.put("task_completed", "您的宠物视频已生成！");
        zhSubjects.put("task_failed", "视频生成失败");
        zhSubjects.put("recharge_success", "充值成功");
        zhSubjects.put("low_credits", "积分余额不足提醒");
        SUBJECTS.put("zh", zhSubjects);
    }

    @Override
    public void sendTemplateEmail(String to, String templateType, String locale, Map<String, Object> variables,
            Long userId) {
        String subject = getLocalizedSubject(templateType, locale);
        String templateName = resolveTemplateName(templateType, locale);

        // Create log entry
        VirtualEmailLog emailLog = new VirtualEmailLog();
        emailLog.setUserId(userId);
        emailLog.setRecipientEmail(to);
        emailLog.setTemplateType(templateType);
        emailLog.setSubject(subject);
        emailLog.setStatus("pending");
        emailLog.setProvider("smtp");
        emailLogMapper.insertVirtualEmailLog(emailLog);

        try {
            if (mailSender == null || templateEngine == null) {
                log.warn("Mail sender not configured, skipping email to: {}", to);
                emailLog.setStatus("skipped");
                emailLog.setErrorMessage("Mail sender not configured");
                emailLogMapper.updateVirtualEmailLog(emailLog);
                return;
            }

            // Render template
            Context context = new Context();
            if (variables != null) {
                context.setVariables(variables);
            }
            context.setVariable("platformName", fromName);
            String htmlContent = templateEngine.process(templateName, context);

            // Send email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);

            // Update log
            emailLog.setStatus("sent");
            emailLog.setSentAt(new Date());
            emailLogMapper.updateVirtualEmailLog(emailLog);

            log.info("Email sent successfully: type={}, to={}", templateType, to);
        } catch (Exception e) {
            log.error("Failed to send email: type={}, to={}", templateType, to, e);
            emailLog.setStatus("failed");
            emailLog.setErrorMessage(e.getMessage());
            emailLogMapper.updateVirtualEmailLog(emailLog);
        }
    }

    @Override
    public void sendVerificationCode(String to, String code, String locale, Long userId) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("code", code);
        vars.put("expireMinutes", 15);
        sendTemplateEmail(to, "verification_code", locale, vars, userId);
    }

    @Override
    public void sendWelcomeEmail(String to, String username, String locale, Long userId) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("username", username);
        sendTemplateEmail(to, "welcome", locale, vars, userId);
    }

    @Override
    public void sendTaskCompletedEmail(String to, String taskTitle, String videoUrl, String locale, Long userId) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("taskTitle", taskTitle);
        vars.put("videoUrl", videoUrl);
        sendTemplateEmail(to, "task_completed", locale, vars, userId);
    }

    @Override
    public void sendTaskFailedEmail(String to, String taskTitle, String reason, String locale, Long userId) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("taskTitle", taskTitle);
        vars.put("reason", reason);
        sendTemplateEmail(to, "task_failed", locale, vars, userId);
    }

    @Override
    public void sendRechargeSuccessEmail(String to, Long creditsAmount, String txHash, String locale, Long userId) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("creditsAmount", creditsAmount);
        vars.put("txHash", txHash);
        sendTemplateEmail(to, "recharge_success", locale, vars, userId);
    }

    @Override
    public void sendLowCreditsWarning(String to, Long currentBalance, String locale, Long userId) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("currentBalance", currentBalance);
        sendTemplateEmail(to, "low_credits", locale, vars, userId);
    }

    /**
     * Get localized subject line
     */
    private String getLocalizedSubject(String templateType, String locale) {
        String lang = locale != null ? locale.substring(0, 2) : "en";
        Map<String, String> langSubjects = SUBJECTS.getOrDefault(lang, SUBJECTS.get("en"));
        return langSubjects.getOrDefault(templateType, templateType);
    }

    /**
     * Resolve Thymeleaf template name based on type and locale.
     * Templates are expected at: templates/email/{locale}/{type}.html
     * Falls back to: templates/email/en/{type}.html
     */
    private String resolveTemplateName(String templateType, String locale) {
        String lang = locale != null ? locale.substring(0, 2) : "en";
        return "email/" + lang + "/" + templateType;
    }
}
