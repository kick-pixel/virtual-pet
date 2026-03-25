package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.SysFileInfo;
import com.ruoyi.system.mapper.SysFileInfoMapper;
import com.ruoyi.system.service.ISysFileInfoService;

/**
 * 文件信息 服务层实现
 *
 * @author ruoyi
 */
@Service
public class SysFileInfoServiceImpl implements ISysFileInfoService
{
    @Autowired
    private SysFileInfoMapper fileInfoMapper;

    /**
     * 查询文件信息
     *
     * @param fileId 文件ID
     * @return 文件信息
     */
    @Override
    public SysFileInfo selectFileInfoById(Long fileId)
    {
        return fileInfoMapper.selectFileInfoById(fileId);
    }

    /**
     * 根据文件Key查询文件信息
     *
     * @param fileKey 文件Key
     * @return 文件信息
     */
    @Override
    public SysFileInfo selectFileInfoByKey(String fileKey)
    {
        return fileInfoMapper.selectFileInfoByKey(fileKey);
    }

    /**
     * 查询文件信息列表
     *
     * @param fileInfo 文件信息
     * @return 文件信息集合
     */
    @Override
    public List<SysFileInfo> selectFileInfoList(SysFileInfo fileInfo)
    {
        return fileInfoMapper.selectFileInfoList(fileInfo);
    }

    /**
     * 根据业务类型和业务ID查询文件列表
     *
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 文件信息集合
     */
    @Override
    public List<SysFileInfo> selectFileInfoByBusiness(String businessType, Long businessId)
    {
        return fileInfoMapper.selectFileInfoByBusiness(businessType, businessId);
    }

    /**
     * 新增文件信息
     *
     * @param fileInfo 文件信息
     * @return 结果
     */
    @Override
    public int insertFileInfo(SysFileInfo fileInfo)
    {
        return fileInfoMapper.insertFileInfo(fileInfo);
    }

    /**
     * 修改文件信息
     *
     * @param fileInfo 文件信息
     * @return 结果
     */
    @Override
    public int updateFileInfo(SysFileInfo fileInfo)
    {
        return fileInfoMapper.updateFileInfo(fileInfo);
    }

    /**
     * 删除文件信息
     *
     * @param fileId 文件ID
     * @return 结果
     */
    @Override
    public int deleteFileInfoById(Long fileId)
    {
        return fileInfoMapper.deleteFileInfoById(fileId);
    }

    /**
     * 批量删除文件信息
     *
     * @param fileIds 需要删除的文件ID
     * @return 结果
     */
    @Override
    public int deleteFileInfoByIds(Long[] fileIds)
    {
        return fileInfoMapper.deleteFileInfoByIds(fileIds);
    }
}
