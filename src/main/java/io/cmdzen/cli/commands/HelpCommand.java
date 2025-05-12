package io.cmdzen.cli.commands;

import org.springframework.shell.command.annotation.Command;

@Command
public class HelpCommand {

    @Command(command = "help",
            alias = "-h",
            description = "Shows all commands")
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
