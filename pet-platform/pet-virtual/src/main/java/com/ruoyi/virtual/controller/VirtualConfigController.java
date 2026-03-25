package com.ruoyi.virtual.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.file.service.IFileService;
import com.ruoyi.virtual.config.VirtualConfig;

@RestController
@RequestMapping("/api/virtual/config")
public class VirtualConfigController
{
    private static final Logger log = LoggerFactory.getLogger(VirtualConfigController.class);

    @Autowired
    private VirtualConfig virtualConfig;

    @Autowired(required = false)
    private IFileService fileService;

    @GetMapping("/splash")
    public AjaxResult getSplashVideo()
    {
        Long fileId = virtualConfig.getSplashFileId();
        if (fileId == null)
        {
            return AjaxResult.success(MessageUtils.message("virtual.config.splash.not.configured"), null);
        }

        if (fileService == null)
        {
            return AjaxResult.error(MessageUtils.message("virtual.config.file.service.not.configured"));
        }

        try
        {
            String url = fileService.getPresignedUrl(fileId, 60);
            return AjaxResult.success("ok", url);
        }
        catch (Exception e)
        {
            log.error("Failed to get splash video URL: fileId={}, error={}", fileId, e.getMessage());
            return AjaxResult.error(MessageUtils.message("virtual.config.splash.failed"));
        }
    }
}
