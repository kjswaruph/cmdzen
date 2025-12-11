package io.cmdzen.cli.services;

import io.cmdzen.cli.model.CommandHistory;
import io.cmdzen.cli.model.ShellEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.jline.reader.History;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ShellServiceImpl implements ShellService {

    private final History jLineHistory;
    private final ShellEnvironment shellEnvironment;

    public ShellServiceImpl(History jLineHistory) {
        this.jLineHistory = jLineHistory;
        this.shellEnvironment = new ShellEnvironment();
    }

    @Override
    public ShellEnvironment getShellEnvironment() {
        return shellEnvironment;
    }

    @Override
    public CommandHistory getCommandHistory() {
        ArrayList<String> history = new ArrayList<>();
        for (int i = 0; i < jLineHistory.size(); i++) {
            history.add(jLineHistory.get(i));
        }
        return new CommandHistory(history.size(), history);
    }

    @Override
    public CommandHistory getLocalCommandHistory() throws IOException {
        try {
            Path path = shellEnvironment.getHistoryPath();
            List<String> historyLines = Files.readAllLines(path);
            return new CommandHistory(historyLines.size(), historyLines);
        } catch (IOException e) {
            log.error("Failed to read shell history from file: {}", shellEnvironment.getHistoryPath(), e);
            throw e;
        }
    }

    private String resolveShellPid() {
        String fromEnv = System.getenv("CMDZEN_SHELL_PID");
        if (fromEnv != null && !fromEnv.isBlank()) {
            log.debug("Using CMDZEN_SHELL_PID from environment: {}", fromEnv);
            return fromEnv.trim();
        }

        String parent = getParentShellPid();
        log.debug("CMDZEN_SHELL_PID not set, falling back to parent PID: {}", parent);
        return parent;
    }

    @Override
    public String getLastCommand() {
        String output = getLastCommandOutput();
        if (output != null) {
            String capturedCmd = output.lines()
                    .filter(line -> line.startsWith("COMMAND:"))
                    .map(line -> line.substring("COMMAND:".length()).trim())
                    .filter(cmd -> !cmd.isEmpty())
                    .findFirst()
                    .orElse(null);

            if (capturedCmd != null &&
                    !capturedCmd.startsWith("cmdzen") &&
                    !capturedCmd.startsWith("./cmdzen-cli")) {
                return capturedCmd;
            }
        }

        try {
            CommandHistory commandHistory = getLocalCommandHistory();
            List<String> history = commandHistory.getHistory();
            for (int i = history.size() - 1; i >= 0; i--) {
                String cmd = history.get(i);
                if (!cmd.isEmpty() && !cmd.startsWith("cmdzen") && !cmd.startsWith("./cmdzen-cli")) {
                    return cmd;
                }
            }
        } catch (IOException e) {
            log.error("Failed to read shell history from file: {}", shellEnvironment.getHistoryPath(), e);
        }

        if (jLineHistory != null && jLineHistory.size() > 0) {
            for (int i = jLineHistory.size() - 1; i >= 0; i--) {
                String cmd = jLineHistory.get(i);
                if (!cmd.isEmpty() && !cmd.startsWith("cmdzen") && !cmd.startsWith("./cmdzen-cli")) {
                    return cmd;
                }
            }
        }

        return "";
    }

    @Override
    public Path getConfigPath() {
        return shellEnvironment.getConfigPath();
    }

    @Override
    public String getLastCommandOutput() {
        String shellPid = resolveShellPid();
        Path outputFile = Path.of("/tmp/cmdzen_last_output_" + shellPid + ".txt");

        if (!Files.exists(outputFile)) {
            log.debug("No output file found at: {}", outputFile);
            return null;
        }

        try {
            String content = Files.readString(outputFile);
            log.debug("Read output file: {}", outputFile);
            return content;
        } catch (IOException e) {
            log.error("Failed to read command output from: {}", outputFile, e);
            return null;
        }
    }

    @Override
    public int getLastExitCode() {
        String output = getLastCommandOutput();
        if (output == null) return 0;

        return output.lines()
                .filter(line -> line.startsWith("EXIT_CODE:"))
                .map(line -> line.substring("EXIT_CODE:".length()).trim())
                .mapToInt(Integer::parseInt)
                .findFirst()
                .orElse(0);
    }

    @Override
    public boolean injectShellIntegration() {
        Path configPath = shellEnvironment.getConfigPath();
        String shellName = shellEnvironment.getShellName();

        try {
            List<String> lines = Files.exists(configPath)
                    ? Files.readAllLines(configPath)
                    : new ArrayList<>();

            boolean alreadyExists = lines.stream()
                    .anyMatch(line -> line.contains("# CmdZen integration"));

            if (alreadyExists) {
                log.info("CmdZen integration already exists in {}", configPath);
                return true;
            }

            List<String> integrationLines = getShellIntegrationCode(shellName);
            lines.addAll(integrationLines);

            Files.write(configPath, lines);
            log.info("Successfully injected CmdZen integration into {}", configPath);
            return true;

        } catch (IOException e) {
            log.error("Failed to inject shell integration into: {}", configPath, e);
            return false;
        }
    }

    private String getParentShellPid() {
        try {
            ProcessHandle current = ProcessHandle.current();
            ProcessHandle parent = current.parent().orElse(null);
            if (parent != null) {
                return String.valueOf(parent.pid());
            }
        } catch (Exception e) {
            log.warn("Could not determine parent PID, using current: {}", e.getMessage());
        }
        return String.valueOf(ProcessHandle.current().pid());
    }

    private List<String> getShellIntegrationCode(String shellName) {
        List<String> lines = new ArrayList<>();
        lines.add("");
        lines.add("# CmdZen integration - Auto-generated");
        lines.add("# Capture last failed command + its output for 'cmdzen solve'");

        switch (shellName.toLowerCase()) {
            case "bash":
                lines.addAll(List.of(
                        "",
                        "export CMDZEN_SHELL_PID=$$",
                        "CMDZEN_SESSION_LOG=\"/tmp/cmdzen_session_$$.log\"",
                        "CMDZEN_OUTPUT_FILE=\"/tmp/cmdzen_last_output_$$.txt\"",
                        "CMDZEN_IN_PROMPT=0",
                        "",
                        "# Start capturing all stdout+stderr of this shell",
                        "cmdzen_start_capture() {",
                        "    [[ -n \"$CMDZEN_CAPTURE_ACTIVE\" ]] && return",
                        "    CMDZEN_CAPTURE_ACTIVE=1",
                        "    exec > >(tee -a \"$CMDZEN_SESSION_LOG\") 2>&1",
                        "}",
                        "cmdzen_start_capture",
                        "",
                        "# Mark the start of each (non-CmdZen) command in the session log",
                        "cmdzen_debug_trap() {",
                        "    if [ \"$CMDZEN_IN_PROMPT\" = \"1\" ]; then",
                        "       return",
                        "    fi",
                        "    case \"$BASH_COMMAND\" in",
                        "        cmdzen*|./cmdzen-cli*|history* )",
                        "            return",
                        "            ;;",
                        "    esac",
                        "    echo \"__CMDZEN_CMD_START__ $(date +%s)\" >> \"$CMDZEN_SESSION_LOG\"",
                        "}",
                        "trap cmdzen_debug_trap DEBUG",
                        "",
                        "# After each command, if it failed, snapshot its output into CMDZEN_OUTPUT_FILE",
                        "cmdzen_prompt_hook() {",
                        "    local exit_code=$?",
                        "    local last_cmd",
                        "    last_cmd=$(history 1 | sed 's/^[ ]*[0-9]\\+[ ]*//')",
                        "",
                        "    if [ $exit_code -ne 0 ] &&",
                        "       [ -n \"$last_cmd\" ] &&",
                        "       [[ \"$last_cmd\" != cmdzen* ]] &&",
                        "       [[ \"$last_cmd\" != ./cmdzen-cli* ]]; then",
                        "",
                        "        # Find the last marker and copy everything from there into OUTPUT_FILE",
                        "        local last_line",
                        "        last_line=$(grep -n \"__CMDZEN_CMD_START__\" \"$CMDZEN_SESSION_LOG\" 2>/dev/null | tail -n 1 | cut -d: -f1)",
                        "",
                        "        if [ -n \"$last_line\" ]; then",
                        "            tail -n +\"$last_line\" \"$CMDZEN_SESSION_LOG\" > \"$CMDZEN_OUTPUT_FILE\"",
                        "        else",
                        "            cp \"$CMDZEN_SESSION_LOG\" \"$CMDZEN_OUTPUT_FILE\" 2>/dev/null || true",
                        "        fi",
                        "",
                        "        {",
                        "            echo \"EXIT_CODE: $exit_code\"",
                        "            echo \"COMMAND: $last_cmd\"",
                        "            echo \"TIMESTAMP: $(date)\"",
                        "        } >> \"$CMDZEN_OUTPUT_FILE\"",
                        "    fi",
                        "}",
                        "PROMPT_COMMAND='CMDZEN_IN_PROMPT=1; cmdzen_prompt_hook; CMDZEN_IN_PROMPT=0'"
                ));
                break;

            case "zsh":
                lines.addAll(List.of(
                        "",
                        "export CMDZEN_SHELL_PID=$$",
                        "CMDZEN_SESSION_LOG=\"/tmp/cmdzen_session_$$.log\"",
                        "CMDZEN_OUTPUT_FILE=\"/tmp/cmdzen_last_output_$$.txt\"",
                        "",
                        "# Start capturing all stdout+stderr of this shell",
                        "cmdzen_start_capture() {",
                        "    [[ -n \"$CMDZEN_CAPTURE_ACTIVE\" ]] && return",
                        "    CMDZEN_CAPTURE_ACTIVE=1",
                        "    exec > >(tee -a \"$CMDZEN_SESSION_LOG\") 2>&1",
                        "}",
                        "cmdzen_start_capture",
                        "",
                        "# Mark the start of each command",
                        "preexec() {",
                        "    case \"$1\" in",
                        "        cmdzen*|./cmdzen-cli*|history* )",
                        "            return",
                        "            ;;",
                        "    esac",
                        "    CMDZEN_LAST_CMD=\"$1\"",
                        "    echo \"__CMDZEN_CMD_START__ $(date +%s)\" >> \"$CMDZEN_SESSION_LOG\"",
                        "}",
                        "",
                        "# After each command, capture failed output",
                        "precmd() {",
                        "    local exit_code=$?",
                        "    if [ $exit_code -ne 0 ] && [ -n \"$CMDZEN_LAST_CMD\" ] &&",
                        "       [[ \"$CMDZEN_LAST_CMD\" != cmdzen* ]] &&",
                        "       [[ \"$CMDZEN_LAST_CMD\" != ./cmdzen-cli* ]]; then",
                        "",
                        "        local last_line",
                        "        last_line=$(grep -n \"__CMDZEN_CMD_START__\" \"$CMDZEN_SESSION_LOG\" 2>/dev/null | tail -n 1 | cut -d: -f1)",
                        "",
                        "        if [ -n \"$last_line\" ]; then",
                        "            tail -n +\"$last_line\" \"$CMDZEN_SESSION_LOG\" > \"$CMDZEN_OUTPUT_FILE\"",
                        "        else",
                        "            cp \"$CMDZEN_SESSION_LOG\" \"$CMDZEN_OUTPUT_FILE\" 2>/dev/null || true",
                        "        fi",
                        "",
                        "        {",
                        "            echo \"EXIT_CODE: $exit_code\"",
                        "            echo \"COMMAND: $CMDZEN_LAST_CMD\"",
                        "            echo \"TIMESTAMP: $(date)\"",
                        "        } >> \"$CMDZEN_OUTPUT_FILE\"",
                        "    fi",
                        "}"
                ));
                break;

            case "fish":
                lines.addAll(List.of(
                        "",
                        "set -gx CMDZEN_SHELL_PID $fish_pid",
                        "set -g CMDZEN_OUTPUT_FILE \"/tmp/cmdzen_last_output_$fish_pid.txt\"",
                        "",
                        "# Fish: capture exit code + command only (no full output capture)",
                        "function cmdzen_capture --on-event fish_postexec",
                        "    set -l exit_code $status",
                        "    if test $exit_code -ne 0",
                        "        and not string match -q \"cmdzen*\" -- $argv",
                        "        and not string match -q \"./cmdzen-cli*\" -- $argv",
                        "        echo \"EXIT_CODE: $exit_code\" > $CMDZEN_OUTPUT_FILE",
                        "        echo \"COMMAND: $argv\" >> $CMDZEN_OUTPUT_FILE",
                        "        echo \"TIMESTAMP: (date)\" >> $CMDZEN_OUTPUT_FILE",
                        "        echo \"Note: Fish shell - full output capture not available\" >> $CMDZEN_OUTPUT_FILE",
                        "    end",
                        "end"
                ));
                break;

            case "ksh":
                lines.addAll(List.of(
                        "",
                        "export CMDZEN_SHELL_PID=$$",
                        "CMDZEN_SESSION_LOG=\"/tmp/cmdzen_session_$$.log\"",
                        "CMDZEN_OUTPUT_FILE=\"/tmp/cmdzen_last_output_$$.txt\"",
                        "",
                        "# Start capturing all output",
                        "cmdzen_start_capture() {",
                        "    [ -n \"$CMDZEN_CAPTURE_ACTIVE\" ] && return",
                        "    CMDZEN_CAPTURE_ACTIVE=1",
                        "    exec > >(tee -a \"$CMDZEN_SESSION_LOG\") 2>&1",
                        "}",
                        "cmdzen_start_capture",
                        "",
                        "# Mark command start",
                        "trap 'case \"$_\" in cmdzen*|./cmdzen-cli*|history*) ;; *) echo \"__CMDZEN_CMD_START__ $(date +%s)\" >> \"$CMDZEN_SESSION_LOG\"; CMDZEN_LAST_CMD=\"$_\";; esac' DEBUG",
                        "",
                        "# Capture on prompt",
                        "function cmdzen_capture {",
                        "    local exit_code=$?",
                        "    if [ $exit_code -ne 0 ] && [ -n \"$CMDZEN_LAST_CMD\" ] &&",
                        "       [[ \"$CMDZEN_LAST_CMD\" != cmdzen* ]] &&",
                        "       [[ \"$CMDZEN_LAST_CMD\" != ./cmdzen-cli* ]]; then",
                        "",
                        "        local last_line",
                        "        last_line=$(grep -n \"__CMDZEN_CMD_START__\" \"$CMDZEN_SESSION_LOG\" 2>/dev/null | tail -n 1 | cut -d: -f1)",
                        "",
                        "        if [ -n \"$last_line\" ]; then",
                        "            tail -n +\"$last_line\" \"$CMDZEN_SESSION_LOG\" > \"$CMDZEN_OUTPUT_FILE\"",
                        "        fi",
                        "",
                        "        {",
                        "            echo \"EXIT_CODE: $exit_code\"",
                        "            echo \"COMMAND: $CMDZEN_LAST_CMD\"",
                        "            echo \"TIMESTAMP: $(date)\"",
                        "        } >> \"$CMDZEN_OUTPUT_FILE\"",
                        "    fi",
                        "}",
                        "PS1=\"$(cmdzen_capture)$PS1\""
                ));
                break;

            default:
                log.warn("Unknown shell type: {}, using minimal integration", shellName);
                lines.addAll(List.of(
                        "",
                        "export CMDZEN_SHELL_PID=$$",
                        "CMDZEN_OUTPUT_FILE=\"/tmp/cmdzen_last_output_$$.txt\"",
                        "# Basic capture only - full integration not available for this shell"
                ));
                break;
        }

        lines.add("# End CmdZen integration");
        lines.add("");

        return lines;
    }

}
