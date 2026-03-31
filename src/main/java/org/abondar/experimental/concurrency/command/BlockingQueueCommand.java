package org.abondar.experimental.concurrency.command;

import org.abondar.experimental.concurrency.command.core.Command;
import org.abondar.experimental.concurrency.command.core.CommandName;
import org.abondar.experimental.concurrency.queue.CustomBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockingQueueCommand implements Command {

    private final Logger log = LoggerFactory.getLogger(BlockingQueueCommand.class);

    private final CustomBlockingQueue<Integer> queue;

    public BlockingQueueCommand() {
        queue = new CustomBlockingQueue<>(3);
    }

    @Override
    public CommandName name() {
        return CommandName.QUEUE;
    }

    @Override
    public void run() {
        try {
            var producer1 = createProducer(1);
            var producer2 = createProducer(2);
            var producer3 = createProducer(3);
            var producer4 = createProducer(4);

            var consumer = Thread.ofVirtual().start(() -> {
                try {
                    int res = queue.take();
                    log.info("Consumed: {}", res);
                } catch (InterruptedException e) {
                    log.error("Error while consuming", e);
                    throw new RuntimeException(e);
                }
            });


            producer1.join();
            producer2.join();
            producer3.join();
            producer4.join();
            consumer.join();

        } catch (InterruptedException e) {
            log.error("Error while joining", e);
            throw new RuntimeException(e);
        }

    }


    private Thread createProducer(Integer data) {
        return Thread.ofVirtual().start(() -> {
            try {
                queue.add(data);
                log.info("Added: {} queue size: {}", data, queue.size());
            } catch (InterruptedException e) {
                log.error("Error while adding", e);
                throw new RuntimeException(e);
            }
        });
    }

}
