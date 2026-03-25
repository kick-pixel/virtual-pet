package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.VirtualShareRecord;

/**
 * Virtual platform share service
 */
public interface IVirtualShareService {
    /**
     * 创建分享记录（防重复：同一 taskId + platform + userId 不重复创建）
     *
     * @return 已有记录或新建记录
     */
    public VirtualShareRecord createShareRecord(Long userId, Long taskId, String platform, String shareUrl);

    /** Get share records by user */
    public List<VirtualShareRecord> getUserShareRecords(Long userId);
}
