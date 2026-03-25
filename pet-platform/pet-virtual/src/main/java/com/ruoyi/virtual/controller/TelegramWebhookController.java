package com.ruoyi.virtual.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.virtual.config.VirtualConfig;
import com.ruoyi.virtual.telegram.VirtualPetBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Telegram Webhook Controller
 *
 * @author RuoYi
 */
@RestController
@RequestMapping("/api/virtual/telegram")
public class TelegramWebhookController {

    private static final Logger log = LoggerFactory.getLogger(TelegramWebhookController.class);

    @Autowired
    private VirtualPetBot virtualPetBot;

    @Autowired
    private VirtualConfig virtualConfig;

    @PostMapping("/webhook")
    public AjaxResult onUpdateReceived(@RequestBody Update update) {
        if (!Boolean.TRUE.equals(virtualConfig.getTelegram().getEnabled())) {
            return AjaxResult.error("Telegram bot is disabled");
        }

        try {
            virtualPetBot.processUpdate(update);
            return AjaxResult.success();
        } catch (Exception e) {
            log.error("Error processing Telegram update", e);
            return AjaxResult.error(e.getMessage());
        }
    }
}
