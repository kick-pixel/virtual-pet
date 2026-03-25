package com.ruoyi.virtual.telegram.handler;

import com.ruoyi.system.domain.VirtualUser;
import com.ruoyi.system.service.IVirtualUserService;
import com.ruoyi.virtual.telegram.keyboard.KeyboardFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collections;
import java.util.List;

/**
 * Handle /start command
 *
 * @author RuoYi
 */
@Component
public class StartHandler implements CommandHandler {

    @Autowired
    private IVirtualUserService virtualUserService;

    @Override
    public boolean canHandle(String command) {
        return "/start".equals(command) || "start".equalsIgnoreCase(command);
    }

    @Override
    public List<PartialBotApiMethod<?>> handle(Update update) {
        Long telegramId = update.getMessage().getFrom().getId();
        String username = update.getMessage().getFrom().getUserName();
        String languageCode = update.getMessage().getFrom().getLanguageCode();
        boolean isChinese = "zh".equalsIgnoreCase(languageCode)
                || (languageCode != null && languageCode.startsWith("zh"));

        VirtualUser user = virtualUserService.selectVirtualUserByTelegramId(String.valueOf(telegramId));

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(update.getMessage().getChatId()));

        if (user != null) {
            String welcomeRaw = isChinese ? "欢迎回来，%s！\n您已绑定账户：%s\n请选择以下操作："
                    : "Welcome back, %s!\nYou are linked to account: %s\nPlease select an action:";
            message.setText(String.format(welcomeRaw, username, user.getNickname()));
        } else {
            String welcomeRaw = isChinese ? "欢迎来到 Pet AI Bot！\n您尚未绑定账户，请先登录或注册。\n点击 /login 获取绑定链接。"
                    : "Welcome to Pet AI Bot!\nYou are not linked to an account.\nPlease /login to bind your account.";
            message.setText(welcomeRaw);
        }

        // Always show main menu if bound, or prompt to login
        if (user != null) {
            message.setReplyMarkup(KeyboardFactory.getMainMenuKeyboard(isChinese));
        }

        return Collections.singletonList(message);
    }
}
