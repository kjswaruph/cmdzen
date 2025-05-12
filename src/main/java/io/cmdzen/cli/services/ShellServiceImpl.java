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

    private History jLineHistory;
    private ShellEnvironment shellEnvironment;

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
            Path path =  shellEnvironment.getHistoryPath();
            List<String> historyLines = Files.readAllLines(path);
            return new CommandHistory(historyLines.size(), historyLines);
        } catch (IOException e) {
            log.error("Failed to read shell history from file: {}", shellEnvironment.getHistoryPath(), e);
            throw e;
        }
    }

    @Override
    public Path getConfigPath() {
        return shellEnvironment.getConfigPath();
    }

}
