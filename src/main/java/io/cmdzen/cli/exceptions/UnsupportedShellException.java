package io.cmdzen.cli.exceptions;

public class UnsupportedShellException extends RuntimeException {
    public UnsupportedShellException(String message) {
        super(message);
    }
}