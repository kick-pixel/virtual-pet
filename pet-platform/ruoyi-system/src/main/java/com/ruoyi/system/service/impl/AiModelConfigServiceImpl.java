package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.AiModelConfig;
import com.ruoyi.system.mapper.AiModelConfigMapper;
import com.ruoyi.system.service.IAiModelConfigService;

/**
 * AI模型配置 服务层实现
 *
 * @author ruoyi
 */
@Service
public class AiModelConfigServiceImpl implements IAiModelConfigService
{
    @Autowired
    private AiModelConfigMapper modelConfigMapper;

    /**
     * 查询AI模型配置
     *
     * @param modelId 模型ID
     * @return AI模型配置
     */
    @Override
    public AiModelConfig selectModelConfigById(Long modelId)
    {
        return modelConfigMapper.selectModelConfigById(modelId);
    }

    /**
     * 根据提供商和模型名称查询配置
     *
     * @param provider 提供商
     * @param modelName 模型名称
     * @return AI模型配置
     */
    @Override
    public AiModelConfig selectModelConfigByProviderAndName(String provider, String modelName)
    {
        return modelConfigMapper.selectModelConfigByProviderAndName(provider, modelName);
    }

    /**
     * 查询AI模型配置列表
     *
     * @param modelConfig AI模型配置
     * @return AI模型配置集合
     */
    @Override
    public List<AiModelConfig> selectModelConfigList(AiModelConfig modelConfig)
    {
        return modelConfigMapper.selectModelConfigList(modelConfig);
    }

    /**
     * 根据提供商查询模型列表
     *
     * @param provider 提供商
     * @return AI模型配置集合
     */
    @Override
    public List<AiModelConfig> selectModelConfigByProvider(String provider)
    {
        return modelConfigMapper.selectModelConfigByProvider(provider);
    }

    /**
     * 查询已启用的模型配置列表
     *
     * @return AI模型配置集合
     */
    @Override
    public List<AiModelConfig> selectEnabledModelConfigList()
    {
        return modelConfigMapper.selectEnabledModelConfigList();
    }

    /**
     * 根据模型类型查询配置列表
     *
     * @param modelType 模型类型
     * @return AI模型配置集合
     */
    @Override
    public List<AiModelConfig> selectModelConfigByType(String modelType)
    {
        return modelConfigMapper.selectModelConfigByType(modelType);
    }

    /**
     * 新增AI模型配置
     *
     * @param modelConfig AI模型配置
     * @return 结果
     */
    @Override
    public int insertModelConfig(AiModelConfig modelConfig)
    {
        return modelConfigMapper.insertModelConfig(modelConfig);
    }

    /**
     * 修改AI模型配置
     *
     * @param modelConfig AI模型配置
     * @return 结果
     */
    @Override
    public int updateModelConfig(AiModelConfig modelConfig)
    {
        return modelConfigMapper.updateModelConfig(modelConfig);
    }

    /**
     * 删除AI模型配置
     *
     * @param modelId 模型ID
     * @return 结果
     */
    @Override
    public int deleteModelConfigById(Long modelId)
    {
        return modelConfigMapper.deleteModelConfigById(modelId);
    }

    /**
     * 批量删除AI模型配置
     *
     * @param modelIds 需要删除的模型ID
     * @return 结果
     */
    @Override
    public int deleteModelConfigByIds(Long[] modelIds)
    {
        return modelConfigMapper.deleteModelConfigByIds(modelIds);
    }
}
