package com.ruoyi.virtual.config;

import com.ruoyi.virtual.telegram.VirtualPetBot;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Telegram Bot Configuration
 *
 * @author RuoYi
 */
@Configuration
public class TelegramBotConfig {

    @Bean
    @ConditionalOnProperty(prefix = "virtual.telegram", name = "enabled", havingValue = "true")
    public TelegramBotsApi telegramBotsApi(VirtualPetBot virtualPetBot) throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        // We don't register webhook bot here usually if we control the webhook endpoint
        // manually via Controller.
        // But if using starter to handle webhook, we might.
        // Given we implemented a Controller to receive updates, we don't need to
        // register the bot with API for polling.
        // We just need the Bot instance (which is a @Component).
        // However, if we wanted Polling, we would register it here.
        // Since we designed VirtualPetBot as WebhookBot, we leave it as is.
        // Use this config class just to placeholder provided beans if needed.
        return api;
    }
}
