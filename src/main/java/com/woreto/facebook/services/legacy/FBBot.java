package com.woreto.facebook.services.legacy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woreto.RetryService;
import com.woreto.facebook.models.FBAccount;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v131.network.Network;
import org.openqa.selenium.devtools.v131.network.model.Request;
import org.openqa.selenium.devtools.v131.network.model.RequestId;
import org.openqa.selenium.devtools.v131.network.model.ResponseReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.woreto.HumanMimicService.click;
import static com.woreto.HumanMimicService.type;

//@Service
public class FBBot {

    private static final String SESSION_FILE = "session-cookies.json";
    private static final String BASE_URL = "https://www.facebook.com/";
    private static final Logger LOGGER = LoggerFactory.getLogger(FBBot.class);

    private ChromeDriver driver;
    private DevTools devTools;
    private String currUser;

    private final Map<String, Request> requestMap = new ConcurrentHashMap<>();

    public void init() {
        setupChromeDriver();
        driver.get(BASE_URL);
        restoreCookies();
        try {
            Thread.sleep(1000);
            driver.get(BASE_URL);
            currUser = getLoggedInUser();
            if (currUser != null) {
                LOGGER.info("Restored session as {}", currUser);
            }
        } catch (Exception ignored) {}
    }

    public void run(FBAccount account) throws InterruptedException {
        if (currUser == null) {
            loginAccount(account);
        } else if (!account.getFullName().equals(currUser)) {
            switchAccount(account);
        }
        saveCookies();
    }

    public void stop() {
        driver.quit();
    }

    public ChromeDriver getDriver() {
        return driver;
    }

    public String getCurrUser() {
        return currUser;
    }

    public void setCurrUser(String currUser) {
        this.currUser = currUser;
    }

    public void navigateTo(String url, int waitInSec) throws InterruptedException {
        driver.get(url);
        Thread.sleep(waitInSec * 1000L);
    }

    public void findAndClickSpan(String content, int waitInSec) throws InterruptedException {
        WebElement element = driver.findElement(By.xpath("//span[text()='" + content + "']"));
        click(element, driver);
        Thread.sleep(waitInSec * 1000L);
    }

    public void findAndClickBySelector(String selector, int waitInSec) throws InterruptedException {
        WebElement element = driver.findElement(By.xpath(selector));
        click(element, driver);
        Thread.sleep(waitInSec * 1000L);
    }

    public void findAndUploadInput(String prefix, String mediaPath, int waitInSec) throws InterruptedException {
        WebElement fileInput =
                driver.findElement(By.xpath("//span[text()='" + prefix + "']/following::input[1]"));
        fileInput.sendKeys(mediaPath);
        Thread.sleep(waitInSec * 1000L);
    }

    public void findAndTypeBySelector(String selector, String input, int waitInSec) throws InterruptedException {
        WebElement element = driver.findElement(By.xpath(selector));
        click(element, driver);
        Thread.sleep(1000L);
        type(element, input);
        Thread.sleep((waitInSec - 1) * 1000L);
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

    private void setupChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        driver = new ChromeDriver(options);
        driver.manage().window().setSize(new Dimension(1920, 1080));

        devTools = driver.getDevTools();
        devTools.createSession();
    }

    private void saveCookies() {
        Set<Cookie> cookies = driver.manage().getCookies();
        try (Writer writer = new FileWriter(SESSION_FILE)) {
            new ObjectMapper().writeValue(writer, cookies);
        } catch (IOException e) {
            LOGGER.error("Error saving cookies", e);
        }
    }

    private void restoreCookies() {
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
        } catch (IOException e) {
            LOGGER.error("Error restoring cookies", e);
        }
        LOGGER.info("Restored cookies successfully");
    }

    private void loginAccount(FBAccount account) throws InterruptedException {
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement passField = driver.findElement(By.id("pass"));
        WebElement loginButton = driver.findElement(By.name("login"));

        click(emailField, driver);
        type(emailField, account.getEmailId());
        Thread.sleep(1000);
        click(passField, driver);
        type(passField, account.getPassword());
        Thread.sleep(1000);
        click(loginButton, driver);
        Thread.sleep(4000);

        currUser = getLoggedInUser();
        LOGGER.info("Logged in with user {}", currUser);
    }

    private void switchAccount(FBAccount account) throws InterruptedException {
        findAndClickBySelector("//div[@aria-label='Your profile']", 2);
        try {
            findAndClickSpan(account.getFullName(), 2);
            currUser = getLoggedInUser();
            LOGGER.info("Switched to user {}", currUser);
        } catch (Exception e) {
            LOGGER.error("Couldn't switch to profile {} from current context {}", account.getFullName(), currUser);
            findAndClickSpan("Log out", 4);
            loginAccount(account);
        }
    }

    private String getLoggedInUser() throws InterruptedException {
        if (!BASE_URL.equals(driver.getCurrentUrl())) {
            driver.get(BASE_URL);
            Thread.sleep(4000);
        }
        try {
            WebElement element = driver.findElement(By.xpath("//span[contains(text(), \"What's on your mind\")]"));
            String text = element.getText(); // e.g., "What's on your mind, Gaurav?"
            String firstName =  text.substring(text.indexOf(",") + 2, text.length() - 1); // get the name

            element = driver.findElement(By.xpath("//span[contains(text(), \"" + firstName + "\")]"));
            return element.getText();
        } catch (NoSuchElementException e) {
            return null; // Not logged in
        }
    }
}
