package io.cmdzen.cli.services;

import io.cmdzen.cli.model.CommandHistory;
import io.cmdzen.cli.model.ShellEnvironment;

import java.io.IOException;
import java.nio.file.Path;

public interface ShellService {

    ShellEnvironment getShellEnvironment();
    CommandHistory getCommandHistory();
    CommandHistory getLocalCommandHistory() throws IOException;
    Path getConfigPath();

}
