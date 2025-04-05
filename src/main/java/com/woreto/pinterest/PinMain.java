package com.woreto.pinterest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(scanBasePackages = {"com.woreto"})
public class PinMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(PinMain.class);

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(PinMain.class, args);
        PinGrabber pinGrabber = applicationContext.getBean(PinGrabber.class);
        try {
            pinGrabber.run(args);
        } catch (Exception e) {
            LOGGER.error("Error running pin grabber", e);
        }
    }
}
