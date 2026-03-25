package com.ruoyi.virtual.telegram.handler;

import com.ruoyi.virtual.config.VirtualConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collections;
import java.util.List;

/**
 * Handle /login command
 *
 * @author RuoYi
 */
@Component
public class LoginHandler implements CommandHandler {

    @Autowired
    private VirtualConfig virtualConfig;

    @Override
    public boolean canHandle(String command) {
        return "/login".equals(command) || "login".equalsIgnoreCase(command);
    }

    @Override
    public List<PartialBotApiMethod<?>> handle(Update update) {
        Long telegramId = update.getMessage().getFrom().getId();
        String languageCode = update.getMessage().getFrom().getLanguageCode();
        boolean isChinese = "zh".equalsIgnoreCase(languageCode)
                || (languageCode != null && languageCode.startsWith("zh"));

        // In a real scenario, we would generate a unique token stored in Redis with TTL
        // For now, we point to the web app binding page with telegramId as param
        // (simplified)
        // Or better: prompt user to enter their email in the bot to receive a binding
        // code.

        // Let's guide user to Web App for binding
        // Let's guide user to Web App for binding
        // We'll use a placeholder or assume localhost for dev.

        String loginUrl = "https://pet.example.com/user/settings?bind=telegram&tgId=" + telegramId;

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(update.getMessage().getChatId()));

        String text = isChinese
                ? "🔗 <b>绑定账户</b>\n\n请点击下方链接登录并绑定您的 Telegram 账号：\n\n<a href=\"%s\">点击这里绑定</a>\n\n绑定后您将收到 50 积分奖励！"
                : "🔗 <b>Bind Account</b>\n\nPlease click the link below to login and bind your Telegram account:\n\n<a href=\"%s\">Click here to bind</a>\n\nYou will receive 50 bonus credits after binding!";

        message.setText(String.format(text, loginUrl));
        message.setParseMode("HTML");

        return Collections.singletonList(message);
    }
}
