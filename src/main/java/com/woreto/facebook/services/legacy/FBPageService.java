package com.woreto.facebook.services.legacy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woreto.facebook.models.FBAccount;
import com.woreto.facebook.models.FBGroup;
import com.woreto.facebook.models.FBPage;
import com.woreto.facebook.models.FBPagePost;
import org.openqa.selenium.devtools.v131.network.Network;
import org.openqa.selenium.devtools.v131.network.model.Request;
import org.openqa.selenium.devtools.v131.network.model.RequestId;
import org.openqa.selenium.devtools.v131.network.model.ResponseReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

//@Service
public class FBPageService {

    public static final String PAGE_URL = "https://www.facebook.com/profile.php?id=";

    private static final Logger LOGGER = LoggerFactory.getLogger(FBPageService.class);

    @Autowired
    private FBBot fbBot;

    @Autowired
    private FBDaoService fbDaoService;

    @Autowired
    private FBGroupService fbGroupService;

    public void run() {
        List<FBPage> fbPages = fbDaoService.fetchAllPages();
        Map<String, List<FBPage>> fbPagesByManager =
                fbPages.stream().collect(Collectors.groupingBy(FBPage::getManagerId));
        List<FBPage> latestFbPagesToPost = new ArrayList<>();
        for (List<FBPage> fbPagesForEachManager : fbPagesByManager.values()) {
            latestFbPagesToPost.add(fbPagesForEachManager.get(0)); // pick page with oldest lastPosted
        }

        for (FBPage page : latestFbPagesToPost) {
            createAndSharePost(page);
        }
    }

    private boolean shouldPost(FBPage page) {
        Long diff = System.currentTimeMillis() - page.getLastPosted();
        return (diff > TimeUnit.HOURS.toMillis(1));
    }

    public void createAndSharePost(FBPage page) {
        try {
            FBAccount account = fbDaoService.fetchAccount(page.getManagerId());

            // publish post on page
            fbBot.run(account);
            createPost(page);

            // share published post on groups
            FBPagePost post = fbDaoService.fetchLatestPagePost();
            fbBot.run(account);
            for (String groupId : page.getGroupsToShare()) {
                FBGroup group = fbDaoService.fetchGroup(groupId);
                fbGroupService.shareWith(group, post, page);
            }
        } catch (Exception e) {
            LOGGER.error("Error publishing & sharing post for page {}", page.getId(), e);
        }
    }

    public void createPost(FBPage page) throws Exception {
        if (!shouldPost(page)) {
            return;
        }
        LOGGER.info("Creating post for {}", page.getName());
        FBPagePost publishedPost = publishPost(page, "/home/tinytrebuchet/Downloads/roshi-2.png");
        if (publishedPost != null) {
            fbDaoService.updateLastPosted(page);
        }
        LOGGER.error("Published post {} on page {}", publishedPost.getUrl(), page.getName());
    }

    private FBPagePost publishPost(FBPage page, String mediaPath) throws Exception {
        fbBot.navigateTo(PAGE_URL + page.getId(), 4);
        fbBot.findAndClickSpan("Switch Now", 4);
        fbBot.setCurrUser(page.getName()); // Switched to Page profile
        fbBot.findAndClickSpan("Photo/video", 4);
        fbBot.findAndUploadInput("Create post", mediaPath, 8);
        fbBot.findAndClickSpan("Next", 4);

        // attach listener to capture response details
        final CompletableFuture<FBPagePost> postFuture = new CompletableFuture<>();
        fbBot.attachListener(responseReceived -> {
            FBPagePost publishedPost = postPublishedListener(responseReceived);
            if (publishedPost != null) {
                postFuture.complete(publishedPost);
            }
        });
        fbBot.findAndClickSpan("Post", 8);
        fbBot.detachListeners();
        return postFuture.get(1, TimeUnit.SECONDS);
    }

    private FBPagePost postPublishedListener(ResponseReceived responseReceived) {
        String url = responseReceived.getResponse().getUrl();
        if (!url.contains("facebook.com/api/graphql")) {
            return null;
        }
        RequestId requestId = responseReceived.getRequestId();
        Request request = fbBot.getRequest(requestId);
        if (request == null) {
            LOGGER.warn("No request found for requestId {}", requestId);
            return null;
        }
        if (!("POST".equals(request.getMethod()) && request.getPostData().orElse("").contains("ComposerStoryCreateMutation"))) {
            return null;
        }

        Network.GetResponseBodyResponse response = fbBot.getResponseBody(requestId);
        if (response == null) {
            LOGGER.warn("No response found for requestId {}", requestId);
            return null;
        }
        try {
            FBPagePost post = scrapePublishedPost(response.getBody());
            fbDaoService.savePagePost(post);
            return post;
        } catch (Exception e) {
            LOGGER.error("Error parsing json response {}", response.getBody(), e);
        }
        return null;
    }

    private FBPagePost scrapePublishedPost(String responseStr) throws JsonProcessingException {
        JsonNode response = new ObjectMapper().readTree(responseStr);
        JsonNode story = response.get("data").get("story_create").get("story");
        String url = story.get("url").asText();
        String actorId = story.get("default_actor").get("id").asText();
        String storyId = story.get("legacy_story_hideable_id").asText();
        return new FBPagePost(storyId, actorId, url);
    }
}
