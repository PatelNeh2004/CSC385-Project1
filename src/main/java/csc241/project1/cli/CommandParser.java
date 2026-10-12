package csc241.project1.cli;

import csc241.project1.model.Direction;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CommandParser {
    private static final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
    private static final Pattern ACTION = Pattern.compile("(?:(.+?)\\s+)?(clean|dirty)\\s+room", FLAGS);
    private static final Pattern MOVE = Pattern.compile(
            "(?:(.+?)\\s+)?(?:go|leave|move)\\s+(north|south|east|west|n|s|e|w)", FLAGS);
    private static final Pattern LOOK = Pattern.compile("(.+?)\\s+look", FLAGS);

    public Command parse(String input) {
        String text = input.trim();
        switch (text.toLowerCase(Locale.ROOT)) {
            case "look": return new Command(Command.Kind.LOOK, null, null);
            case "help": return new Command(Command.Kind.HELP, null, null);
            case "respect": return new Command(Command.Kind.RESPECT, null, null);
            case "exit": case "quit": return new Command(Command.Kind.EXIT, null, null);
            case "north": case "south": case "east": case "west":
            case "n": case "s": case "e": case "w":
                return new Command(Command.Kind.MOVE, null, Direction.parse(text));
            default: break;
        }
        Matcher action = ACTION.matcher(text);
        if (action.matches()) {
            Command.Kind kind = action.group(2).equalsIgnoreCase("clean") ? Command.Kind.CLEAN : Command.Kind.DIRTY;
            return new Command(kind, name(action.group(1)), null);
        }
        Matcher move = MOVE.matcher(text);
        if (move.matches()) return new Command(Command.Kind.MOVE, name(move.group(1)), Direction.parse(move.group(2)));
        Matcher look = LOOK.matcher(text);
        if (look.matches()) return new Command(Command.Kind.LOOK, name(look.group(1)), null);
        throw new IllegalArgumentException("Unknown command. Type help for the command list.");
    }

    private String name(String text) {
        if (text == null) return null;
        String result = text.trim();
        if (result.length() >= 2 && result.startsWith("\"") && result.endsWith("\"")) {
            result = result.substring(1, result.length() - 1).trim();
        }
        if (result.isEmpty()) throw new IllegalArgumentException("A creature name is required.");
        return result;
    }
}
