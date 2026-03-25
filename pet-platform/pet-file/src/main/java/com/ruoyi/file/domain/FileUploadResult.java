package com.ruoyi.file.domain;

/**
 * 文件上传结果
 *
 * @author ruoyi
 */
public class FileUploadResult
{
    /** 文件ID */
    private Long fileId;

    /** 文件名称 */
    private String fileName;

    /** 存储Key */
    private String fileKey;

    /** 文件访问URL */
    private String fileUrl;

    /** 文件大小 */
    private Long fileSize;

    /** 文件类型 */
    private String fileType;

    /** MIME类型 */
    private String mimeType;

    /** 文件扩展名 */
    private String fileExt;

    public FileUploadResult()
    {
    }

    public FileUploadResult(Long fileId, String fileName, String fileKey, String fileUrl,
                            Long fileSize, String fileType, String mimeType, String fileExt)
    {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileKey = fileKey;
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.mimeType = mimeType;
        this.fileExt = fileExt;
    }

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
}
