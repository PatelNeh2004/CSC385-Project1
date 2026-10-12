package csc241.project1.io;

public final class WorldFormatException extends Exception {
    private static final long serialVersionUID = 1L;

    public WorldFormatException(String message) { super(message); }
    public WorldFormatException(String message, Throwable cause) { super(message, cause); }
}
