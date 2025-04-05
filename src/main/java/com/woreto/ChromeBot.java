package com.woreto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v131.network.Network;
import org.openqa.selenium.devtools.v131.network.model.Request;
import org.openqa.selenium.devtools.v131.network.model.RequestId;
import org.openqa.selenium.devtools.v131.network.model.ResponseReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.woreto.HumanMimicService.click;
import static com.woreto.HumanMimicService.type;

public class ChromeBot {

    public static final String SESSION_FILE = "session-cookies.json";

    private static Logger LOGGER = LoggerFactory.getLogger(ChromeBot.class);

    public ChromeDriver driver;
    public DevTools devTools;
    public String currUser;

    private final Map<String, Request> requestMap = new HashMap<>();

    public void navigateTo(String url, int waitInSec) throws InterruptedException {
        navigateTo(url, waitInSec, false);
    }

    public void navigateTo(String url, int waitInSec, boolean force) throws InterruptedException {
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl != null && StringUtils.pathEquals(url, currentUrl) && !force) {
            return;
        }
        driver.get(url);
        Thread.sleep(waitInSec * 1000L);
    }

    public void findAndClickSpan(String content, int waitInSec) throws InterruptedException {
        WebElement element = driver.findElement(By.xpath("//span[text()='" + content + "']"));
        click(element, driver);
        Thread.sleep(waitInSec * 1000L);
    }

    public void findAndClick(By selector, int waitInSec) throws InterruptedException {
        WebElement element = driver.findElement(selector);
        click(element, driver);
        Thread.sleep(waitInSec * 1000L);
    }

    public void findAndClickJs(By selector, int waitInSec) throws InterruptedException {
        WebElement element = driver.findElement(selector);
        driver.executeScript("arguments[0].click();", element);
        Thread.sleep(waitInSec * 1000L);
    }

    public void findAndType(By selector, String input, int waitInSec) throws InterruptedException {
        WebElement element = driver.findElement(selector);
        click(element, driver);
        Thread.sleep(1000L);
        type(element, input);
        Thread.sleep((waitInSec - 1) * 1000L);
    }

    public String findAndGet(By selector) {
        try {
            WebElement element = driver.findElement(selector);
            return element.getText();
        } catch (Exception e) {
            return null;
        }
    }

    public void findAndUpload(By selector, String mediaPath, int waitInSec) throws InterruptedException {
        WebElement fileInput = driver.findElement(selector);
        fileInput.sendKeys(mediaPath);
        Thread.sleep(waitInSec * 1000L);
    }

    public void attachListener(Consumer<ResponseReceived> listener) {
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
        devTools.addListener(Network.requestWillBeSent(), requestToSend ->
                requestMap.put(requestToSend.getRequestId().toString(), requestToSend.getRequest())
        );
        devTools.addListener(Network.responseReceived(), listener);
    }

    public void detachListeners() {
        devTools.clearListeners();
        devTools.send(Network.disable());
        requestMap.clear();
    }

    public Request getRequest(RequestId requestId) {
        Request request = requestMap.get(requestId.toString());
        return request;
    }

    public Network.GetResponseBodyResponse getResponseBody(RequestId requestId) {
        Network.GetResponseBodyResponse response;
        try {
            response = RetryService.executeWithRetry(
                    () -> driver.getDevTools().send(Network.getResponseBody(requestId))
            );
            return response;
        } catch (Exception e) {
            LOGGER.error("No response body found for requestId {}", requestId);
        }
        return null;
    }

    public void setupChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        driver = new ChromeDriver(options);
        driver.manage().window().setSize(new Dimension(1920, 1080));

        devTools = driver.getDevTools();
        devTools.createSession();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    public void saveCookies() {
        Set<Cookie> cookies = driver.manage().getCookies();
        try (Writer writer = new FileWriter(SESSION_FILE)) {
            new ObjectMapper().writeValue(writer, cookies);
            LOGGER.info("Saved cookies to {}", SESSION_FILE);
        } catch (IOException e) {
            LOGGER.error("Error saving cookies", e);
        }
    }

    public void saveCookies(String key) {
        Set<Cookie> cookies = driver.manage().getCookies();
        try {
            // Check if file exists and load existing data
            Map<String, List<Cookie>> cookieMap = new HashMap<>();
            File file = new File(SESSION_FILE);

            if (file.exists() && file.length() > 0) {
                try (Reader reader = new FileReader(file)) {
                    cookieMap = new ObjectMapper().readValue(reader, new TypeReference<>() {});
                }
            }

            // Update map with new cookies for the specified account
            cookieMap.put(key, new ArrayList<>(cookies));

            // Write updated map back to file
            try (Writer writer = new FileWriter(SESSION_FILE)) {
                new ObjectMapper().writeValue(writer, cookieMap);
                LOGGER.info("Saved cookies for key '{}'", key);
            }
        } catch (IOException e) {
            LOGGER.error("Error saving cookies for account '{}'", key, e);
        }
    }

    public boolean restoreCookies(String key, String url) {
        try {
            File file = new File(SESSION_FILE);
            if (!file.exists()) {
                return false;
            }

            try (Reader reader = new FileReader(file)) {
                Map<String, List<Map<String, Object>>> allCookies =
                        new ObjectMapper().readValue(reader, new TypeReference<>() {});

                List<Map<String, Object>> cookieList = allCookies.get(key);
                if (cookieList == null || cookieList.isEmpty()) {
                    return false;
                }

                // Clear existing cookies before adding new ones
                driver.manage().deleteAllCookies();

                for (Map<String, Object> cookieMap : cookieList) {
                    String name = (String) cookieMap.get("name");
                    String value = (String) cookieMap.get("value");
                    String domain = (String) cookieMap.get("domain");
                    String path = (String) cookieMap.get("path");
                    Boolean secure = (Boolean) cookieMap.get("secure");
                    Boolean httpOnly = (Boolean) cookieMap.get("httpOnly");
                    Long expiry = (Long) cookieMap.get("expiry");

                    Cookie cookie = new Cookie.Builder(name, value)
                            .domain(domain)
                            .path(path)
                            .isSecure(secure)
                            .isHttpOnly(httpOnly)
                            .expiresOn(expiry == null ? null : Date.from(Instant.ofEpochMilli(expiry)))
                            .build();

                    driver.manage().addCookie(cookie);
                }
                LOGGER.info("Restored cookies for key '{}' successfully", key);
                if (url != null) {
                    navigateTo(url, 4, true);
                }
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("Error restoring cookies for key '{}'", key, e);
        }
        return false;
    }

    public void restoreCookies() {
        try (Reader reader = new FileReader(SESSION_FILE)) {
            List<Map<String, Object>> cookieList = new ObjectMapper().readValue(reader, new TypeReference<>() {});
            for (Map<String, Object> cookieMap : cookieList) {
                String name = (String) cookieMap.get("name");
                String value = (String) cookieMap.get("value");
                String domain = (String) cookieMap.get("domain");
                String path = (String) cookieMap.get("path");
                Boolean secure = (Boolean) cookieMap.get("secure");
                Boolean httpOnly = (Boolean) cookieMap.get("httpOnly");
                Long expiry = (Long) cookieMap.get("expiry");
                Cookie cookie = new Cookie.Builder(name, value)
                        .domain(domain)
                        .path(path)
                        .isSecure(secure)
                        .isHttpOnly(httpOnly)
                        .expiresOn(expiry == null ? null : Date.from(Instant.ofEpochMilli(expiry)))
                        .build();
                driver.manage().addCookie(cookie);
            }
            LOGGER.info("Restored cookies successfully");
        } catch (Exception e) {
            LOGGER.error("Error restoring cookies", e);
        }
    }
}
