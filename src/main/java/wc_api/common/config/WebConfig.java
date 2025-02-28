package wc_api.common.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.config.file.path}")
    private String uploadPath;

    @PostConstruct
    public void init() {
        System.out.println("=== WebConfig Initialization ===");
        System.out.println("Upload Path: " + uploadPath);
        File directory = new File(uploadPath);
        System.out.println("Directory exists: " + directory.exists());
        System.out.println("Directory absolute path: " + directory.getAbsolutePath());
        System.out.println("Directory readable: " + directory.canRead());

        // 실제 파일 목록도 출력
        if (directory.exists()) {
            System.out.println("Files in directory:");
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    System.out.println("File: " + file.getName());
                    System.out.println("Absolute path: " + file.getAbsolutePath());
                    System.out.println("Readable: " + file.canRead());
                }
            }
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 경로가 항상 /로 끝나도록 보장
        String resourceLocation = uploadPath.endsWith("/") ?
                "file:" + uploadPath : "file:" + uploadPath + "/";

        System.out.println("Resource location: " + resourceLocation);

        registry.addResourceHandler("/images/**")
                .addResourceLocations(resourceLocation)
                .setCacheControl(CacheControl.noCache())
                .resourceChain(true);
    }
}