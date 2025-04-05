package com.woreto.pinterest;

import com.woreto.ChromeBot;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PinService {

    public static final String TMP_DIR = "/tmp";
    public static final String PINTEREST_BASE_URL = "https://in.pinterest.com/pin/";
    public static final String PINTEREST_VIDEO_DOWNLOADER_URL = "https://www.savepin.app/";

    private final static Logger LOGGER = LoggerFactory.getLogger(PinService.class);

    @Autowired
    ChromeBot bot;

    @Autowired
    PinDAOService pinDAOService;

    public String pickAndDownloadPin(String keyword) throws Exception {
        PinterestPin pin = pinDAOService.fetchUnpostedPin(keyword);
        if (pin == null) {
            throw new RuntimeException("Out of scraped pins for " + keyword);
        }
        LOGGER.info("Loading media for pin {}", pin.getId());
        String mediaUrl = switch(pin.getMediaType()) {
            case PinterestPin.Type.IMAGE -> downloadFile(pin.getImageUrl(), pin.getId() + ".jpg");
            case PinterestPin.Type.VIDEO -> downloadVideo(pin.getId(), pin.getId() + ".mp4");
        };
        pinDAOService.updatePosted(pin.getId());
        return mediaUrl;
    }

    public String downloadVideo(String pinId, String filePath) throws Exception {
        String pinUrl = PINTEREST_BASE_URL + pinId;
        bot.navigateTo(PINTEREST_VIDEO_DOWNLOADER_URL, 4);

        bot.findAndType(By.xpath("//input[@id='url']"), pinUrl, 2);
        bot.findAndClickSpan("Download", 10);
        WebElement element = bot.driver.findElement(By.xpath("//a[text()='Download']"));
        String downloadUrl = element.getAttribute("href");
        return downloadFile(downloadUrl, filePath);
    }

    public String downloadFile(String urlStr, String fileName) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            Path destinationPath = Paths.get(TMP_DIR, fileName);
            try (
                    InputStream inputStream = connection.getInputStream();
                    OutputStream outputStream = new FileOutputStream(destinationPath.toFile())
            ) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } finally {
                connection.disconnect();
            }
            LOGGER.info("Downloaded pin to {}", destinationPath);
            return destinationPath.toString();
        } catch (Exception e) {
            LOGGER.error("Error downloading file from {}", urlStr, e);
        }
        return null;
    }
}
