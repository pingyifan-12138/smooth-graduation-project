package com.manage.club.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 配置上传文件的访问路径
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${upload.base-path}")
    private String basePath;
    @Value("${upload.access-path}")
    private String accessPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射/uploads/**路径到本地文件目录
        System.out.println("静态资源映射: " + accessPath + " -> file:" + basePath);
        registry.addResourceHandler(accessPath)
                .addResourceLocations("file:" + basePath);
    }

}