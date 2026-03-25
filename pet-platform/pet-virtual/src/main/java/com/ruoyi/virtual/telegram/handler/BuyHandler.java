package com.ruoyi.virtual.telegram.handler;

import com.ruoyi.system.domain.VirtualUser;
import com.ruoyi.system.service.IVirtualUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collections;
import java.util.List;

/**
 * Handle /buy command
 *
 * @author RuoYi
 */
@Component
public class BuyHandler implements CommandHandler {

    @Autowired
    private IVirtualUserService virtualUserService;

    @Override
    public boolean canHandle(String command) {
        return "/buy".equals(command) || "buy".equalsIgnoreCase(command) || "💎 buy credits".equalsIgnoreCase(command)
                || "💎 购买积分".equals(command);
    }

    @Override
    public List<PartialBotApiMethod<?>> handle(Update update) {
        Long telegramId = update.getMessage().getFrom().getId();
        String languageCode = update.getMessage().getFrom().getLanguageCode();
        boolean isChinese = "zh".equalsIgnoreCase(languageCode)
                || (languageCode != null && languageCode.startsWith("zh"));

        VirtualUser user = virtualUserService.selectVirtualUserByTelegramId(String.valueOf(telegramId));
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(update.getMessage().getChatId()));

        if (user == null) {
            String text = isChinese ? "❌ 您尚未绑定账户，请使用 /login 绑定。"
                    : "❌ You are not bound to an account. Please use /login.";
            message.setText(text);
            return Collections.singletonList(message);
        }

        String text = isChinese
                ? "💎 <b>购买积分</b>\n\n请点击下方链接进行充值：\n\n<a href=\"https://pet.example.com/recharge\">点击前往充值页面</a>"
                : "💎 <b>Buy Credits</b>\n\nPlease click the link below to recharge:\n\n<a href=\"https://pet.example.com/recharge\">Go to Recharge Page</a>";

        message.setText(text);
        message.setParseMode("HTML");

        return Collections.singletonList(message);
    }
}
