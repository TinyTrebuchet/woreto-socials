package com.woreto.facebook.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woreto.ChromeBot;
import com.woreto.facebook.models.FBPage;
import com.woreto.facebook.models.FBPagePost;
import com.woreto.pinterest.PinService;
import org.openqa.selenium.By;
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
import java.util.function.Function;

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
        bot.findAndClickSpan("Photo/video", 2);
        bot.findAndType(By.xpath("//span[text()='Create post']/following::div[@role='textbox']"), "#dbz #anime #goku", 1);
        bot.findAndUpload(By.xpath("//span[text()='Create post']/following::input[1]"), mediaPath, 8);

        CompletableFuture<FBPagePost> postFuture;
        if (bot.findAndGet(By.xpath("//span[text()='Reel options']")) != null) {
            // reels
            bot.findAndClick(By.xpath("//span[text()='Previous']/following::span[text()='Next']"), 2);
            postFuture = attachListener(this::reelPublishedListener);
            bot.findAndClick(By.xpath("//span[text()='Previous']/following::span[text()='Publish']"), 8);
        } else {
            // images
            bot.findAndClickSpan("Next", 4);
            postFuture = attachListener(this::postPublishedListener);
            bot.findAndClickSpan("Post", 12);
        }

        bot.detachListeners();
        FBPagePost post = postFuture.get(1, TimeUnit.SECONDS);
        post.setActorId(page.getId());
        post.setUrl(getPagePostShareUrl(page.getId(), post.getStoryId()));
        fbDaoService.savePagePost(post);
        return post;
    }

    private CompletableFuture<FBPagePost> attachListener(Function<ResponseReceived, FBPagePost> listener) {
        final CompletableFuture<FBPagePost> postFuture = new CompletableFuture<>();
        bot.attachListener(responseReceived -> {
            FBPagePost publishedPost = listener.apply(responseReceived);
            if (publishedPost != null) {
                postFuture.complete(publishedPost);
            }
        });
        return postFuture;
    }

    private FBPagePost reelPublishedListener(ResponseReceived responseReceived) {
        try {
            String responseStr = filterAndFetchPostPublishedResponse(responseReceived);
            if (responseStr == null) {
                return null;
            }
            JsonNode response = new ObjectMapper().readTree(responseStr);
            String postId = response.get("data").get("story_create").get("post_id").asText();
            return new FBPagePost(postId, FBPagePost.Type.REEL);
        } catch (Exception e) {
            LOGGER.error("Error with published reel listener", e);
            return null;
        }
    }

    private FBPagePost postPublishedListener(ResponseReceived responseReceived) {
        try {
            String responseStr = filterAndFetchPostPublishedResponse(responseReceived);
            if (responseStr == null) {
                return null;
            }
            JsonNode response = new ObjectMapper().readTree(responseStr);
            JsonNode story = response.get("data").get("story_create").get("story");
            String storyId = story.get("legacy_story_hideable_id").asText();
            return new FBPagePost(storyId, FBPagePost.Type.IMAGE);
        } catch (Exception e) {
            LOGGER.error("Error with published post listener");
            return null;
        }
    }

    private String filterAndFetchPostPublishedResponse(ResponseReceived responseReceived) {
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

        return bot.getResponseBody(requestId).getBody();
    }

    private String getPagePostShareUrl(String pageId, String postId) {
        return "https://www.facebook.com/permalink.php?story_fbid=" + postId + "&id=" + pageId;
    }
}
