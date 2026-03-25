package com.ruoyi.virtual.security;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.http.UserAgentUtils;
import com.ruoyi.common.utils.ip.AddressUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * 虚拟宠物平台 Token 服务
 *
 * 与 RuoYi 框架的 TokenService 独立，使用单独的 Redis 键前缀 "virtual_login_tokens:"
 * 复用相同的 JWT secret，但 claims 中包含 "virtual_user_key" 以区分来源
 *
 * @author ruoyi
 */
@Component
public class VirtualTokenService
{
    private static final Logger log = LoggerFactory.getLogger(VirtualTokenService.class);

    /** JWT claims 中的键名，用于标识虚拟平台用户 */
    private static final String VIRTUAL_USER_KEY = "virtual_user_key";

    /** Redis 键前缀 */
    private static final String VIRTUAL_TOKEN_PREFIX = "virtual_login_tokens:";

    /** 20 分钟阈值：低于此值自动续期 */
    private static final Long MILLIS_MINUTE_TWENTY = 20 * 60 * 1000L;

    protected static final long MILLIS_SECOND = 1000;
    protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;

    @Value("${token.header}")
    private String header;

    @Value("${token.secret}")
    private String secret;

    @Value("${token.expireTime}")
    private int expireTime;

    @Autowired
    private RedisCache redisCache;

    /**
     * 从请求中获取虚拟平台登录用户
     */
    public VirtualLoginUser getLoginUser(HttpServletRequest request)
    {
        String token = getToken(request);
        if (StringUtils.isNotEmpty(token))
        {
            try
            {
                Claims claims = parseToken(token);
                String uuid = (String) claims.get(VIRTUAL_USER_KEY);
                if (StringUtils.isNotEmpty(uuid))
                {
                    String userKey = getTokenKey(uuid);
                    return redisCache.getCacheObject(userKey);
                }
            }
            catch (Exception e)
            {
                log.error("获取虚拟平台用户信息异常'{}'", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 创建令牌
     */
    public String createToken(VirtualLoginUser loginUser)
    {
        String uuid = IdUtils.fastUUID();
        loginUser.setToken(uuid);
        setUserAgent(loginUser);
        refreshToken(loginUser);

        Map<String, Object> claims = new HashMap<>();
        claims.put(VIRTUAL_USER_KEY, uuid);
        return createJwt(claims);
    }

    /**
     * 验证令牌有效期，不足 20 分钟自动续期
     */
    public void verifyToken(VirtualLoginUser loginUser)
    {
        long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= MILLIS_MINUTE_TWENTY)
        {
            refreshToken(loginUser);
        }
    }

    /**
     * 刷新令牌有效期
     */
    public void refreshToken(VirtualLoginUser loginUser)
    {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + expireTime * MILLIS_MINUTE);
        String userKey = getTokenKey(loginUser.getToken());
        redisCache.setCacheObject(userKey, loginUser, expireTime, TimeUnit.MINUTES);
    }

    /**
     * 删除用户身份信息
     */
    public void delLoginUser(String token)
    {
        if (StringUtils.isNotEmpty(token))
        {
            String userKey = getTokenKey(token);
            redisCache.deleteObject(userKey);
        }
    }

    /**
     * 设置用户身份信息
     */
    public void setLoginUser(VirtualLoginUser loginUser)
    {
        if (StringUtils.isNotNull(loginUser) && StringUtils.isNotEmpty(loginUser.getToken()))
        {
            refreshToken(loginUser);
        }
    }

    /**
     * 设置 UserAgent 信息
     */
    private void setUserAgent(VirtualLoginUser loginUser)
    {
        try
        {
            String userAgent = com.ruoyi.common.utils.ServletUtils.getRequest().getHeader("User-Agent");
            String ip = IpUtils.getIpAddr();
            loginUser.setIpaddr(ip);
            loginUser.setBrowser(UserAgentUtils.getBrowser(userAgent));
            loginUser.setOs(UserAgentUtils.getOperatingSystem(userAgent));
        }
        catch (Exception e)
        {
            // 非 HTTP 上下文时忽略
        }
    }

    /**
     * 生成 JWT
     */
    private String createJwt(Map<String, Object> claims)
    {
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 解析 JWT
     */
    private Claims parseToken(String token)
    {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从请求头获取 Token
     */
    private String getToken(HttpServletRequest request)
    {
        String token = request.getHeader(header);
        if (StringUtils.isNotEmpty(token) && token.startsWith("Bearer "))
        {
            token = token.substring(7);
        }
        return token;
    }

    /**
     * 生成 Redis 缓存键
     */
    private String getTokenKey(String uuid)
    {
        return VIRTUAL_TOKEN_PREFIX + uuid;
    }
}
