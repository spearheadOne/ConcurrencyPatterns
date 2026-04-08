package org.abondar.experimental.concurrency.command;

import org.abondar.experimental.concurrency.command.core.Command;
import org.abondar.experimental.concurrency.command.core.CommandName;
import org.abondar.experimental.concurrency.pattern.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;

public class CounterCommand implements Command {


    private final Logger log = LoggerFactory.getLogger(CounterCommand.class);

    @Override
    public CommandName name() {
        return CommandName.CNT;
    }

    @Override
    public void run() {
        var counter = new Counter();
        var poolSize = 10;
        try (var pool = Executors.newFixedThreadPool(poolSize)) {
            Runnable incrementTask = () -> log.info("Incremented value {}", counter.increment());
            Runnable decrementTask = () -> log.info("Decremented value {}", counter.decrement());
            Runnable deltaTask = () -> log.info("Added 50 to value {}",  counter.add(50));

            Runnable resetTask = () -> {
                counter.reset();

                log.info("Reset value");
            };


            for (int i = 0; i < poolSize; i++) {
                pool.execute(incrementTask);

                if (i == 2) {
                    pool.execute(decrementTask);
                }

                if (i == 3) {
                    pool.execute(resetTask);
                }

                if (i == 5 ) {
                    pool.execute(deltaTask);
                }

            }
        }

        log.info("Final value {}", counter.getCount());
    }
}
