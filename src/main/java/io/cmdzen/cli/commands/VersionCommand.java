package io.cmdzen.cli.commands;

import org.jline.utils.AttributedString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.command.annotation.Command;

@Command
public class VersionCommand {

    @Value("${spring.application.version}")
    private String version;

    @Command(command = "version",
            alias = "-v",
            description = "Display the version of Cmdzen.")
    public String printVersion() {
        AttributedString attributedString = new AttributedString("Cmdzen version " + version);
        return attributedString.toAnsi();
    }
}
