package io.cmdzen.cli.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CommandHistory {
    private int count;
    private List<String> history;

    public CommandHistory(int size, List<String> history) {
        this.history = history;
        this.count = size;
    }

    public void getCommandHistory() {
        for (String command : history) {
            System.out.println(command);
        }
    }
}
