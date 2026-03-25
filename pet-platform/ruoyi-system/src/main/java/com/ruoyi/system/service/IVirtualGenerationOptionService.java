package com.ruoyi.system.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.system.domain.VirtualGenerationOption;

/**
 * 生成选项配置Service接口
 *
 * @author ruoyi
 */
public interface IVirtualGenerationOptionService
{
    /**
     * 查询所有启用的选项，按 optionGroup 分组
     *
     * @return 分组后的选项Map
     */
    Map<String, List<VirtualGenerationOption>> getGroupedActiveOptions();

    /**
     * 查询指定分组的选项
     *
     * @param optionGroup 选项分组
     * @return 选项列表
     */
    List<VirtualGenerationOption> getOptionsByGroup(String optionGroup);

    /**
     * 查询所有启用的选项
     *
     * @return 选项列表
     */
    List<VirtualGenerationOption> getActiveOptions();
}
