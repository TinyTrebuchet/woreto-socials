package com.woreto.linkedin;

import com.woreto.linkedin.service.LinkedinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication(scanBasePackages = {"com.woreto"})
public class LinkedinMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(LinkedinMain.class);

    public static void main(String[] args) {
        String post =
"""
Hello World.
This is a multi line post;
""";

        String mediaPath = "/home/tinytrebuchet/Downloads/akira-toriyama.jpg";
        String emailId = "gaurav.guleria2907@gmail.com";
        List<String> groupIds = List.of("14633050");

        ApplicationContext applicationContext = SpringApplication.run(LinkedinMain.class, args);
        LinkedinService linkedinService = applicationContext.getBean(LinkedinService.class);
        try {
            linkedinService.createPosts(post, mediaPath, emailId, groupIds);
        } catch (Exception e) {
            LOGGER.error("Error creating posts", e);
        }
        System.exit(0);
    }
}
