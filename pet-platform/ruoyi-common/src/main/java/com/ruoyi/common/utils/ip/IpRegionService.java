package com.ruoyi.common.utils.ip;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.lionsoul.ip2region.service.Config;
import org.lionsoul.ip2region.service.Ip2Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * IP 归属地查询服务
 *
 * 使用 ip2region 离线库（v3.3.5+），支持 IPv4 和 IPv6 双协议并发安全查询。
 * xdb 文件放置于 classpath:ip2region/ 目录下。
 */
@Component
public class IpRegionService {

    private static final Logger log = LoggerFactory.getLogger(IpRegionService.class);

    private static final String V4_XDB = "ip2region/ip2region_v4.xdb";
    private static final String V6_XDB = "ip2region/ip2region_v6.xdb";

    private Ip2Region ip2Region;

    @PostConstruct
    public void init() {
        try {
            Config v4Config = loadConfig(V4_XDB, true);
            Config v6Config = loadConfig(V6_XDB, false);

            if (v4Config == null && v6Config == null) {
                log.warn("ip2region: 未找到任何 xdb 文件，IP 归属地查询已禁用");
                return;
            }

            ip2Region = Ip2Region.create(v4Config, v6Config);
            log.info("ip2region 服务初始化成功 (v4={}, v6={})", v4Config != null, v6Config != null);
        } catch (Exception e) {
            log.error("ip2region 服务初始化失败: {}", e.getMessage());
        }
    }

    private Config loadConfig(String classpathPath, boolean isV4) {
        ClassPathResource resource = new ClassPathResource(classpathPath);
        if (!resource.exists()) {
            log.warn("ip2region: {} 不存在，跳过", classpathPath);
            return null;
        }
        try {
            // 先读入 byte[]，再包装为 ByteArrayInputStream（JAR 内资源也可用）
            final byte[] content;
            try (InputStream is = resource.getInputStream()) {
                content = is.readAllBytes();
            }
            // setXdbInputStream 仅支持 BufferCache；全链式调用，避免 ConfigBuilder 中间类型
            if (isV4) {
                return Config.custom()
                        .setCachePolicy(Config.BufferCache)
                        .setXdbInputStream(new ByteArrayInputStream(content))
                        .asV4();
            } else {
                return Config.custom()
                        .setCachePolicy(Config.BufferCache)
                        .setXdbInputStream(new ByteArrayInputStream(content))
                        .asV6();
            }
        } catch (Exception e) {
            log.error("ip2region: 加载 {} 失败: {}", classpathPath, e.getMessage());
            return null;
        }
    }

    @PreDestroy
    public void close() {
        if (ip2Region != null) {
            try {
                ip2Region.close();
            } catch (Exception e) {
                log.warn("ip2region: 关闭服务失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 根据 IP 地址查询归属地
     *
     * <p>返回格式：国家-省份-城市（忽略 "0" 和空值）
     * <br>例如：{@code 中国-广东省-深圳市} / {@code United States-California-San Jose}
     *
     * @param ip IPv4 或 IPv6 地址字符串
     * @return 归属地字符串，查询失败或服务未初始化返回 {@code null}
     */
    public String getRegion(String ip) {
        if (ip == null || ip.isBlank() || ip2Region == null) {
            return null;
        }
        try {
            String raw = ip2Region.search(ip);
            return formatRegion(raw);
        } catch (Exception e) {
            log.debug("ip2region 查询失败 ip={}: {}", ip, e.getMessage());
            return null;
        }
    }

    /**
     * 格式化原始 region 字符串
     *
     * <p>原始格式：{@code country|province|city|ISP|countryCode}
     * <br>取前三个非 "0" 非空字段，用 "-" 拼接
     */
    private static String formatRegion(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String[] parts = raw.split("\\|");
        StringBuilder sb = new StringBuilder();
        // 只取 country, province, city（索引 0-2）
        for (int i = 0; i < Math.min(3, parts.length); i++) {
            String p = parts[i].trim();
            if (!p.isEmpty() && !"0".equals(p)) {
                if (sb.length() > 0) {
                    sb.append('-');
                }
                sb.append(p);
            }
        }
        String result = sb.toString();
        return result.isEmpty() ? null : result;
    }
}
