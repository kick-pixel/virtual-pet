package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 文件信息对象 sys_file_info
 *
 * @author ruoyi
 */
public class SysFileInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 文件ID */
    @Excel(name = "File ID", cellType = ColumnType.NUMERIC)
    private Long fileId;

    /** 文件名称 */
    @Excel(name = "File Name")
    private String fileName;

    /** 存储Key（路径） */
    private String fileKey;

    /** 文件访问URL */
    @Excel(name = "File URL")
    private String fileUrl;

    /** 文件大小（字节） */
    @Excel(name = "File Size")
    private Long fileSize;

    /** 文件类型（image/video/document） */
    @Excel(name = "File Type")
    private String fileType;

    /** MIME类型 */
    private String mimeType;

    /** 文件扩展名 */
    @Excel(name = "Extension")
    private String fileExt;

    /** 存储提供商（s3/aliyun/minio） */
    @Excel(name = "Storage Provider")
    private String storageProvider;

    /** 存储桶名称 */
    private String bucketName;

    /** 上传用户ID */
    private Long uploadUserId;

    /** 用户类型（admin/app_user） */
    private String uploadUserType;

    /** 业务类型 */
    @Excel(name = "Business Type")
    private String businessType;

    /** 关联业务ID */
    private Long businessId;

    /** 状态（0正常 1删除） */
    @Excel(name = "Status", readConverterExp = "0=Normal,1=Deleted")
    private String status;

    public Long getFileId()
    {
        return fileId;
    }

    public void setFileId(Long fileId)
    {
        this.fileId = fileId;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getFileKey()
    {
        return fileKey;
    }

    public void setFileKey(String fileKey)
    {
        this.fileKey = fileKey;
    }

    public String getFileUrl()
    {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl)
    {
        this.fileUrl = fileUrl;
    }

    public Long getFileSize()
    {
        return fileSize;
    }

    public void setFileSize(Long fileSize)
    {
        this.fileSize = fileSize;
    }

    public String getFileType()
    {
        return fileType;
    }

    public void setFileType(String fileType)
    {
        this.fileType = fileType;
    }

    public String getMimeType()
    {
        return mimeType;
    }

    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }

    public String getFileExt()
    {
        return fileExt;
    }

    public void setFileExt(String fileExt)
    {
        this.fileExt = fileExt;
    }

    public String getStorageProvider()
    {
        return storageProvider;
    }

    public void setStorageProvider(String storageProvider)
    {
        this.storageProvider = storageProvider;
    }

    public String getBucketName()
    {
        return bucketName;
    }

    public void setBucketName(String bucketName)
    {
        this.bucketName = bucketName;
    }

    public Long getUploadUserId()
    {
        return uploadUserId;
    }

    public void setUploadUserId(Long uploadUserId)
    {
        this.uploadUserId = uploadUserId;
    }

    public String getUploadUserType()
    {
        return uploadUserType;
    }

    public void setUploadUserType(String uploadUserType)
    {
        this.uploadUserType = uploadUserType;
    }

    public String getBusinessType()
    {
        return businessType;
    }

    public void setBusinessType(String businessType)
    {
        this.businessType = businessType;
    }

    public Long getBusinessId()
    {
        return businessId;
    }

    public void setBusinessId(Long businessId)
    {
        this.businessId = businessId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("fileId", getFileId())
            .append("fileName", getFileName())
            .append("fileKey", getFileKey())
            .append("fileUrl", getFileUrl())
            .append("fileSize", getFileSize())
            .append("fileType", getFileType())
            .append("storageProvider", getStorageProvider())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
