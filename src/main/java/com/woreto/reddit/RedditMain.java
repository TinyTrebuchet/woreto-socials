package com.woreto.reddit;

import com.woreto.reddit.service.RedditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication(scanBasePackages = {"com.woreto"})
public class RedditMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedditMain.class);

    public static void main(String[] args) {
        if (args.length < 3) {
            LOGGER.error("Args: <post_link> <emailId> <subreddit1> [<subreddit2> ...]");
            return;
        }
        ApplicationContext applicationContext = SpringApplication.run(RedditMain.class, args);
        RedditService redditService = applicationContext.getBean(RedditService.class);
        List<String> targetSubs = Arrays.asList(args).subList(2, args.length);
        try {
            redditService.crossPost(args[0], args[1], targetSubs);
        } catch (Exception e) {
            LOGGER.error("Error sharing post", e);
        }
    }
}
