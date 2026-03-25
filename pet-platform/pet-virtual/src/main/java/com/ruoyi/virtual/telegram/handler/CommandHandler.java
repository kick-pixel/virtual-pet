package com.ruoyi.virtual.telegram.handler;

import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

/**
 * Telegram Command Handler Interface
 *
 * @author RuoYi
 */
public interface CommandHandler {
    /**
     * Check if the handler can handle the given command
     *
     * @param command The command text
     * @return true if can handle
     */
    boolean canHandle(String command);

    /**
     * Handle the update
     *
     * @param update The update object
     * @return List of methods to execute (e.g. SendMessage, SendPhoto)
     */
    List<PartialBotApiMethod<?>> handle(Update update);
}
