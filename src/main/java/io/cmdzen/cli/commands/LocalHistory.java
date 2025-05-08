package io.cmdzen.cli.commands;

import io.cmdzen.cli.model.CommandHistory;
import io.cmdzen.cli.services.CommandHistoryService;
import org.springframework.shell.command.annotation.Command;

import java.io.IOException;

@Command(group = "Util")
public class LocalHistory {

    private CommandHistoryService commandHistoryService;

    public LocalHistory(CommandHistoryService commandHistoryService) {
        this.commandHistoryService = commandHistoryService;
    }

    @Command(command = "local", description = "Gives the local history")
    public void getLocalHistory() throws IOException {
        commandHistoryService.getLocalCommandHistory().getCommandHistory();
    }
}
