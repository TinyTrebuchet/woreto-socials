package com.woreto.facebook.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woreto.ChromeBot;
import com.woreto.facebook.models.FBPage;
import com.woreto.facebook.models.FBPagePost;
import com.woreto.pinterest.PinService;
import org.openqa.selenium.By;
import org.openqa.selenium.devtools.v131.network.Network;
import org.openqa.selenium.devtools.v131.network.model.Request;
import org.openqa.selenium.devtools.v131.network.model.RequestId;
import org.openqa.selenium.devtools.v131.network.model.ResponseReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class FBPageService {

    public static final String PAGE_URL = "https://www.facebook.com/profile.php?id=";

    private static final Logger LOGGER = LoggerFactory.getLogger(FBPageService.class);
    private final Random RANDOM = new Random();

    @Autowired
    private ChromeBot bot;

    @Autowired
    private FBDaoService fbDaoService;

    @Autowired
    private FBGroupService fbGroupService;

    @Autowired
    PinService pinService;

    public FBPagePost createPost(FBPage page) throws Exception {
        LOGGER.info("Creating post for {}", page.getName());
        FBPagePost publishedPost = publishPost(page, getMediaToPost(page));
        fbDaoService.updateLastPosted(page);
        LOGGER.info("Published post {} on page {}", publishedPost.getUrl(), page.getName());
        return publishedPost;
    }

    private String getMediaToPost(FBPage page) throws Exception {
        List<String> keywords = page.getKeywords();
        String keyword = keywords.get(RANDOM.nextInt(keywords.size()));
        return pinService.pickAndDownloadPin(keyword);
    }

    private FBPagePost publishPost(FBPage page, String mediaPath) throws Exception {
        bot.navigateTo(PAGE_URL + page.getId(), 4);
        bot.findAndClickSpan("Switch Now", 4);
        bot.currUser = page.getName(); // Switched to Page profile
        bot.findAndClickSpan("Photo/video", 3);
        bot.findAndType(By.xpath("//span[text()='Create post']/following::div[@role='textbox']"), "#dbz #anime #goku", 1);
        bot.findAndUpload(By.xpath("//span[text()='Create post']/following::input[1]"), mediaPath, 8);

        CompletableFuture<FBPagePost> postFuture;
        if (bot.findAndGet(By.xpath("//span[text()='Reel options']")) != null) {
            // reels
            bot.findAndClick(By.xpath("//span[text()='Reel options']/following::span[text()='Next'])"), 3);
            postFuture = attachListener();
            bot.findAndClick(By.xpath("//span[text()='Reel options']/following::span[text()='Publish'])"), 8);
        } else {
            // images
            bot.findAndClickSpan("Next", 4);
            postFuture = attachListener();
            bot.findAndClickSpan("Post", 12);
        }

        bot.detachListeners();
        return postFuture.get(1, TimeUnit.SECONDS);
    }

    private CompletableFuture<FBPagePost> attachListener() {
        final CompletableFuture<FBPagePost> postFuture = new CompletableFuture<>();
        bot.attachListener(responseReceived -> {
            FBPagePost publishedPost = postPublishedListener(responseReceived);
            if (publishedPost != null) {
                postFuture.complete(publishedPost);
            }
        });
        return postFuture;
    }

    private FBPagePost postPublishedListener(ResponseReceived responseReceived) {
        String url = responseReceived.getResponse().getUrl();
        if (!url.contains("facebook.com/api/graphql")) {
            return null;
        }
        RequestId requestId = responseReceived.getRequestId();
        Request request = bot.getRequest(requestId);
        if (request == null) {
            LOGGER.warn("No request found for requestId {}", requestId);
            return null;
        }
        if (!("POST".equals(request.getMethod()) && request.getPostData().orElse("").contains("ComposerStoryCreateMutation"))) {
            return null;
        }

        Network.GetResponseBodyResponse response = bot.getResponseBody(requestId);
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
