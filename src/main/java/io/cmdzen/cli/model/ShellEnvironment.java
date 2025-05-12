package io.cmdzen.cli.model;

import io.cmdzen.cli.exceptions.ShellVariableMissingException;
import io.cmdzen.cli.exceptions.UnsupportedShellException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

@Data
@Slf4j
public class ShellEnvironment {
    private String shellName;
    private Path configPath;
    private Path historyPath;

    public ShellEnvironment() {
        shellConfigure();
    }

    public void shellConfigure()  {
        String env = System.getenv("SHELL");
        if (env.isEmpty()) {
            log.error("Environment variable SHELL is not set.");
            throw new ShellVariableMissingException("SHELL environment variable is not set.");
        }

        String homeDir = System.getProperty("user.home");
        if (env.contains("bash")) {
            shellName = "bash";
            configPath = Path.of(homeDir, ".bashrc");
            historyPath = Path.of(homeDir, ".bash_history");
        } else if (env.contains("zsh")) {
            shellName = "zsh";
            configPath = Path.of(homeDir, ".zshrc");
            historyPath = Path.of(homeDir, ".zsh_history");
        } else if (env.contains("fish")) {
            shellName = "fish";
            configPath = Path.of(homeDir, ".config/fish/config.fish");
            historyPath = Path.of(homeDir, ".local/share/fish/fish_history");
        } else if (env.contains("ksh")) {
            shellName = "ksh";
            configPath = Path.of(homeDir, ".kshrc");
            historyPath = Path.of(homeDir, ".ksh_history");
        } else {
            log.warn("Unsupported shell detected: {}", env);
            throw new UnsupportedShellException("Unsupported shell detected: " + env + "Create a issue on Github");
        }

    }
}
