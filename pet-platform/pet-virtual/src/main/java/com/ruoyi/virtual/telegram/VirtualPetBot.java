package com.ruoyi.virtual.telegram;

import com.ruoyi.virtual.config.VirtualConfig;
import com.ruoyi.virtual.telegram.handler.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Pet AI Telegram Bot
 *
 * @author RuoYi
 */
@Component
public class VirtualPetBot extends TelegramWebhookBot {

    private static final Logger log = LoggerFactory.getLogger(VirtualPetBot.class);

    @Autowired
    private VirtualConfig virtualConfig;

    private final Map<String, CommandHandler> commandHandlers;

    @Autowired
    public VirtualPetBot(List<CommandHandler> handlers) {
        this.commandHandlers = handlers.stream()
                .collect(Collectors.toMap(h -> h.getClass().getSimpleName(), Function.identity()));
    }

    @Override
    public String getBotUsername() {
        return virtualConfig.getTelegram().getBotUsername();
    }

    @Override
    public String getBotToken() {
        return virtualConfig.getTelegram().getBotToken();
    }

    @Override
    public String getBotPath() {
        return virtualConfig.getTelegram().getWebhookUrl();
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();

            // Iterate through handlers to find a match
            for (CommandHandler handler : commandHandlers.values()) {
                if (handler.canHandle(text)) {
                    List<PartialBotApiMethod<?>> responses = handler.handle(update);
                    if (responses != null && !responses.isEmpty()) {
                        for (PartialBotApiMethod<?> response : responses) {
                            try {
                                if (response instanceof BotApiMethod) {
                                    execute((BotApiMethod<?>) response);
                                } else {
                                    // Handle other types like SendPhoto if needed (requires casting/specific
                                    // execute)
                                    // For now, CommandHandler interface returns PartialBotApiMethod,
                                    // but execute() takes BotApiMethod or specialized methods.
                                    // We'll assume BotApiMethod for now or handle exceptions.
                                    log.warn("Unsupported response type: {}", response.getClass().getName());
                                }
                            } catch (TelegramApiException e) {
                                log.error("Error sending response", e);
                            }
                        }
                    }
                    return null; // Handled
                }
            }

            // Default response if no handler matches
            // Maybe echo or ignore
            // For now, ignore
        }
        return null;
    }

    // Helper to manually trigger update processing from Controller
    public void processUpdate(Update update) {
        this.onWebhookUpdateReceived(update);
    }
}
