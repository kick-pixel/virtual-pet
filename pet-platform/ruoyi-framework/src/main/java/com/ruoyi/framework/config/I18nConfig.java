package com.ruoyi.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import com.ruoyi.common.constant.Constants;

/**
 * 资源文件配置加载
 * 使用 AcceptHeaderLocaleResolver，从请求头 Accept-Language 读取语言，适合无状态 REST API。
 *
 * @author ruoyi
 */
@Configuration
public class I18nConfig
{
    @Bean
    public LocaleResolver localeResolver()
    {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Constants.DEFAULT_LOCALE);
        return resolver;
    }
}
