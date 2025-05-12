package io.cmdzen.cli.commands;

import io.cmdzen.cli.services.ShellService;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;

import java.io.IOException;

@Command
public class LocalHistoryCommand {

    private ShellService commandHistoryService;

    public LocalHistoryCommand(ShellService commandHistoryService) {
        this.commandHistoryService = commandHistoryService;
    }

    @Command(command = "local",
            alias = "-l",
            description = "Gives the local history")
    public void getLocalHistory(@Option(shortNames = 'l') String arg) throws IOException {
        commandHistoryService.getLocalCommandHistory().getCommandHistory();
    }
}
