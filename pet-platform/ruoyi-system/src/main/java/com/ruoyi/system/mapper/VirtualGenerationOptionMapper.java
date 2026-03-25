package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.VirtualGenerationOption;

/**
 * 生成选项配置Mapper接口
 */
public interface VirtualGenerationOptionMapper {
    public VirtualGenerationOption selectByOptionId(Long optionId);

    public List<VirtualGenerationOption> selectByGroup(String optionGroup);

    public List<VirtualGenerationOption> selectActiveOptions();

    public List<VirtualGenerationOption> selectVirtualGenerationOptionList(VirtualGenerationOption option);

    public int insertVirtualGenerationOption(VirtualGenerationOption option);

    public int updateVirtualGenerationOption(VirtualGenerationOption option);
}
