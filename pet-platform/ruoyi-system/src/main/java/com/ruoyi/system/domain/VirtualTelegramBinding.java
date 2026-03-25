package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * Telegram用户绑定对象 virtual_telegram_binding
 */
public class VirtualTelegramBinding extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private Long telegramUserId;
    private String telegramUsername;
    private Long telegramChatId;
    private String bindCode;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date bindCodeExpire;

    private String botState;
    private String botStateData;
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTelegramUserId() {
        return telegramUserId;
    }

    public void setTelegramUserId(Long telegramUserId) {
        this.telegramUserId = telegramUserId;
    }

    public String getTelegramUsername() {
        return telegramUsername;
    }

    public void setTelegramUsername(String telegramUsername) {
        this.telegramUsername = telegramUsername;
    }

    public Long getTelegramChatId() {
        return telegramChatId;
    }

    public void setTelegramChatId(Long telegramChatId) {
        this.telegramChatId = telegramChatId;
    }

    public String getBindCode() {
        return bindCode;
    }

    public void setBindCode(String bindCode) {
        this.bindCode = bindCode;
    }

    public Date getBindCodeExpire() {
        return bindCodeExpire;
    }

    public void setBindCodeExpire(Date bindCodeExpire) {
        this.bindCodeExpire = bindCodeExpire;
    }

    public String getBotState() {
        return botState;
    }

    public void setBotState(String botState) {
        this.botState = botState;
    }

    public String getBotStateData() {
        return botStateData;
    }

    public void setBotStateData(String botStateData) {
        this.botStateData = botStateData;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
