package com.woreto;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.Random;

public class HumanMimicService {

    public static final int AVG_TYPING_DELAY_MS = 200; // 60 wpm, 1 word = 5 char
    public static final int AVG_TYPING_DELAY_VARIATION = 50;
    public static final float AVG_ERROR_RATE = 0.04F;

    private static final Random RANDOM = new Random();

    public static void type(WebElement element, String text) throws InterruptedException {
        for (char c : text.toCharArray()) {
            typeChar(element, c);
        }
    }

    private static void typeChar(WebElement element, char c) throws InterruptedException {
        if (booleanWithProb(AVG_ERROR_RATE)) {
            element.sendKeys("#");
            Thread.sleep(waitTimeForKeystroke());
            element.sendKeys(Keys.BACK_SPACE);
            Thread.sleep(waitTimeForKeystroke());
        }
        element.sendKeys(String.valueOf(c));
        Thread.sleep(waitTimeForKeystroke());
    }

    public static void click(WebElement element, WebDriver driver) throws InterruptedException {
//        Actions actions = new Actions(driver);
//
//        // Get the element's location and size
//        int elementX = element.getLocation().getX();
//        int elementY = element.getLocation().getY();
//        int elementWidth = element.getSize().getWidth();
//        int elementHeight = element.getSize().getHeight();
//
//        // Start from a random offset near the element
//        int startX = elementX + RANDOM.nextInt(50) - 25;
//        int startY = elementY + RANDOM.nextInt(50) - 25;
//        actions.moveByOffset(startX, startY).perform();
//        Thread.sleep(RANDOM.nextInt(100) + 50); // Short pause
//
//        // Simulate curved path to the element
//        int steps = 10;
//        for (int i = 1; i <= steps; i++) {
//            int intermediateX = startX + (elementX - startX) * i / steps + RANDOM.nextInt(3) - 1;
//            int intermediateY = startY + (elementY - startY) * i / steps + RANDOM.nextInt(3) - 1;
//            actions.moveByOffset(intermediateX - startX, intermediateY - startY).perform();
//            startX = intermediateX;
//            startY = intermediateY;
//            Thread.sleep(RANDOM.nextInt(30) + 20); // Vary the delay between movements
//        }
//
//        // Move slightly inside the element (random offset within element bounds)
//        int finalX = elementX + RANDOM.nextInt(elementWidth) - elementWidth / 2;
//        int finalY = elementY + RANDOM.nextInt(elementHeight) - elementHeight / 2;
//        actions.moveByOffset(finalX - startX, finalY - startY).perform();
//        Thread.sleep(RANDOM.nextInt(100) + 50); // Short pause before clicking
//
//        // Finally, click the element
//        actions.click().perform();
        element.click();
    }

    private static boolean booleanWithProb(float prob) {
        return RANDOM.nextFloat() < prob;
    }

    private static int waitTimeForKeystroke() {
        return (AVG_TYPING_DELAY_MS + RANDOM.nextInt(-AVG_TYPING_DELAY_VARIATION, AVG_TYPING_DELAY_VARIATION));
    }
}
