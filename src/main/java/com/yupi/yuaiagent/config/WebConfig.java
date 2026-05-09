package com.yupi.yuaiagent.config;

import com.yupi.yuaiagent.Service.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/**", "/error")
                .excludePathPatterns(
                        "/auth/**",
                        "/error",
                        "/api/doc.html",           // Knife4j 文档页面
                        "/doc.html",               // 有时路径直接是 /doc.html
                        "/swagger-ui/**",          // Swagger UI 静态资源
                        "/swagger-resources/**",   // Swagger 资源
                        "/v3/api-docs/**",         // OpenAPI 规范接口
                        "/webjars/**"              // 文档依赖的前端资源
                );
    }
}