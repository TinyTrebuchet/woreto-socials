package com.woreto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class RetryService {

    public static final int COUNT = 3;
    public static final int TIMEOUT = 500;
    private static final Logger LOGGER = LoggerFactory.getLogger(RetryService.class);

    public static <V> V executeWithRetry(Callable<V> callable) throws Exception {
        return executeWithRetry(callable, COUNT, TIMEOUT);
    }

    public static <V> V executeWithRetry(Callable<V> callable, int retryCount, int retryTimeout) throws Exception {
        while (--retryCount > 0) {
            try {
                return callable.call();
            } catch (Exception e) {
                LOGGER.warn("Callable failed! Retrying...");
                Thread.sleep(retryTimeout);
            }
        }
        return callable.call();
    }

}
