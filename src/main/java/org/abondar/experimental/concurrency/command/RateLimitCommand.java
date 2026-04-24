package org.abondar.experimental.concurrency.command;

import org.abondar.experimental.concurrency.command.core.Command;
import org.abondar.experimental.concurrency.command.core.CommandName;
import org.abondar.experimental.concurrency.pattern.TokenBucketRateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RateLimitCommand implements Command {

    private final Logger log = LoggerFactory.getLogger(RateLimitCommand.class);

    @Override
    public CommandName name() {
        return CommandName.RATE;
    }

    @Override
    public void run() {
        var limiter = new TokenBucketRateLimiter(5,100);

        try {
            for (int i = 0; i < 20; i++) {
                log.info("Request allowed: {}", limiter.allowRequest());
                log.info("Available tokens: {}", limiter.availableTokens());
                Thread.sleep(50);
            }

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ex);
        } finally {

            limiter.shutdown();

        }
    }
}
