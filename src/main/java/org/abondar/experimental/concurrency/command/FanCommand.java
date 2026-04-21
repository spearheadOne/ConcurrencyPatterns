package org.abondar.experimental.concurrency.command;

import org.abondar.experimental.concurrency.command.core.Command;
import org.abondar.experimental.concurrency.command.core.CommandName;
import org.abondar.experimental.concurrency.pattern.FanInOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FanCommand implements Command {

    private final Logger log = LoggerFactory.getLogger(FanCommand.class);

    @Override
    public CommandName name() {
        return CommandName.FAN;
    }

    @Override
    public void run() {
        List<String> data = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            data.add(UUID.randomUUID().toString());
        }

        var fan = new FanInOut();
        fan.fanOut(data);

        var res = fan.fanIn();

        res.forEach(log::info);

        fan.shutdown();
    }
}
