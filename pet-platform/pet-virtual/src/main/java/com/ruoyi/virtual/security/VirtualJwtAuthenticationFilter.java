package com.ruoyi.virtual.security;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.ruoyi.common.utils.StringUtils;

/**
 * 虚拟宠物平台 JWT 认证过滤器
 *
 * 仅处理 /api/virtual/** 路径的请求
 * 与 RuoYi 框架的 JwtAuthenticationTokenFilter 共存，互不干扰
 *
 * @author ruoyi
 */
@Component
public class VirtualJwtAuthenticationFilter extends OncePerRequestFilter
{
    @Autowired
    private VirtualTokenService virtualTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException
    {
        // 仅处理虚拟平台路径
        String requestUri = request.getRequestURI();
        if (requestUri.startsWith("/api/virtual/"))
        {
            VirtualLoginUser loginUser = virtualTokenService.getLoginUser(request);
            if (StringUtils.isNotNull(loginUser) && SecurityContextHolder.getContext().getAuthentication() == null)
            {
                virtualTokenService.verifyToken(loginUser);
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        chain.doFilter(request, response);
    }
}
