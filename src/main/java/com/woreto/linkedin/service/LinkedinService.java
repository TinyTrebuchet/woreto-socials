package com.woreto.linkedin.service;

import com.woreto.ChromeBot;
import com.woreto.linkedin.models.LinkedinAccount;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LinkedinService {

    @Autowired
    ChromeBot bot;

    @Autowired
    LinkedinDaoService linkedinDaoService;

    private static final String BASE_URL = "https://www.linkedin.com";
    private static final String LOGIN_PAGE = BASE_URL + "/login/";
    private static final String GROUPS_PAGE = BASE_URL + "/groups/";
    private static final Logger LOGGER = LoggerFactory.getLogger(LinkedinService.class);

    public void createPosts(String content, String mediaPath, String emailId, List<String> groupIds) throws Exception {
        loginIfNeeded(emailId);
        for (String groupId : groupIds) {
            try {
                createPostForGroup(content, mediaPath, groupId);
            } catch (Exception e) {
                LOGGER.error("Error creating post for group {}", groupId, e);
            }
        }
    }

    private void createPostForGroup(String content, String mediaPath, String groupId) throws Exception {
        bot.navigateTo(GROUPS_PAGE + groupId, 3);
        bot.findAndClickSpan("Start a public post", 2);
        bot.findAndType(By.xpath("//div[@contenteditable='true']"), content, 3);
        bot.findAndClickJs(By.xpath("//button[@aria-label='Add media']"), 2);
        bot.findAndUpload(By.name("file"), mediaPath, 4);
        bot.findAndClickSpan("Next", 2);
        bot.findAndClickSpan("Post", 6);
        LOGGER.info("Successfully posted to group {}", groupId);
    }

    private void loginIfNeeded(String emailId) throws Exception {
        String currUser = getLoggedInUser();
        LinkedinAccount account = linkedinDaoService.fetchAccount(emailId);
        if (currUser != null && currUser.equals(account.getFullName())) {
            LOGGER.info("Already logged in as {}", currUser);
            return;
        }
        if (currUser != null) {
            LOGGER.info("Currently logged in as {}, switching to {}", currUser, account.getFullName());
            logout();
        }
        login(account);
    }

    private void login(LinkedinAccount account) throws Exception {
        bot.navigateTo(LOGIN_PAGE, 3);
        bot.findAndType(By.id("username"), account.getEmailId(), 3);
        bot.findAndType(By.id("password"), account.getPassword(), 3);
        bot.findAndClick(By.xpath("//button[@aria-label='Sign in']"), 6);
        bot.saveCookies();
        LOGGER.info("Logged in with {}", account.getEmailId());
    }

    private void logout() throws Exception {
        bot.navigateTo("https://www.linkedin.com/m/logout/", 4);
    }

    private String getLoggedInUser() throws Exception {
        bot.navigateTo(BASE_URL, 2);
        bot.restoreCookies();
        bot.navigateTo(BASE_URL, 4);
        try {
            return bot.findAndGet(By.className("profile-card-name"));
        } catch (NoSuchElementException e) {
            return null;
        }
    }
}
