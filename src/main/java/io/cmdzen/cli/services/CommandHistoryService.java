package io.cmdzen.cli.services;

import io.cmdzen.cli.model.CommandHistory;

import java.io.IOException;

public interface CommandHistoryService {

    CommandHistory getCommandHistory();
    CommandHistory getLocalCommandHistory() throws IOException;

}
