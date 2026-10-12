package csc241.project1.model;

import java.util.Locale;

public enum RoomState {
    CLEAN("clean"), HALF_DIRTY("half-dirty"), DIRTY("dirty");

    private final String label;

    RoomState(String label) { this.label = label; }

    public static RoomState parse(String text) {
        for (RoomState state : values()) {
            if (state.label.equals(text.trim().toLowerCase(Locale.ROOT))) return state;
        }
        throw new IllegalArgumentException("Room state must be clean, half-dirty, or dirty.");
    }

    public RoomState after(RoomAction action) {
        int next = ordinal() + (action == RoomAction.CLEAN ? -1 : 1);
        return values()[Math.max(0, Math.min(values().length - 1, next))];
    }

    @Override
    public String toString() { return label; }
}
