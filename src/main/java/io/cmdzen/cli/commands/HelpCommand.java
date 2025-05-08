package io.cmdzen.cli.commands;

import io.cmdzen.cli.model.CommandNames;
import org.springframework.shell.command.annotation.Command;

@Command(group = "Help All")
public class HelpCommand {

    @Command(command = CommandNames.HELP, description = "Shows all commands")
    public String cmdzMessage() {
        return """
                CmdZen - YOUR LINUX COMMANDLINE ASSISTANT
                
                Usage:
                  cmdzen [OPTIONS] [COMMAND]
                
                Description:
                  CmdZen helps new Linux users by providing command explanations,
                  installation guidance, command recommendations, and dynamic command creation.
                
                Options:
                  --help, -h      Show this help message
                  --create        Start dynamic command creation wizard
                  --install       Begin guided distro installation
                  --explain       Explain an error message
                  --recommend     Suggest useful commands
                
                Example:
                  cmdzen --create
                """;
    }
}
