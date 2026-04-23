package org.abondar.experimental.concurrency.command;

import org.abondar.experimental.concurrency.command.core.Command;
import org.abondar.experimental.concurrency.command.core.CommandName;
import org.abondar.experimental.concurrency.pattern.StructuredConcurrency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class StructuredConcurrencyCommand implements Command {

    private final Logger log = LoggerFactory.getLogger(StructuredConcurrencyCommand.class);

    @Override
    public CommandName name() {
        return CommandName.STRC;
    }

    @Override
    public void run() {
       List<Callable<Integer>> subtasks = IntStream.range(1, 5)
                .mapToObj(i -> (Callable<Integer>) () -> {
                    int t = ThreadLocalRandom.current().nextInt(1000);
                    log.info("New number {}",t);
                    Thread.sleep(t);
                    return t * i;
                })
                .toList();

        var strc = new StructuredConcurrency();
        var res = strc.runTasksInScope(subtasks);
        res.forEach(rs->log.info("Result: {}", rs));

    }
}
