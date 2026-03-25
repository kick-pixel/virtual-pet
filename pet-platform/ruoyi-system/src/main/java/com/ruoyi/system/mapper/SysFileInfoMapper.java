package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysFileInfo;

/**
 * 文件信息 数据层
 *
 * @author ruoyi
 */
public interface SysFileInfoMapper
{
    /**
     * 查询文件信息
     *
     * @param fileId 文件ID
     * @return 文件信息
     */
    public SysFileInfo selectFileInfoById(Long fileId);

    /**
     * 根据文件Key查询文件信息
     *
     * @param fileKey 文件Key
     * @return 文件信息
     */
    public SysFileInfo selectFileInfoByKey(String fileKey);

    /**
     * 查询文件信息列表
     *
     * @param fileInfo 文件信息
     * @return 文件信息集合
     */
    public List<SysFileInfo> selectFileInfoList(SysFileInfo fileInfo);

    /**
     * 新增文件信息
     *
     * @param fileInfo 文件信息
     * @return 结果
     */
    public int insertFileInfo(SysFileInfo fileInfo);

    /**
     * 修改文件信息
     *
     * @param fileInfo 文件信息
     * @return 结果
     */
    public int updateFileInfo(SysFileInfo fileInfo);

    /**
     * 删除文件信息
     *
     * @param fileId 文件ID
     * @return 结果
     */
    public int deleteFileInfoById(Long fileId);

    /**
     * 批量删除文件信息
     *
     * @param fileIds 需要删除的文件ID
     * @return 结果
     */
    public int deleteFileInfoByIds(Long[] fileIds);

    /**
     * 根据业务类型和业务ID查询文件列表
     *
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 文件信息集合
     */
    public List<SysFileInfo> selectFileInfoByBusiness(String businessType, Long businessId);
}
