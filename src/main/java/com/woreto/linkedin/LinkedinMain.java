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
        if (args.length < 4) {
            LOGGER.error("Args: <content> <media_path> <emailId> <groupId1> [<groupId2> ...]");
            return;
        }
        ApplicationContext applicationContext = SpringApplication.run(LinkedinMain.class, args);
        LinkedinService linkedinService = applicationContext.getBean(LinkedinService.class);
        List<String> targetGroupIds = Arrays.asList(args).subList(3, args.length);
        try {
            linkedinService.createPosts(args[0], args[1], args[2], targetGroupIds);
        } catch (Exception e) {
            LOGGER.error("Error creating posts", e);
        }
    }
}
