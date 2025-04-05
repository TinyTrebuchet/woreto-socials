package com.woreto.facebook.services.legacy;

import com.woreto.facebook.models.FBGroup;
import com.woreto.facebook.models.FBPage;
import com.woreto.facebook.models.FBPagePost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//@Service
public class FBGroupService {

    public static final String GROUP_URL = "https://www.facebook.com/groups/";

    private static final Logger LOGGER = LoggerFactory.getLogger(FBGroupService.class);

    @Autowired
    private FBDaoService fbDaoService;

    @Autowired
    private FBBot fbBot;

    public void run(FBBot fbBot) {
    }

    public void shareWith(FBGroup group, FBPagePost post, FBPage page) throws Exception {
        fbBot.navigateTo(post.getUrl(), 4);

        // scroll modal to bottom
//        List<WebElement> scrollableElements = fbBot.findScrollableElements();
//        scrollableElements.getLast().sendKeys(Keys.END);
//        Thread.sleep(1000);

        fbBot.findAndClickBySelector("//span[text()=\"" + page.getName() + "'s Post\"]"
                + "/following::span[text()='Share']", 3);
        fbBot.findAndClickSpan("Group", 3);
        fbBot.findAndTypeBySelector("//input[@placeholder='Search for groups']", group.getName(), 3);
        fbBot.findAndClickBySelector("//span[text()='Share to a group']"
                + "/following::span[text()='" + group.getName() + "']", 4);
        fbBot.findAndClickSpan("Post", 8);
        LOGGER.info("Shared post {} with group {}", post.getStoryId(), group.getId());
    }
}
