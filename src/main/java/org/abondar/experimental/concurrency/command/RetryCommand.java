package org.abondar.experimental.concurrency.command;

import org.abondar.experimental.concurrency.command.core.Command;
import org.abondar.experimental.concurrency.command.core.CommandName;
import org.abondar.experimental.concurrency.pattern.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;


public class RetryCommand implements Command {

    private final Logger log = LoggerFactory.getLogger(RetryCommand.class);

    @Override
    public CommandName name() {
        return CommandName.RETRY;
    }

    @Override
    public void run() {
        var retry = new Retry(5);

        Callable<String> successfulTask = () -> "Hello";

        AtomicInteger counter = new AtomicInteger();
        Callable<String> failTask = () -> {
            int attempt = counter.incrementAndGet();

            if (attempt < 3) {
                throw new RuntimeException("Fail on attempt " + attempt);
            }

            return "Success on attempt " + attempt;
        };

        Callable<String> brokenTask = () -> {
            throw new RuntimeException("Broken");
        };

        try {
            var res = retry.executeWithRetry(successfulTask);
            res.ifPresent(r -> log.info("Result: {}", r));

            res = retry.executeWithRetry(failTask);
            res.ifPresent(r -> log.info("Result: {}", r));

            retry = new Retry(1);
            retry.executeWithRetry(brokenTask);
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }
    }
}
