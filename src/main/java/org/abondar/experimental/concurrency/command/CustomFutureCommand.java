package org.abondar.experimental.concurrency.command;

import org.abondar.experimental.concurrency.command.core.Command;
import org.abondar.experimental.concurrency.command.core.CommandName;
import org.abondar.experimental.concurrency.pattern.future.CustomFuture;
import org.abondar.experimental.concurrency.pattern.future.FutureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class CustomFutureCommand implements Command {

    private static final Logger log = LoggerFactory.getLogger(CustomFutureCommand.class);

    @Override
    public CommandName name() {
        return CommandName.FUTURE;
    }

    @Override
    public void run() {
        runDemo("success", future -> future.complete("Hello from custom future"));
        runDemo("failure", future ->
                future.fail(new IllegalStateException("Simulated worker failure")));
        runDemo("cancel", CustomFuture::cancel);
    }


    private void runDemo(String demoName, Consumer<CustomFuture<String>> terminalAction) {
        var future = new CustomFuture<String>();
        var waiter = Thread.ofVirtual()
                .name("future-" + demoName + "-waiter")
                .start(() -> waitForResult(demoName, future));

        var worker = Thread.ofVirtual()
                .name("future-" + demoName + "-worker")
                .start(() -> finishFuture(demoName, future, terminalAction));

        join(worker);
        join(waiter);
    }

    private void waitForResult(String demoName, CustomFuture<String> future) {
        try {
            log.info("[{}] waiting for future...", demoName);

            var result = future.get();

            log.info("[{}] future completed with result: {}", demoName, result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            log.error("[{}] interrupted while waiting for future", demoName, e);
        } catch (FutureException e) {
            log.error("[{}] future finished without value", demoName, e);
        }

    }

    private void finishFuture(String demoName, CustomFuture<String> future,
                              Consumer<CustomFuture<String>> terminalAction) {
        try {
            TimeUnit.MILLISECONDS.sleep(500);

            terminalAction.accept(future);

            log.info("[{}] terminal action executed, state={}", demoName, future.getState());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            future.fail(e);
        }

    }

    private void join(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            log.error("Interrupted while joining {}", thread.getName(), e);
        }

    }
}
