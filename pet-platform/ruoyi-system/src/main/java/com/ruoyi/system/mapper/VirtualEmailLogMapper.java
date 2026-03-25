package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.VirtualEmailLog;

/**
 * 邮件发送日志Mapper接口
 */
public interface VirtualEmailLogMapper {
    public VirtualEmailLog selectByLogId(Long logId);

    public List<VirtualEmailLog> selectByUserId(Long userId);

    public List<VirtualEmailLog> selectVirtualEmailLogList(VirtualEmailLog log);

    public int insertVirtualEmailLog(VirtualEmailLog log);

    public int updateVirtualEmailLog(VirtualEmailLog log);
}
