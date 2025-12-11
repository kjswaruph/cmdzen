package io.cmdzen.cli.exceptions;

public class ShellVariableMissingException extends RuntimeException {
    public ShellVariableMissingException(String message) {
        super(message);
    }
}
