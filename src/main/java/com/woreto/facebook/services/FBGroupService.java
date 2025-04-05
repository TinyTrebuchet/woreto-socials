package com.woreto.facebook.services;

import com.woreto.ChromeBot;
import com.woreto.facebook.models.FBGroup;
import com.woreto.facebook.models.FBPage;
import com.woreto.facebook.models.FBPagePost;

import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FBGroupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FBGroupService.class);

    @Autowired
    FBDaoService fbDaoService;

    @Autowired
    ChromeBot bot;

    public void shareWith(FBGroup group, FBPagePost post, FBPage page) throws Exception {
        bot.navigateTo(post.getUrl(), 4);
        bot.findAndClick(By.xpath("//span[text()=\"" + page.getName() + "'s Post\"]"
                + "/following::span[text()='Share']"), 3);
        bot.findAndClickSpan("Group", 3);
        bot.findAndType(By.xpath("//input[@placeholder='Search for groups']"), group.getName(), 3);
        bot.findAndClick(By.xpath("//span[text()='Share to a group']"
                + "/following::span[text()='" + group.getName() + "']"), 4);
        bot.findAndClickSpan("Post", 8);
        fbDaoService.updateLastPosted(group);
        LOGGER.info("Shared post {} with group {}", post.getStoryId(), group.getId());
    }
}
