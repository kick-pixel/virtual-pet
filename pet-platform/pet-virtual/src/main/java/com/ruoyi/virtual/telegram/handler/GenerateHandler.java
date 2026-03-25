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
 * Handle /generate command
 *
 * @author RuoYi
 */
@Component
public class GenerateHandler implements CommandHandler {

    @Autowired
    private IVirtualUserService virtualUserService;

    @Override
    public boolean canHandle(String command) {
        return "/generate".equals(command) || "generate".equalsIgnoreCase(command)
                || "🎬 create video".equalsIgnoreCase(command) || "🎬 生成视频".equals(command);
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

        // For now, we just redirect them to the web app or give a basic instruction
        // In the future, we could implement a conversation flow (State machine) to
        // gather photo and prompts.
        String text = isChinese
                ? "🎬 <b>生成视频</b>\n\n直接在 Telegram 生成视频功能即将推出！\n目前请访问网页版进行创作：\n\n<a href=\"https://pet.example.com/create\">点击前往创作页面</a>"
                : "🎬 <b>Generate Video</b>\n\nDirect video generation in Telegram is coming soon!\nFor now, please visit our website to create:\n\n<a href=\"https://pet.example.com/create\">Go to Creation Page</a>";

        message.setText(text);
        message.setParseMode("HTML");

        return Collections.singletonList(message);
    }
}
