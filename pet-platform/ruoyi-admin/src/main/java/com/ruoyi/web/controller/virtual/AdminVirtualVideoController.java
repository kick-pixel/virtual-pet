package com.ruoyi.web.controller.virtual;

import java.util.List;
import java.util.Map;

import com.ruoyi.file.service.IFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.AiVideoTask;
import com.ruoyi.system.mapper.AiVideoTaskMapper;

/**
 * 管理后台 - 视频管理
 */
@RestController
@RequestMapping("/admin/virtual/video")
public class AdminVirtualVideoController extends BaseController {

    @Autowired
    private AiVideoTaskMapper aiVideoTaskMapper;
    @Autowired
    private IFileService fileService;

    /**
     * 视频列表
     */
    @PreAuthorize("@ss.hasPermi('virtual:video:list')")
    @GetMapping("/list")
    public TableDataInfo list(AiVideoTask query) {
        startPage();
        List<AiVideoTask> list = aiVideoTaskMapper.selectAdminVideoList(query);

        for (AiVideoTask task : list) {
            if (task.getFileId() != null) {
                try {
                    String url = fileService.getPresignedUrl(task.getFileId(), 7 * 24 * 60);
                    task.setOssVideoUrl(url);
                } catch (Exception e) {
                    logger.error("Failed to generate video presigned URL: {}", e.getMessage());
                }
            }
            if (task.getVideoPicFileId() != null) {
                try {
                    String url = fileService.getPresignedUrl(task.getVideoPicFileId(), 7 * 24 * 60);
                    task.setVideoPicUrl(url);
                } catch (Exception e) {
                    logger.error("Failed to generate video pic presigned URL: {}", e.getMessage());
                }
            }
            if (task.getPromptImageFileId() != null) {
                try {
                    String url = fileService.getPresignedUrl(task.getPromptImageFileId(), 7 * 24 * 60);
                    task.setPromptImageUrl(url);
                } catch (Exception e) {
                    logger.error("Failed to generate image presigned URL: {}", e.getMessage());
                }
            }
        }
        return getDataTable(list);
    }

    /**
     * 封禁/解封视频
     */
    @PreAuthorize("@ss.hasPermi('virtual:video:ban')")
    @Log(title = "视频管理-封禁/解封", businessType = BusinessType.UPDATE)
    @PutMapping("/{taskId}/ban")
    public AjaxResult updateAdminStatus(@PathVariable Long taskId,
                                        @RequestBody Map<String, Integer> body) {
        Integer adminStatus = body.get("adminStatus");
        if (adminStatus == null || (adminStatus != 0 && adminStatus != 1)) {
            return AjaxResult.error("参数错误：adminStatus 必须为 0 或 1");
        }
        aiVideoTaskMapper.updateAdminStatus(taskId, adminStatus);
        return AjaxResult.success();
    }
}
