package csc241.project1.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Room {
    public static final int CAPACITY = 10;
    private final String name;
    private final String description;
    private RoomState state;
    private final Map<Direction, Room> neighbors = new EnumMap<>(Direction.class);
    private final List<Creature> occupants = new ArrayList<>();

    public Room(String name, String description, RoomState state) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Room name is required.");
        this.name = name.trim();
        this.description = Objects.requireNonNull(description, "Room description is required.");
        this.state = Objects.requireNonNull(state, "Room state is required.");
    }

    public String name() { return name; }
    public String description() { return description; }
    public RoomState state() { return state; }
    public boolean isFull() { return occupants.size() >= CAPACITY; }
    public List<Creature> occupants() { return Collections.unmodifiableList(occupants); }
    public Map<Direction, Room> neighbors() { return Collections.unmodifiableMap(neighbors); }
    public Room neighbor(Direction direction) { return neighbors.get(direction); }
    public void connect(Direction direction, Room destination) {
        neighbors.put(Objects.requireNonNull(direction), Objects.requireNonNull(destination));
    }

    public boolean apply(RoomAction action) {
        RoomState next = state.after(Objects.requireNonNull(action));
        boolean changed = next != state;
        state = next;
        return changed;
    }

    void add(Creature creature) {
        if (isFull()) throw new IllegalStateException("Room full.");
        occupants.add(creature);
    }
    void remove(Creature creature) { occupants.remove(creature); }
}
