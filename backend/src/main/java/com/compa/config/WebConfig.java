package com.compa.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.compa.config.EstudianteAccessInterceptor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final EstudianteAccessInterceptor estudianteAccessInterceptor;
    private final MenorAutorizacionInterceptor menorAutorizacionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(estudianteAccessInterceptor).addPathPatterns("/api/**");
        registry.addInterceptor(menorAutorizacionInterceptor).addPathPatterns("/api/**");
    }
}