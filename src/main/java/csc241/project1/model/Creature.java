package csc241.project1.model;

import java.util.Objects;

/** Creature-specific behavior is supplied by each concrete subtype. */
public abstract class Creature {
    private final String name;
    private final String description;
    private Room room;

    protected Creature(String name, String description) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Creature name is required.");
        this.name = name.trim();
        this.description = Objects.requireNonNull(description, "Creature description is required.");
    }

    public final String name() { return name; }
    public final String description() { return description; }
    public final Room room() { return room; }
    final void setRoom(Room room) { this.room = room; }

    public abstract String type();
    public abstract boolean accepts(RoomState state);
    public abstract RoomAction preferredAction();
    public abstract Reaction reactionTo(RoomAction action);
    public abstract Reaction discontentReaction();
    public boolean isPlayer() { return false; }
}
