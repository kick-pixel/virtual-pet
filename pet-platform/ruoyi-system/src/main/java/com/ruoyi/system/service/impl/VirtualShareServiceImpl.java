package com.ruoyi.system.service.impl;

import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.VirtualShareRecord;
import com.ruoyi.system.mapper.VirtualShareRecordMapper;
import com.ruoyi.system.service.IVirtualShareService;

/**
 * Virtual platform share service implementation
 */
@Service
public class VirtualShareServiceImpl implements IVirtualShareService {
    private static final Logger log = LoggerFactory.getLogger(VirtualShareServiceImpl.class);

    @Autowired
    private VirtualShareRecordMapper shareRecordMapper;

    @Override
    public VirtualShareRecord createShareRecord(Long userId, Long taskId, String platform, String shareUrl) {
        // 防重复：同一 taskId + platform + userId 查询是否已有记录
        VirtualShareRecord query = new VirtualShareRecord();
        query.setUserId(userId);
        query.setTaskId(taskId);
        query.setPlatform(platform);
        List<VirtualShareRecord> existing = shareRecordMapper.selectVirtualShareRecordList(query);
        if (!existing.isEmpty()) {
            log.debug("Share record already exists, user: {}, task: {}, platform: {}", userId, taskId, platform);
            return existing.get(0);
        }

        VirtualShareRecord record = new VirtualShareRecord();
        record.setUserId(userId);
        record.setTaskId(taskId);
        record.setPlatform(platform);
        record.setShareUrl(shareUrl);
        record.setStatus("completed");
        record.setSharedAt(new Date());
        shareRecordMapper.insertVirtualShareRecord(record);

        log.info("Share recorded, user: {}, task: {}, platform: {}", userId, taskId, platform);
        return record;
    }

    @Override
    public List<VirtualShareRecord> getUserShareRecords(Long userId) {
        return shareRecordMapper.selectByUserId(userId);
    }
}
