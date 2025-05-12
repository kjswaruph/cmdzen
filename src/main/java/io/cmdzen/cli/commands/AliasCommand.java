package io.cmdzen.cli.commands;

import io.cmdzen.cli.services.ShellService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Command
@Slf4j
public class AliasCommand {

    private ShellService shellService;

    public AliasCommand(ShellService shellService) {
        this.shellService = shellService;
    }

    @Command(command = "alias",
            alias = "-a",
            description = "Change alias")
    public void changeAlias(@Option(shortNames = 'n', longNames = "new") String newAlias) {
        Path configPath = shellService.getConfigPath(); 
        File configFile = new File(configPath.toUri());

        if (configFile.exists()) {
            try {
                List<String> lines = Files.readAllLines(configFile.toPath());
                boolean modified = false;
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i).trim();
                    if (line.contains("\"$CMDZEN_PATH\"")) {
                        lines.set(i, "alias " + newAlias + "=\"$CMDZEN_PATH\"");
                        modified = true;
                        break;
                    }
                }

                if (modified) {
                    Files.write(configFile.toPath(), lines);
                    System.out.println("Alias updated to " + newAlias + ". Please run `source "+ configPath + "` or restart your terminal.");
                    log.info("Alias updated to " + newAlias + ".");
                } else {
                    System.out.println("No matching alias found.");
                    log.info("No matching alias found.");
                }
            } catch (IOException e) {
                log.error("Error reading config file.", e);
            }
        } else {
            log.info("No config file found: {}", configFile);
            System.out.println("Configuration file not found: "+configPath.toUri());
        }
    }
}
