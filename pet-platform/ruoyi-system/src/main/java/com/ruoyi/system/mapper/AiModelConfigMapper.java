package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.AiModelConfig;

/**
 * AI模型配置 数据层
 *
 * @author ruoyi
 */
public interface AiModelConfigMapper
{
    /**
     * 查询AI模型配置
     *
     * @param modelId 模型ID
     * @return AI模型配置
     */
    public AiModelConfig selectModelConfigById(Long modelId);

    /**
     * 根据提供商和模型名称查询配置
     *
     * @param provider 提供商
     * @param modelName 模型名称
     * @return AI模型配置
     */
    public AiModelConfig selectModelConfigByProviderAndName(String provider, String modelName);

    /**
     * 查询AI模型配置列表
     *
     * @param modelConfig AI模型配置
     * @return AI模型配置集合
     */
    public List<AiModelConfig> selectModelConfigList(AiModelConfig modelConfig);

    /**
     * 根据提供商查询模型列表
     *
     * @param provider 提供商
     * @return AI模型配置集合
     */
    public List<AiModelConfig> selectModelConfigByProvider(String provider);

    /**
     * 查询已启用的模型配置列表
     *
     * @return AI模型配置集合
     */
    public List<AiModelConfig> selectEnabledModelConfigList();

    /**
     * 根据模型类型查询配置列表
     *
     * @param modelType 模型类型
     * @return AI模型配置集合
     */
    public List<AiModelConfig> selectModelConfigByType(String modelType);

    /**
     * 新增AI模型配置
     *
     * @param modelConfig AI模型配置
     * @return 结果
     */
    public int insertModelConfig(AiModelConfig modelConfig);

    /**
     * 修改AI模型配置
     *
     * @param modelConfig AI模型配置
     * @return 结果
     */
    public int updateModelConfig(AiModelConfig modelConfig);

    /**
     * 删除AI模型配置
     *
     * @param modelId 模型ID
     * @return 结果
     */
    public int deleteModelConfigById(Long modelId);

    /**
     * 批量删除AI模型配置
     *
     * @param modelIds 需要删除的模型ID
     * @return 结果
     */
    public int deleteModelConfigByIds(Long[] modelIds);
}
