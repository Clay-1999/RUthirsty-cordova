package com.surveillance;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Video Surveillance Platform Main Application
 *
 * @author Video Surveillance Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
@MapperScan("com.surveillance.dao.mapper")
public class VideoSurveillanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VideoSurveillanceApplication.class, args);
        System.out.println("""

                ========================================
                Video Surveillance Platform Started
                ========================================
                """);
    }
}
