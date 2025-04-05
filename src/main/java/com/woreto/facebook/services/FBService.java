package com.woreto.facebook.services;

import com.woreto.ChromeBot;
import com.woreto.facebook.models.FBAccount;
import com.woreto.facebook.models.FBGroup;
import com.woreto.facebook.models.FBPage;
import com.woreto.facebook.models.FBPagePost;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class FBService {

    private static final String BASE_URL = "https://www.facebook.com/";
    private static final Logger LOGGER = LoggerFactory.getLogger(FBService.class);

    @Autowired
    ChromeBot bot;

    @Autowired
    private FBDaoService fbDaoService;

    @Autowired
    private FBGroupService fbGroupService;

    @Autowired
    private FBPageService fbPageService;

    public void run() throws Exception {
        bot.navigateTo(BASE_URL, 1);

        List<FBPage> fbPages = fbDaoService.fetchAllPages();
        fbPages.sort(Comparator.comparing(FBPage::getManagerId));

        // publish posts
        Map<FBPage, FBPagePost> publishedPosts = new HashMap<>();
        for (FBPage page : fbPages) {
            FBPagePost post = createPost(page);
            if (post != null) {
                publishedPosts.put(page, post);
            }
        }

        // share to groups
        for (Map.Entry<FBPage, FBPagePost> entry : publishedPosts.entrySet()) {
            FBPage page = entry.getKey();
            FBPagePost post = entry.getValue();
            for (String groupId : page.getGroupsToShare()) {
                FBGroup group = fbDaoService.fetchGroup(groupId);
                sharePost(group, page, post);
            }
        }
    }

    private FBPagePost createPost(FBPage page) {
        try {
            if (shouldPost(page.getLastPosted(), TimeUnit.HOURS.toMillis(1))) {
                loginIfNeeded(fbDaoService.fetchAccount(page.getManagerId()));
                return fbPageService.createPost(page);
            }
        } catch (Exception e) {
            LOGGER.error("Error publishing & sharing post for page {}", page.getId(), e);
        }
        return null;
    }

    private void sharePost(FBGroup group, FBPage page, FBPagePost post) {
        try {
            if (shouldPost(group.getLastPosted(), TimeUnit.MINUTES.toMillis(10))) {
                loginIfNeeded(fbDaoService.fetchAccount(group.getManagerId()));
                fbGroupService.shareWith(group, post, page);
            }
        } catch (Exception e) {
            LOGGER.error("Error publishing & sharing post for page {}", page.getId(), e);
        }
    }

    private boolean shouldPost(Long lastPosted, Long cooldown) {
        return true;
//        long diff = System.currentTimeMillis() - lastPosted;
//        return (diff > cooldown);
    }

    private void loginIfNeeded(FBAccount account) throws Exception {
        if (account.getFullName().equals(bot.currUser)) {
            return;
        }
        if (bot.currUser != null) {
            switchAccount(account);
        } else {
            loginAccount(account);
        }
    }

    private void loginAccount(FBAccount account) throws Exception {
        if (bot.restoreCookies("FACEBOOK:" + account.getEmailId(), BASE_URL)) {
            bot.currUser = getLoggedInUser();
            if (account.getFullName().equals(bot.currUser)) {
                return;
            }
        }
        if (bot.currUser != null) {
            logout();
        }
        bot.findAndType(By.id("email"), account.getEmailId(), 1);
        bot.findAndType(By.id("pass"), account.getPassword(), 1);
        bot.findAndClick(By.name("login"), 4);
        bot.currUser = getLoggedInUser();
        bot.saveCookies("FACEBOOK:" + account.getEmailId());
        LOGGER.info("Logged in with user {}", bot.currUser);
    }

    private void switchAccount(FBAccount account) throws Exception {
        bot.navigateTo(BASE_URL, 4);
        bot.findAndClick(By.xpath("//div[@aria-label='Your profile']"), 2);
        try {
            bot.findAndClickSpan(account.getFullName(), 2);
            bot.currUser = getLoggedInUser();
            LOGGER.info("Switched to user {}", bot.currUser);
        } catch (Exception e) {
            LOGGER.error("Couldn't switch to profile {} from current context {}", account.getFullName(), bot.currUser);
            loginAccount(account);
        }
    }

    private void logout() throws Exception {
        bot.findAndClick(By.xpath("//div[@aria-label='Your profile']"), 2);
        bot.findAndClickSpan("Log out", 4);
        bot.currUser = null;
    }

    private String getLoggedInUser() throws Exception {
        bot.navigateTo(BASE_URL, 4);
        try {
            // e.g., "What's on your mind, Gaurav?"
            String text = bot.findAndGet(By.xpath("//span[contains(text(), \"What's on your mind\")]"));
            String firstName =  text.substring(text.indexOf(",") + 2, text.length() - 1); // get the name
            return bot.findAndGet(By.xpath("//span[contains(text(), \"" + firstName + "\")]"));
        } catch (NoSuchElementException e) {
            return null; // Not logged in
        }
    }
}
