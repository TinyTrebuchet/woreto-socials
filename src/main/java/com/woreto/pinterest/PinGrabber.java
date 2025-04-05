package com.woreto.pinterest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woreto.RetryService;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v131.network.Network;
import org.openqa.selenium.devtools.v131.network.model.Request;
import org.openqa.selenium.devtools.v131.network.model.ResponseReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PinGrabber {

    private static final Logger LOGGER = LoggerFactory.getLogger(PinGrabber.class);

    @Autowired
    private PinDAOService pinDAOService;

    private ChromeDriver driver;
    private Map<String, PinterestPin> scrapedPins;

    public void run(String[] args) throws InterruptedException {
        final String query, keyword;
        if (args.length > 0) {
            query = String.join("%20", args);
            keyword = String.join(" ", args);
        } else {
            throw new RuntimeException("No keywords supplied");
        }
        String pinterestURL = "https://www.pinterest.com/search/pins/?q=" + query;

        scrapedPins = new ConcurrentHashMap<>();
        setupChromeDriver();
        driver.get(pinterestURL);
        Thread.sleep(5000);

        // Scroll, capture & scrape AJAX calls
        int scrolls = 40;
        for (int i = 0; i < scrolls; i++) {
            driver.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(1000); // Pause to allow AJAX loading
        }
        Thread.sleep(3000);

        LOGGER.info("Scraped {} pins!!", scrapedPins.size());
        scrapedPins.values().forEach(pin -> pin.setKeyword(keyword));
        pinDAOService.bulkSave(scrapedPins.values());

        driver.quit();
    }

    private void setupChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        driver = new ChromeDriver(options);

        Map<String, Request> requestMap = new HashMap<>();

        // Enable Network tracking
        DevTools devTools = driver.getDevTools();
        devTools.createSession();
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
        devTools.addListener(Network.requestWillBeSent(), requestToSend -> {
            requestMap.put(requestToSend.getRequestId().toString(), requestToSend.getRequest());
        });
        devTools.addListener(Network.responseReceived(),
                responseReceived -> captureAJAXResponse(responseReceived, requestMap));
    }

    private void captureAJAXResponse(ResponseReceived responseReceived, Map<String, Request> requestMap) {
        String url = responseReceived.getResponse().getUrl();
        if (!url.contains("pinterest.com/resource/BaseSearchResource/get/")) {
            return;
        }
        Request request = requestMap.get(responseReceived.getRequestId().toString());
        if (request == null) {
            LOGGER.warn("No request found for requestId {}", responseReceived.getRequestId());
            return;
        }
        if (("GET".equals(request.getMethod()) && !url.contains("appliedProductFilters"))
            || ("POST".equals(request.getMethod()) && !request.getPostData().orElse("").contains("appliedProductFilters"))) {
            return;
        }

        Network.GetResponseBodyResponse response;
        try {
            response = RetryService.executeWithRetry(
                    () -> driver.getDevTools().send(Network.getResponseBody(responseReceived.getRequestId()))
            );
        } catch (Exception e) {
            LOGGER.error("No response body found for requestId {}", responseReceived.getRequestId());
            return;
        }

        try {
            List<PinterestPin> pins = scrapePinMetadata(response.getBody());
            pins.forEach(pin -> scrapedPins.put(pin.getId(), pin));
        } catch (Exception e) {
            LOGGER.error("Error capturing AJAX response", e);
        }
    }

    private List<PinterestPin> scrapePinMetadata(String responseStr) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode response = objectMapper.readTree(responseStr);

        List<PinterestPin> pins = new ArrayList<>();
        JsonNode results = response.get("resource_response").get("data").get("results");
        for (JsonNode item : results) {
            String id = item.get("id").asText();
            try {
                JsonNode origImage = item.get("images").get("orig");

                PinterestPin pin = new PinterestPin(
                        id,
                        null,
                        getMediaType(item),
                        origImage.get("url").asText(),
                        origImage.get("width").asInt(),
                        origImage.get("height").asInt(),
                        item.get("title").asText(),
                        item.get("description").asText(),
                        item.get("auto_alt_text").asText(),
                        getCumulativeReactionCount(item.get("reaction_counts")),
                        item.get("pinner").get("follower_count").asInt(),
                        item.get("created_at").asText(),
                        false
                );
                pins.add(pin);
            } catch (Exception e) {
                LOGGER.error("Error parsing pin metadata for {}", id, e);
            }
        }
        return pins;
    }

    private int getCumulativeReactionCount(JsonNode reactionCounts) {
        int totalCount = 0;
        Iterator<JsonNode> it = reactionCounts.elements();
        while (it.hasNext()) {
            totalCount += it.next().asInt();
        }
        return totalCount;
    }

    private PinterestPin.Type getMediaType(JsonNode item) {
        JsonNode storyPinData = item.get("story_pin_data");
        if (storyPinData.isNull()) {
            return PinterestPin.Type.IMAGE;
        }
        return (storyPinData.get("total_video_duration").asInt() > 0) ? PinterestPin.Type.VIDEO : PinterestPin.Type.IMAGE;
    }
}
