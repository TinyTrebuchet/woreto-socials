package com.woreto.facebook;


import com.woreto.facebook.services.FBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(scanBasePackages = {"com.woreto"})
public class FBMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(FBMain.class);

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(FBMain.class, args);
        FBService fbService = applicationContext.getBean(FBService.class);
        try {
            fbService.run();
            System.exit(0);
        } catch (Exception e) {
            LOGGER.error("Error running facebook driver", e);
        }
    }
}
