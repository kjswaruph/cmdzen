package io.cmdzen.cli.commands;

import io.cmdzen.cli.services.ShellService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.command.annotation.Command;

@Command
@Slf4j
public class LastFailedCommand {

    private final ShellService shellService;

    public LastFailedCommand(ShellService shellService) {
        this.shellService = shellService;
    }

    @Command(command = "last-failed",
            alias = "-lf",
            description = "Shows the last failed command and its output")
    public String getLastFailed() {
        try {
            String capturedOutput = shellService.getLastCommandOutput();
            int exitCode = shellService.getLastExitCode();
            String lastCommand = shellService.getLastCommand();

            if (capturedOutput == null || capturedOutput.isEmpty()) {
                return "❌ No failed command found.\n" +
                       "Make sure you've run 'cmdzen integrate' to enable command capture.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════════════════════════\n");
            sb.append("🔴 LAST FAILED COMMAND\n");
            sb.append("═══════════════════════════════════════════════════════\n\n");

            // Parse and display command info
            String[] lines = capturedOutput.split("\\R");
            String command = null;
            String timestamp = null;
            int code = exitCode;
            StringBuilder output = new StringBuilder();

            for (String line : lines) {
                String trimmed = line.trim();

                if (trimmed.isEmpty()) {
                    continue;
                }

                if (trimmed.startsWith("COMMAND:")) {
                    command = trimmed.substring("COMMAND:".length()).trim();
                } else if (trimmed.startsWith("EXIT_CODE:")) {
                    try {
                        code = Integer.parseInt(trimmed.substring("EXIT_CODE:".length()).trim());
                    } catch (NumberFormatException ignored) {
                    }
                } else if (trimmed.startsWith("TIMESTAMP:")) {
                    timestamp = trimmed.substring("TIMESTAMP:".length()).trim();
                } else if (trimmed.startsWith("__CMDZEN_CMD_START__")) {
                } else {
                    output.append(line).append("\n");
                }
            }

            // Display command
            sb.append("Command:\n");
            sb.append("  $ ").append(command != null ? command : lastCommand).append("\n\n");

            // Display exit code
            sb.append("Exit Code: ").append(code).append("\n");

            // Display timestamp if available
            if (timestamp != null && !timestamp.isEmpty()) {
                sb.append("Time: ").append(timestamp).append("\n");
            }

            sb.append("\n");

            // Display output
            if (output.length() > 0) {
                sb.append("Output:\n");
                sb.append("───────────────────────────────────────────────────────\n");
                sb.append(output.toString().trim());
                sb.append("\n───────────────────────────────────────────────────────\n");
            } else {
                sb.append("(No output captured)\n");
            }

            return sb.toString();

        } catch (Exception e) {
            log.error("Error showing last failed command", e);
            return "Error retrieving last failed command: " + e.getMessage();
        }
    }
}
