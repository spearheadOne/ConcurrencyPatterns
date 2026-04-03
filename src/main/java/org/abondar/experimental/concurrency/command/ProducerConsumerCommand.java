package org.abondar.experimental.concurrency.command;

import org.abondar.experimental.concurrency.command.core.Command;
import org.abondar.experimental.concurrency.command.core.CommandName;
import org.abondar.experimental.concurrency.pattern.ProducerConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProducerConsumerCommand implements Command {
    private final Logger log = LoggerFactory.getLogger(ProducerConsumerCommand.class);


    @Override
    public CommandName name() {
        return CommandName.PRC;
    }

    @Override
    public void run() {
        var producerConsumer = new ProducerConsumer(10, 4,7);
        try {
            producerConsumer.runProducers();
            producerConsumer.runConsumers();
        } catch (InterruptedException e) {
            log.info("Interrupted", e);
        }

    }
}
