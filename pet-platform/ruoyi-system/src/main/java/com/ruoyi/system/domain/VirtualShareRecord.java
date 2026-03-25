package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 视频分享记录对象 virtual_share_record
 */
public class VirtualShareRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long shareId;
    private Long userId;
    private Long taskId;
    private String platform;
    private String shareUrl;
    private String shareContent;
    private String status;
    private Long rewardCredits;
    private Long rewardTxId;
    private String errorMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date sharedAt;

    public Long getShareId() {
        return shareId;
    }

    public void setShareId(Long shareId) {
        this.shareId = shareId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public String getShareContent() {
        return shareContent;
    }

    public void setShareContent(String shareContent) {
        this.shareContent = shareContent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getRewardCredits() {
        return rewardCredits;
    }

    public void setRewardCredits(Long rewardCredits) {
        this.rewardCredits = rewardCredits;
    }

    public Long getRewardTxId() {
        return rewardTxId;
    }

    public void setRewardTxId(Long rewardTxId) {
        this.rewardTxId = rewardTxId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Date getSharedAt() {
        return sharedAt;
    }

    public void setSharedAt(Date sharedAt) {
        this.sharedAt = sharedAt;
    }
}
