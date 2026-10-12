package csc241.project1.game;

import csc241.project1.cli.Command;
import csc241.project1.model.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

/** All game rules live here; the console only reads commands and displays events. */
public final class GameEngine {
    public static final String HELP = """
            Commands (case-insensitive; names can contain spaces):
              look                    Describe your room, exits, and other creatures.
              clean room              Clean your room by one state.
              dirty room              Dirty your room by one state.
              go <direction>          Move yourself north, south, east, or west.
              <name> clean room       Order a creature here to clean the room.
              <name> dirty room       Order a creature here to dirty the room.
              <name> leave <direction> Order a creature here to move.
              <name> look             Show the room from that creature's viewpoint.
              respect                 Show your current respect.
              help                    Show these commands.
              exit                    End the game (Exit and quit also work).
            Direction shortcuts: n, s, e, w. A direction alone also moves you.
            go, leave, and move are interchangeable. Quotes around names are optional.
            Example: Mary clean room; Peter leave north; \"Old Dog\" look.
            Respect starts at 40. Win above 80; lose at 0 or below.
            """;

    private final World world;
    private final Random random;
    private final Consumer<String> output;
    private GameStatus status = GameStatus.PLAYING;

    public GameEngine(World world, Random random, Consumer<String> output) {
        this.world = Objects.requireNonNull(world);
        this.random = Objects.requireNonNull(random);
        this.output = Objects.requireNonNull(output);
        if (world.player() == null) throw new IllegalArgumentException("A PC is required.");
    }

    public GameStatus status() { return status; }
    public boolean isRunning() { return status == GameStatus.PLAYING; }

    public void execute(Command command) {
        if (!isRunning()) return;
        switch (command.kind()) {
            case HELP: output.accept(HELP); return;
            case RESPECT: showRespect(); return;
            case EXIT:
                status = GameStatus.EXITED;
                output.accept("Goodbye! Final respect: " + world.player().respect() + ".");
                return;
            default: break;
        }
        Creature actor = world.player();
        if (command.actorName() != null) {
            actor = world.creature(command.actorName());
            if (actor == null || actor.room() != world.player().room()) {
                output.accept("There is no creature named \"" + command.actorName() + "\" in this room.");
                return;
            }
        }
        switch (command.kind()) {
            case LOOK: look(actor); return;
            case CLEAN: changeRoom(actor, RoomAction.CLEAN); break;
            case DIRTY: changeRoom(actor, RoomAction.DIRTY); break;
            case MOVE: orderedMove(actor, command.direction()); break;
            default: throw new IllegalArgumentException("Unsupported command.");
        }
        showRespect();
        checkEnding();
    }

    public void look(Creature viewer) {
        Room room = viewer.room();
        output.accept(room.name() + " | " + room.state() + " | " + room.description());
        List<String> exits = new ArrayList<>();
        for (Map.Entry<Direction, Room> exit : room.neighbors().entrySet()) {
            exits.add(exit.getKey() + " -> " + exit.getValue().name());
        }
        output.accept("Exits: " + (exits.isEmpty() ? "none" : String.join(", ", exits)));
        output.accept("Occupancy: " + room.occupants().size() + "/" + Room.CAPACITY + ".");
        boolean hasOthers = false;
        for (Creature creature : room.occupants()) {
            if (creature == viewer) continue;
            output.accept(creature.name() + " | " + creature.type() + " | " + creature.description());
            hasOthers = true;
        }
        if (!hasOthers) output.accept("No other creatures are here.");
        showRespect();
    }

    private void changeRoom(Creature actor, RoomAction action) {
        Room room = actor.room();
        RoomState before = room.state();
        if (!room.apply(action)) {
            output.accept(room.name() + " is already " + room.state() + "; nothing changes.");
            return;
        }
        output.accept(actor.name() + (action == RoomAction.CLEAN ? " cleans " : " dirties ")
                + room.name() + ": " + before + " -> " + room.state() + ".");

        // Snapshot before moving: every original occupant reacts before any departures.
        List<Creature> originalOccupants = new ArrayList<>(room.occupants());
        for (Creature creature : originalOccupants) {
            int times = creature == actor && !actor.isPlayer() ? 3 : 1;
            react(creature, creature.reactionTo(action), times);
        }
        for (Creature creature : originalOccupants) {
            if (creature.room() == room && !creature.accepts(room.state())) flee(creature);
        }
    }

    private void orderedMove(Creature creature, Direction direction) {
        Room source = creature.room();
        Room destination = source.neighbor(direction);
        if (destination == null) {
            output.accept("There is no door " + direction + " from " + source.name() + ".");
            return;
        }
        if (!world.move(creature, destination)) {
            output.accept("Room full. " + destination.name() + " cannot admit " + creature.name() + ".");
            react(creature, creature.discontentReaction(), 1);
            return;
        }
        announceMove(creature, source, destination, direction);
        makeComfortable(creature);
    }

    private void flee(Creature creature) {
        Room source = creature.room();
        List<Map.Entry<Direction, Room>> exits = new ArrayList<>(source.neighbors().entrySet());
        Collections.shuffle(exits, random);
        for (Map.Entry<Direction, Room> exit : exits) {
            if (world.move(creature, exit.getValue())) {
                announceMove(creature, source, exit.getValue(), exit.getKey());
                makeComfortable(creature);
                return;
            }
            output.accept("Room full. " + creature.name() + " cannot enter " + exit.getValue().name()
                    + " and tries another door.");
        }
        world.remove(creature);
        output.accept(creature.name() + " drills a hole in the ceiling and leaves the house through the roof"
                + (exits.isEmpty() ? " because there are no exits." : " because all adjacent rooms are full."));

        // Removal happens first. Creatures that already left cannot add a roof penalty.
        for (Creature remaining : new ArrayList<>(world.player().room().occupants())) {
            react(remaining, remaining.discontentReaction(), 1);
        }
    }

    private void makeComfortable(Creature creature) {
        Room room = creature.room();
        if (creature.accepts(room.state())) return;
        RoomState before = room.state();
        RoomAction action = creature.preferredAction();
        room.apply(action);
        output.accept(creature.name() + (action == RoomAction.CLEAN ? " cleans " : " dirties ")
                + room.name() + " on arrival: " + before + " -> " + room.state() + ".");
        // One step from an unacceptable extreme reaches half-dirty, acceptable to everyone.
        // This autonomous arrival action is not a PC cleaning/dirtying command.
    }

    private void announceMove(Creature creature, Room source, Room destination, Direction direction) {
        output.accept(creature.name() + " leaves " + source.name() + " " + direction
                + " and enters " + destination.name() + ".");
    }

    private void react(Creature creature, Reaction reaction, int times) {
        if (reaction == null || creature.room() != world.player().room()) return;
        for (int i = 0; i < times; i++) {
            world.player().changeRespect(reaction.respectChange());
            output.accept(creature.name() + " the " + creature.type() + " " + reaction.message() + ".");
        }
    }

    private void showRespect() { output.accept("Your respect is now " + world.player().respect() + "."); }

    private void checkEnding() {
        if (world.player().respect() <= 0) {
            status = GameStatus.DISGRACED;
            output.accept("Shame on you! You leave in disgrace.");
        } else if (world.player().respect() > 80) {
            status = GameStatus.PRAISED;
            output.accept("Congratulations, you are praised!");
        }
    }
}
