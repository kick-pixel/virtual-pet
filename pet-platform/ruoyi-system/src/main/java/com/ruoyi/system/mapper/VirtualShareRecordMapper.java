package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.VirtualShareRecord;

/**
 * 视频分享记录Mapper接口
 */
public interface VirtualShareRecordMapper {
    public VirtualShareRecord selectByShareId(Long shareId);

    public List<VirtualShareRecord> selectByUserId(Long userId);

    public List<VirtualShareRecord> selectVirtualShareRecordList(VirtualShareRecord record);

    public int insertVirtualShareRecord(VirtualShareRecord record);

    public int updateVirtualShareRecord(VirtualShareRecord record);
}
