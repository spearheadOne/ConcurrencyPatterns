package org.abondar.experimental.concurrency.pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;


public class Retry {

    private final int maxAttempts;

    private final Logger log = LoggerFactory.getLogger(Retry.class);


    public Retry(int maxAttempts) {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("Max attempts must be positive");
        }

        this.maxAttempts = maxAttempts;
    }

    public <T> Optional<T> executeWithRetry(Callable<T> task) throws Exception {
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try {
                return Optional.ofNullable(task.call());
            } catch (Exception ex) {
                if (attempt == maxAttempts - 1) {
                    throw new RuntimeException("Exceed max attempts: " + maxAttempts, ex);
                }

                int baseDelayMs = 100;
                var expDelay = baseDelayMs * Double.valueOf(Math.pow(2, attempt)).intValue();
                int maxDelayMs = 5000;
                var cappedDelay = Math.min(expDelay, maxDelayMs);
                var delay = ThreadLocalRandom.current().nextInt(1, cappedDelay + 1);

                log.info("Attempt {} failed. Retrying after {} ms", attempt + 1, delay);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw interrupted;
                }

            }
        }

        return Optional.empty();
    }


}
