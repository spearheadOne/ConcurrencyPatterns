package org.abondar.experimental.concurrency.command;

import org.abondar.experimental.concurrency.command.core.Command;
import org.abondar.experimental.concurrency.command.core.CommandName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelpCommand implements Command {
    private final Logger log = LoggerFactory.getLogger(HelpCommand.class);

    @Override
    public CommandName name() {
        return CommandName.HELP;
    }

    @Override
    public void run() {
        for (CommandName c : CommandName.values()) {
            log.info("{} -> {}", c.name(), c.description());
        }
    }
}
