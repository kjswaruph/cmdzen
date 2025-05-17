package io.cmdzen.cli.commands;

import io.cmdzen.cli.services.ShellService;
import org.springframework.shell.command.annotation.Command;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Command
public class SolveCommand {

    private String lastOutput = "";

    private ShellService  shellService;

    public SolveCommand(ShellService shellService) {
        this.shellService = shellService;
    }

    @Command(command = "solve",
            alias = "-s",
            description = "Capture output and suggest fixes.")
    public String solve(){
        try {
            String command = shellService.getLocalCommandHistory().getHistory().getLast();
            System.out.println("Last command: " + command);
            List<String> commandParts = Arrays.asList(command.split("\\s+"));
            ProcessBuilder builder = new ProcessBuilder(commandParts)
                    .redirectErrorStream(true);
            Process process = builder.start();
            String output = new String(process.getInputStream().readAllBytes());
            lastOutput = output;
            System.out.println("Output from process are: " + output);
            return output;
        } catch (IOException e) {
            lastOutput = e.getMessage();
            return "Error: " + e.getMessage();
        }
    }

    @Command(command = "last",
            description = "Show output of previous command.")
    public String last() {
        return lastOutput.isEmpty() ? "No previous output." : lastOutput;
    }
}