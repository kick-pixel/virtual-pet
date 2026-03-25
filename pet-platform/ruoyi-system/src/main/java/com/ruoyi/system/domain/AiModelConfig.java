package com.ruoyi.system.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI模型配置对象 ai_model_config
 *
 * @author ruoyi
 */
public class AiModelConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 模型ID */
    @Excel(name = "Model ID", cellType = ColumnType.NUMERIC)
    private Long modelId;

    /** AI提供商 */
    @Excel(name = "AI Provider")
    private String provider;

    /** 模型名称 */
    @Excel(name = "Model Name")
    private String modelName;

    /** 模型显示名称 */
    @Excel(name = "Display Name")
    private String modelDisplayName;

    /** 模型类型（text_to_video/image_to_video） */
    @Excel(name = "Model Type")
    private String modelType;

    /** API端点 */
    private String apiEndpoint;

    /** 每秒价格（USD） */
    @Excel(name = "Price/Second")
    private BigDecimal pricePerSecond;

    /** 最大视频时长（秒） */
    @Excel(name = "Max Duration", cellType = ColumnType.NUMERIC)
    private Integer maxDuration;

    /** 支持的分辨率（逗号分隔） */
    @Excel(name = "Supported Resolutions")
    private String supportedResolutions;

    /** 状态（0停用 1启用） */
    @Excel(name = "Status", readConverterExp = "0=Disabled,1=Enabled")
    private String status;

    /** 排序 */
    private Integer sortOrder;

    public Long getModelId()
    {
        return modelId;
    }

    public void setModelId(Long modelId)
    {
        this.modelId = modelId;
    }

    public String getProvider()
    {
        return provider;
    }

    public void setProvider(String provider)
    {
        this.provider = provider;
    }

    public String getModelName()
    {
        return modelName;
    }

    public void setModelName(String modelName)
    {
        this.modelName = modelName;
    }

    public String getModelDisplayName()
    {
        return modelDisplayName;
    }

    public void setModelDisplayName(String modelDisplayName)
    {
        this.modelDisplayName = modelDisplayName;
    }

    public String getModelType()
    {
        return modelType;
    }

    public void setModelType(String modelType)
    {
        this.modelType = modelType;
    }

    public String getApiEndpoint()
    {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint)
    {
        this.apiEndpoint = apiEndpoint;
    }

    public BigDecimal getPricePerSecond()
    {
        return pricePerSecond;
    }

    public void setPricePerSecond(BigDecimal pricePerSecond)
    {
        this.pricePerSecond = pricePerSecond;
    }

    public Integer getMaxDuration()
    {
        return maxDuration;
    }

    public void setMaxDuration(Integer maxDuration)
    {
        this.maxDuration = maxDuration;
    }

    public String getSupportedResolutions()
    {
        return supportedResolutions;
    }

    public void setSupportedResolutions(String supportedResolutions)
    {
        this.supportedResolutions = supportedResolutions;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder)
    {
        this.sortOrder = sortOrder;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("modelId", getModelId())
            .append("provider", getProvider())
            .append("modelName", getModelName())
            .append("modelDisplayName", getModelDisplayName())
            .append("modelType", getModelType())
            .append("pricePerSecond", getPricePerSecond())
            .append("maxDuration", getMaxDuration())
            .append("supportedResolutions", getSupportedResolutions())
            .append("status", getStatus())
            .append("sortOrder", getSortOrder())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
