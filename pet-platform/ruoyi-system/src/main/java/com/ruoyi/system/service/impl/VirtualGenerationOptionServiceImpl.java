package com.ruoyi.system.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.VirtualGenerationOption;
import com.ruoyi.system.mapper.VirtualGenerationOptionMapper;
import com.ruoyi.system.service.IVirtualGenerationOptionService;

/**
 * 生成选项配置Service实现
 *
 * @author ruoyi
 */
@Service
public class VirtualGenerationOptionServiceImpl implements IVirtualGenerationOptionService
{
    @Autowired
    private VirtualGenerationOptionMapper generationOptionMapper;

    @Override
    public Map<String, List<VirtualGenerationOption>> getGroupedActiveOptions()
    {
        List<VirtualGenerationOption> options = generationOptionMapper.selectActiveOptions();
        return options.stream()
            .collect(Collectors.groupingBy(
                VirtualGenerationOption::getOptionGroup,
                LinkedHashMap::new,
                Collectors.toList()
            ));
    }

    @Override
    public List<VirtualGenerationOption> getOptionsByGroup(String optionGroup)
    {
        return generationOptionMapper.selectByGroup(optionGroup);
    }

    @Override
    public List<VirtualGenerationOption> getActiveOptions()
    {
        return generationOptionMapper.selectActiveOptions();
    }
}
