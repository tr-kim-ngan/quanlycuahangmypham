package com.kimngan.ComesticAdmin;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mapping đường dẫn URL /upload/ tới thư mục chứa file ảnh
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:src/main/resources/static/upload/");
        //.addResourceLocations("classpath:/static/upload/");
    }
}
