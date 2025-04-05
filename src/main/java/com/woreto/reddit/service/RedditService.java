package com.woreto.reddit.service;

import com.woreto.ChromeBot;
import com.woreto.reddit.models.RedditAccount;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class RedditService {

    private static final String BASE_URL = "https://www.reddit.com";
    private static final String ACCOUNT_SETTINGS = "https://www.reddit.com/settings/account";
    private static final String CROSSPOST_URL = "https://www.reddit.com/r/%s/submit/?source_id=t3_%s";

    private static final Logger LOGGER = LoggerFactory.getLogger(RedditService.class);

    @Autowired
    ChromeBot bot;

    @Autowired
    RedditDaoService redditDaoService;

    public void crossPost(String postLink, String emailId, List<String> targetSubs) throws Exception {
        loginIfNeeded(emailId);
        String postId = getPostId(postLink);
        for (String targetSub : targetSubs) {
            String crossPostUrl = String.format(CROSSPOST_URL, targetSub, postId);
            bot.navigateTo(crossPostUrl, 4);
            bot.findAndClick(By.id("submit-post-button"), 6);
            LOGGER.info("Shared post {} with subreddit {}", postId, targetSub);
        }
    }

    private void loginIfNeeded(String emailId) throws Exception {
        bot.navigateTo(BASE_URL, 3);
        bot.restoreCookies();
        String currentEmailId = getCurrentLoggedInUser();
        if (emailId.equals(currentEmailId)) {
            LOGGER.info("Already logged in with {}", emailId);
            return;
        }
        if (currentEmailId != null) {
            LOGGER.info("Logged in with {}, switching to {}", currentEmailId, emailId);
            logout();
        }
        login(emailId);
    }

    private void login(String emailId) throws Exception {
        RedditAccount account = redditDaoService.fetchAccount(emailId);
        bot.navigateTo(BASE_URL, 3);
        bot.findAndClickSpan("Log In", 3);
        bot.findAndType(By.name("username"), emailId, 2);
        bot.findAndType(By.name("password"), account.getPassword(), 2);
        bot.findAndClick(By.xpath("//auth-flow-manager//span[text()='Log In']"), 6);
        bot.saveCookies();
    }

    private void logout() throws Exception {
        bot.findAndClick(By.xpath("//activate-feature[@name='UserDrawerMenu_SZvQJM']//button"), 3);
        bot.findAndClickSpan("Log Out", 3);
    }

    private String getCurrentLoggedInUser() throws Exception {
        bot.navigateTo(ACCOUNT_SETTINGS, 4);
        String currentUrl = bot.driver.getCurrentUrl();
        if (currentUrl == null || currentUrl.contains("reddit.com/login")) {
            return null;
        }
        return bot.findAndGet(By.xpath("//div[@data-testid='email']//label//span[contains(@class, 'truncate')]"));
    }

    private String getPostId(String postLink) {
        List<String> parts = Arrays.asList(postLink.split("/"));
        if (!parts.contains("comments")) {
            throw new RuntimeException("Invalid post link: " + postLink);
        }
        return parts.get(parts.indexOf("comments") + 1);
    }
}
