package io.cmdzen.cli.commands;

import io.cmdzen.cli.services.ShellService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.command.annotation.Command;

@Command
@Slf4j
public class Configure {

    private final ShellService shellService;

    public Configure(ShellService shellService) {
        this.shellService = shellService;
    }

    @Command(command = "configure",
            alias = "-c",
            description = "Add CmdZen integration into your shell config")
    public String integrate() {
        String shellName = shellService.getShellEnvironment().getShellName();
        String configPath = shellService.getConfigPath().toString();

        System.out.println("Installing CmdZen integration for " + shellName + "...");
        System.out.println("Config file: " + configPath);
        System.out.println();

        boolean success = shellService.injectShellIntegration();

        if (success) {
            return getMessage(shellName, configPath);
        } else {
            return "Failed to inject shell integration. Check logs for details.";
        }
    }

    private String getMessage(String shellName, String configPath) {
        StringBuilder sb = new StringBuilder();
        sb.append("CmdZen integration successfully installed!\n\n");
        sb.append("What was added:\n");
        sb.append("- Command capture hooks in your ").append(shellName).append(" config\n");
        sb.append("- Failed command output is now automatically saved\n");
        sb.append("- The 'solve' command can now analyze errors without re-running commands\n\n");
        sb.append("⚠️  IMPORTANT: Restart your terminal or run:\n");
        sb.append("   source ").append(configPath).append("\n\n");
        sb.append("After restarting, failed commands will be automatically captured.\n");
        sb.append("Just run 'cmdzen solve' after any error to get AI-powered help!\n");
        return sb.toString();
    }

    @Command(command = "integration-status",
            description = "Check if shell integration is installed")
    public String checkIntegration() {
        String shellName = shellService.getShellEnvironment().getShellName();
        String configPath = shellService.getConfigPath().toString();

        try {
            java.nio.file.Path path = java.nio.file.Path.of(configPath);
            if (!java.nio.file.Files.exists(path)) {
                return "Config file not found: " + configPath;
            }

            String content = java.nio.file.Files.readString(path);
            boolean hasIntegration = content.contains("# CmdZen integration");

            if (hasIntegration) {
                return "CmdZen integration is installed in " + shellName + "\n" +
                       "Config: " + configPath + "\n\n" +
                       "Failed commands will be automatically captured for 'solve' command.";
            } else {
                return "CmdZen integration not found in " + shellName + "\n" +
                       "Run 'cmdzen integrate' to install it.";
            }

        } catch (Exception e) {
            log.error("Error checking integration status", e);
            return "Error checking integration: " + e.getMessage();
        }
    }
}

