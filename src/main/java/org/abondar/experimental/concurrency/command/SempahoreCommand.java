package org.abondar.experimental.concurrency.command;

import org.abondar.experimental.concurrency.command.core.Command;
import org.abondar.experimental.concurrency.command.core.CommandName;
import org.abondar.experimental.concurrency.pattern.CustomSemaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SempahoreCommand implements Command {

    private final Logger log = LoggerFactory.getLogger(SempahoreCommand.class);

    @Override
    public CommandName name() {
        return CommandName.SEM;
    }

    @Override
    public void run() {
        var semaphore = new CustomSemaphore(2);

        try {
            semaphore.acquire();
            log.info("Main thread acquired one permit. Available permits: {}", semaphore.availablePermits());

            var worker1 = createWorker("Worker-1", semaphore);
            var worker2 = createWorker("Worker-2", semaphore);

            semaphore.release();
            log.info("Available permits after main release: {}", semaphore.availablePermits());

            worker1.join();
            worker2.join();

            log.info("Final available permits: {}", semaphore.availablePermits());

        } catch (InterruptedException ex) {
            throw new RuntimeException();
        }
    }

    private Thread createWorker(String name, CustomSemaphore semaphore) {
        return Thread.ofVirtual()
                .name(name)
                .start(() -> {
                    try {
                        log.info("{} trying to acquire", Thread.currentThread().getName());
                        semaphore.acquire();
                        log.info("{} acquired semaphore. Available permits: {}",
                                Thread.currentThread().getName(),
                                semaphore.availablePermits());

                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    } finally {
                        semaphore.release();
                        log.info("{} released semaphore. Available permits: {}",
                                Thread.currentThread().getName(),
                                semaphore.availablePermits());
                    }
                });
    }
}
