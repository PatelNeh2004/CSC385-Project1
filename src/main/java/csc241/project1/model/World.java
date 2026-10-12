package csc241.project1.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Owns the registry and keeps room membership and creature locations in sync. */
public final class World {
    private final Map<String, Room> rooms = new LinkedHashMap<>();
    private final Map<String, Creature> creatures = new LinkedHashMap<>();
    private Player player;

    private static String key(String name) { return name.trim().toLowerCase(Locale.ROOT); }

    private void checkName(String name) {
        if (rooms.containsKey(key(name)) || creatures.containsKey(key(name))) {
            throw new IllegalArgumentException("Duplicate name: " + name);
        }
    }

    public void addRoom(Room room) {
        checkName(room.name());
        rooms.put(key(room.name()), room);
    }

    public void addCreature(Creature creature, Room room) {
        checkName(creature.name());
        if (rooms.get(key(room.name())) != room) throw new IllegalArgumentException("Unknown room.");
        if (creature.room() != null) throw new IllegalArgumentException("Creature is already placed.");
        if (creature.isPlayer() && player != null) throw new IllegalArgumentException("Exactly one PC is allowed.");
        if (room.isFull()) throw new IllegalArgumentException("Room full: " + room.name());
        room.add(creature);
        creature.setRoom(room);
        creatures.put(key(creature.name()), creature);
        if (creature.isPlayer()) player = (Player) creature;
    }

    public Room room(String name) { return rooms.get(key(name)); }
    public Creature creature(String name) { return creatures.get(key(name)); }
    public Player player() { return player; }
    public Collection<Room> rooms() { return Collections.unmodifiableCollection(rooms.values()); }
    public Collection<Creature> creatures() { return Collections.unmodifiableCollection(creatures.values()); }

    public boolean move(Creature creature, Room destination) {
        if (creatures.get(key(creature.name())) != creature || destination == null
                || rooms.get(key(destination.name())) != destination) {
            throw new IllegalArgumentException("Creature and destination must belong to this world.");
        }
        if (creature.room() == destination) return true;
        if (destination.isFull()) return false;
        creature.room().remove(creature);
        destination.add(creature);
        creature.setRoom(destination);
        return true;
    }

    public void remove(Creature creature) {
        if (creature.isPlayer()) throw new IllegalArgumentException("The PC cannot leave through the roof.");
        if (creatures.get(key(creature.name())) != creature) return;
        creature.room().remove(creature);
        creature.setRoom(null);
        creatures.remove(key(creature.name()));
    }
}
