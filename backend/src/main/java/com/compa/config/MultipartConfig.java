package com.compa.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class MultipartConfig {

    // Límite de subida: el servicio acepta máx. 5 MB, aquí damos un poco más de margen
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(6));
        factory.setMaxRequestSize(DataSize.ofMegabytes(6));
        return factory.createMultipartConfig();
    }
}