package io.cmdzen.cli.commands;

import io.cmdzen.cli.services.AIService;
import io.cmdzen.cli.services.ShellService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;

@Command
@Slf4j
public class SolveCommand {

    private ShellService  shellService;
    private AIService  aiService;

    public SolveCommand(ShellService shellService, AIService aiService) {
        this.shellService = shellService;
        this.aiService = aiService;
    }

    @Command(command = "solve",
            alias = "-s",
            description = "Analyze and suggest fixes for failed commands.")
    public String solve(
            @Option(description = "Additional context or question", defaultValue = "Help me fix this error") String prompt
    ){
        try {
            // Get last command from history
            String lastCommand = shellService.getLastCommand();
            if (lastCommand == null || lastCommand.isEmpty()) {
                return "No previous command found in history.";
            }

            log.debug("Last command: {}", lastCommand);

            // Try to get captured output first (NEW!)
            String capturedOutput = shellService.getLastCommandOutput();
            int exitCode = shellService.getLastExitCode();

            if (capturedOutput != null && exitCode != 0) {
                // Use captured output from shell integration
                log.info("Using captured output from shell integration");
                System.out.println("Analyzing failed command: " + lastCommand);
                System.out.println("Exit code: " + exitCode);
                System.out.println("Captured output:");
                System.out.println("------------------------------");
                System.out.println(stripMetadata(capturedOutput));
                System.out.println("------------------------------");

                String aiPrompt = buildPrompt(lastCommand, exitCode, capturedOutput, prompt, true);
                return aiService.ask(aiPrompt);

            } else {
                // Fallback: re-run command (current behavior)
                log.info("No captured output found, re-running command");
                System.out.println("Re-running command to capture output: " + lastCommand);

                ProcessBuilder pb = new ProcessBuilder("sh", "-c", lastCommand);
                pb.redirectErrorStream(true);
                Process process = pb.start();

                String output = new String(process.getInputStream().readAllBytes());
                int code = process.waitFor();

                System.out.println("Exit code: " + code);
                log.debug("Command output: {}", output);

                String aiPrompt = buildPrompt(lastCommand, code, output, prompt, false);
                return aiService.ask(aiPrompt);
            }

        } catch (Exception e) {
            log.error("Error in solve command", e);
            return "Error analyzing command: " + e.getMessage();
        }
    }

    private String stripMetadata(String captured) {
        return captured.lines()
                .filter(line -> !line.startsWith("__CMDZEN_CMD_START__"))
                .filter(line -> !line.startsWith("EXIT_CODE:"))
                .filter(line -> !line.startsWith("COMMAND:"))
                .filter(line -> !line.startsWith("TIMESTAMP:"))
                .reduce("", (a, b) -> a + b + "\n")
                .trim();
    }


    private String buildPrompt(String command,
                               int exitCode,
                               String output,
                               String userPrompt,
                               boolean fromCapture) {

        StringBuilder sb = new StringBuilder();
        sb.append("A Linux command ");
        sb.append(exitCode != 0 ? "failed and I need help fixing it.\n\n"
                : "was run and I need an explanation.\n\n");

        sb.append("COMMAND: ").append(command).append("\n");
        sb.append("EXIT CODE: ").append(exitCode).append("\n\n");

        sb.append(fromCapture ? "CAPTURED ERROR INFO:\n" : "COMMAND OUTPUT:\n");
        sb.append(output).append("\n\n");

        sb.append("USER QUESTION: ").append(userPrompt).append("\n\n");

        if (exitCode != 0) {
            sb.append("Please:\n");
            sb.append("1. Explain what went wrong\n");
            sb.append("2. Provide the corrected command\n");
            sb.append("3. Explain why the fix works\n");
        } else {
            sb.append("Please explain what this command does and the output.\n");
        }

        return sb.toString();
    }

}