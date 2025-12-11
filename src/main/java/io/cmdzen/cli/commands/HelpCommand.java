package io.cmdzen.cli.commands;

import org.springframework.context.ApplicationContext;
import org.springframework.shell.command.CommandCatalog;
import org.springframework.shell.command.CommandRegistration;
import org.springframework.shell.command.annotation.Command;

import java.util.Map;

@Command
public class HelpCommand {

    private final ApplicationContext applicationContext;

    public HelpCommand(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Command(command = "help",
            alias = "-h",
            description = "Shows all commands")
    public void cmdzMessage() {
        CommandCatalog commandCatalog = applicationContext.getBean(CommandCatalog.class);
        Map<String, CommandRegistration> registrations = commandCatalog.getRegistrations();
        System.out.println("Registered Commands:");
        for (Map.Entry<String, CommandRegistration> registration : registrations.entrySet()) {
            System.out.println("- " + registration.getKey() + " " +
                             registration.getValue().getCommand() + " " +
                             registration.getValue().getDescription());
        }
    }
}
