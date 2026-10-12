package csc241.project1.model;

import java.util.Locale;

public enum Direction {
    NORTH, SOUTH, EAST, WEST;

    public static Direction parse(String text) {
        switch (text.trim().toLowerCase(Locale.ROOT)) {
            case "n": case "north": return NORTH;
            case "s": case "south": return SOUTH;
            case "e": case "east": return EAST;
            case "w": case "west": return WEST;
            default: throw new IllegalArgumentException("Use north, south, east, or west.");
        }
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
