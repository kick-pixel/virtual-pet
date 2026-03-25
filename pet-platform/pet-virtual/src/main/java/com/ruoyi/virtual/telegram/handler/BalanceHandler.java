package com.ruoyi.virtual.telegram.handler;

import com.ruoyi.system.domain.VirtualUser;
import com.ruoyi.system.domain.VirtualUserCredits;
import com.ruoyi.system.service.IVirtualCreditsService;
import com.ruoyi.system.service.IVirtualUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Handle /balance command
 *
 * @author RuoYi
 */
@Component
public class BalanceHandler implements CommandHandler {

    @Autowired
    private IVirtualUserService virtualUserService;

    @Autowired
    private IVirtualCreditsService virtualCreditsService;

    @Override
    public boolean canHandle(String command) {
        return "/balance".equals(command) || "balance".equalsIgnoreCase(command)
                || "💰 balance".equalsIgnoreCase(command) || "💰 余额".equals(command);
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

        VirtualUserCredits userCredits = virtualCreditsService.getBalance(user.getUserId());
        BigDecimal balance = userCredits != null ? BigDecimal.valueOf(userCredits.getBalance()) : BigDecimal.ZERO;
        String text = isChinese ? "💰 <b>当前余额</b>\n\n您的账户余额为：<b>%s</b> 积分"
                : "💰 <b>Current Balance</b>\n\nYour account balance is: <b>%s</b> credits";

        message.setText(String.format(text, balance != null ? balance.toPlainString() : "0"));
        message.setParseMode("HTML");

        return Collections.singletonList(message);
    }
}
