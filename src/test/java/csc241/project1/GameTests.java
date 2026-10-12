package csc241.project1;

import csc241.project1.cli.Command;
import csc241.project1.cli.CommandParser;
import csc241.project1.game.GameEngine;
import csc241.project1.game.GameStatus;
import csc241.project1.io.WorldFormatException;
import csc241.project1.io.WorldLoader;
import csc241.project1.model.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Dependency-free integration tests; failures throw even when assertions are disabled. */
public final class GameTests {
    private static int passed;

    public static List<TestCase> cases() throws Exception {
        List<TestCase> result = new ArrayList<>();
        result.add(new TestCase("provided XML, forward references, and starting respect", () -> {
            World world = new WorldLoader().load(Path.of("input.xml"));
            equal(10, world.rooms().size());
            equal(27, world.creatures().size());
            equal("Squash", world.player().name());
            equal(40, world.player().respect());
            equal(world.room("Pig"), world.room("Violet").neighbor(Direction.NORTH));
        }));
        result.add(new TestCase("room transitions clamp at both boundaries without reactions", () -> {
            Fixture f = new Fixture(RoomState.DIRTY);
            f.add(new Animal("Dog", "a dog"));
            f.command("dirty room");
            equal(RoomState.DIRTY, f.start.state());
            equal(40, f.world.player().respect());
            f.command("clean room");
            equal(RoomState.HALF_DIRTY, f.start.state());
            f.command("clean room");
            equal(RoomState.CLEAN, f.start.state());
            f.command("clean room");
            equal(42, f.world.player().respect());
            f.command("dirty room");
            equal(RoomState.HALF_DIRTY, f.start.state());
            equal(41, f.world.player().respect());
        }));
        result.add(new TestCase("cleaning reactions and NPC departure", () -> {
            Fixture f = new Fixture(RoomState.HALF_DIRTY);
            Room next = f.exit(Direction.NORTH, RoomState.DIRTY);
            Animal dog = new Animal("Dog", "a dog");
            Npc mary = new Npc("Mary", "a person");
            f.add(dog); f.add(mary);
            f.command("clean room");
            equal(40, f.world.player().respect());
            equal(f.start, dog.room()); equal(next, mary.room());
            check(f.log.indexOf("Mary the NPC grumbles at you.") < f.indexContaining("Mary leaves"),
                    "NPC must react before leaving");
        }));
        result.add(new TestCase("dirtying reactions and animal departure", () -> {
            Fixture f = new Fixture(RoomState.HALF_DIRTY);
            Room next = f.exit(Direction.WEST, RoomState.CLEAN);
            Animal dog = new Animal("Dog", "a dog");
            f.add(dog); f.add(new Npc("Mary", "a person"));
            f.command("dirty room");
            equal(40, f.world.player().respect()); equal(next, dog.room());
        }));
        result.add(new TestCase("ordered animal cleaning gives three positive reactions", () -> {
            Fixture f = new Fixture(RoomState.HALF_DIRTY);
            f.exit(Direction.NORTH, RoomState.DIRTY);
            f.add(new Animal("Dog", "a dog")); f.add(new Npc("Mary", "a person"));
            f.command("Dog clean room");
            equal(42, f.world.player().respect());
            equal(3L, f.log.stream().filter(s -> s.equals("Dog the animal licks your face.")).count());
        }));
        result.add(new TestCase("ordered NPC cleaning gives three negative reactions before departure", () -> {
            Fixture f = new Fixture(RoomState.HALF_DIRTY);
            Room next = f.exit(Direction.NORTH, RoomState.DIRTY);
            Npc mary = new Npc("Mary", "a person");
            f.add(mary); f.add(new Animal("Dog", "a dog"));
            f.command("Mary clean room");
            equal(38, f.world.player().respect()); equal(next, mary.room());
            equal(3L, f.log.stream().filter(s -> s.equals("Mary the NPC grumbles at you.")).count());
        }));
        result.add(new TestCase("ordered NPC dirtying gives three positive reactions", () -> {
            Fixture f = new Fixture(RoomState.HALF_DIRTY);
            f.exit(Direction.NORTH, RoomState.CLEAN);
            f.add(new Npc("Mary", "a person")); f.add(new Animal("Dog", "a dog"));
            f.command("Mary dirty room");
            equal(42, f.world.player().respect());
        }));
        result.add(new TestCase("ordered animal dirtying gives three negative reactions", () -> {
            Fixture f = new Fixture(RoomState.HALF_DIRTY);
            f.exit(Direction.NORTH, RoomState.CLEAN);
            f.add(new Animal("Dog", "a dog")); f.add(new Npc("Mary", "a person"));
            f.command("Dog dirty room");
            equal(38, f.world.player().respect());
        }));
        result.add(new TestCase("animal cleans unacceptable destination once without remote respect changes", () -> {
            Fixture f = new Fixture(RoomState.CLEAN);
            Room next = f.exit(Direction.EAST, RoomState.DIRTY);
            Animal dog = new Animal("Dog", "a dog");
            f.add(dog); f.world.addCreature(new Npc("Remote", "a person"), next);
            f.command("Dog leave east");
            equal(next, dog.room()); equal(RoomState.HALF_DIRTY, next.state());
            equal(40, f.world.player().respect());
        }));
        result.add(new TestCase("NPC dirties unacceptable destination once", () -> {
            Fixture f = new Fixture(RoomState.DIRTY);
            Room next = f.exit(Direction.SOUTH, RoomState.CLEAN);
            Npc mary = new Npc("Mary", "a person"); f.add(mary);
            f.command("Mary leave south");
            equal(next, mary.room()); equal(RoomState.HALF_DIRTY, next.state());
            equal(40, f.world.player().respect());
        }));
        result.add(new TestCase("forced movement to a full room causes one animal complaint", () -> {
            Fixture f = new Fixture(RoomState.CLEAN);
            Room next = f.exit(Direction.NORTH, RoomState.CLEAN); fill(f.world, next, 10);
            Animal dog = new Animal("Dog", "a dog"); f.add(dog);
            f.command("Dog leave north");
            equal(f.start, dog.room()); equal(39, f.world.player().respect());
            equal(10, next.occupants().size());
            check(f.contains("Room full."), "Must report full room");
        }));
        result.add(new TestCase("forced NPC movement to a full room causes one grumble", () -> {
            Fixture f = new Fixture(RoomState.DIRTY);
            Room next = f.exit(Direction.NORTH, RoomState.DIRTY); fill(f.world, next, 10);
            f.add(new Npc("Mary", "a person"));
            f.command("Mary leave north");
            equal(39, f.world.player().respect());
            check(f.contains("Mary the NPC grumbles"), "Must grumble");
        }));
        result.add(new TestCase("PC cannot enter full rooms and has no discontent reaction", () -> {
            Fixture f = new Fixture(RoomState.CLEAN);
            Room next = f.exit(Direction.NORTH, RoomState.CLEAN); fill(f.world, next, 10);
            f.command("north");
            equal(f.start, f.world.player().room()); equal(40, f.world.player().respect());
        }));
        result.add(new TestCase("fleeing creature tries another door after a full room", () -> {
            Fixture f = new Fixture(RoomState.HALF_DIRTY);
            Room full = f.exit(Direction.NORTH, RoomState.CLEAN); fill(f.world, full, 10);
            Room open = f.exit(Direction.SOUTH, RoomState.DIRTY);
            Animal dog = new Animal("Dog", "a dog"); f.add(dog);
            f.command("dirty room");
            equal(open, dog.room()); equal(RoomState.HALF_DIRTY, open.state());
            equal(39, f.world.player().respect());
            check(f.contains("tries another door"), "Must attempt the alternative exit");
        }));
        result.add(new TestCase("roof penalty excludes the animal that took the last neighboring spot", () -> {
            Fixture f = new Fixture(RoomState.HALF_DIRTY);
            Room next = f.exit(Direction.NORTH, RoomState.DIRTY); fill(f.world, next, 9);
            Animal first = new Animal("First", "a dog");
            Animal second = new Animal("Second", "a cat");
            f.add(first); f.add(second); f.add(new Npc("Mary", "a person"));
            f.command("dirty room");
            equal(next, first.room()); equal(null, second.room());
            equal(null, f.world.creature("Second"));
            equal(38, f.world.player().respect());
            equal(1L, f.log.stream().filter(s -> s.equals("First the animal growls at you.")).count());
            equal(10, next.occupants().size());
        }));
        result.add(new TestCase("roof removal with all exits full penalizes each remaining creature once", () -> {
            Fixture f = new Fixture(RoomState.HALF_DIRTY);
            Room next = f.exit(Direction.NORTH, RoomState.DIRTY); fill(f.world, next, 10);
            f.add(new Npc("Mary", "a person")); f.add(new Animal("Dog", "a dog"));
            f.command("Mary clean room");
            equal(null, f.world.creature("Mary"));
            equal(37, f.world.player().respect()); // -3 grumbles +1 lick -1 roof growl
            check(f.contains("all adjacent rooms are full"), "Must explain roof departure");
        }));
        result.add(new TestCase("no exits also causes roof departure", () -> {
            Fixture f = new Fixture(RoomState.HALF_DIRTY);
            f.add(new Animal("Dog", "a dog")); f.command("dirty room");
            equal(1, f.world.creatures().size()); equal(39, f.world.player().respect());
            check(f.contains("because there are no exits"), "Must explain absent exits");
        }));
        result.add(new TestCase("multiple roof departures each have their own remaining-occupant penalty", () -> {
            Fixture f = new Fixture(RoomState.HALF_DIRTY);
            f.add(new Animal("First", "a dog")); f.add(new Animal("Second", "a cat"));
            f.add(new Npc("Mary", "a person")); f.command("dirty room");
            equal(36, f.world.player().respect()); equal(2, f.world.creatures().size());
        }));
        result.add(new TestCase("room capacity includes PC", () -> {
            Fixture f = new Fixture(RoomState.CLEAN); fill(f.world, f.start, 9);
            equal(10, f.start.occupants().size());
            expect(IllegalArgumentException.class, () -> f.add(new Animal("Extra", "a dog")));
            equal(10, f.world.creatures().size());
        }));
        result.add(new TestCase("PC can stay in every state and moves without cleaning", () -> {
            Fixture f = new Fixture(RoomState.CLEAN);
            Room next = f.exit(Direction.EAST, RoomState.DIRTY);
            f.command("east"); equal(next, f.world.player().room());
            equal(RoomState.DIRTY, next.state()); equal(40, f.world.player().respect());
        }));
        result.add(new TestCase("absent doors and nonlocal creatures do not change state or respect", () -> {
            Fixture f = new Fixture(RoomState.HALF_DIRTY);
            Room remote = f.exit(Direction.EAST, RoomState.CLEAN);
            f.world.addCreature(new Animal("Remote", "a dog"), remote);
            f.command("go north"); f.command("Remote clean room"); f.command("Missing look");
            equal(RoomState.HALF_DIRTY, f.start.state()); equal(40, f.world.player().respect());
            check(f.contains("no door"), "Must report absent door");
            check(f.contains("no creature"), "Must report nonlocal creature");
        }));
        result.add(new TestCase("look reports room, exits, types and descriptions, with creature viewpoints", () -> {
            Fixture f = new Fixture(RoomState.HALF_DIRTY);
            f.add(new Animal("Old Dog", "a friendly dog"));
            f.command("look");
            check(f.contains("Start | half-dirty | starting room"), "Room details missing");
            check(f.contains("Old Dog | animal | a friendly dog"), "Creature details missing");
            check(!f.contains("Hero | PC"), "PC should not list itself");
            f.log.clear(); f.command("Old Dog look");
            check(f.contains("Hero | PC | the player"), "Creature look should list PC");
            check(!f.contains("Old Dog | animal"), "Viewer should not list itself");
        }));
        result.add(new TestCase("case-insensitive multiword names and quoted names", () -> {
            Fixture f = new Fixture(RoomState.DIRTY);
            f.add(new Animal("Old Dog", "a dog")); f.command("\"OLD DOG\" CLEAN ROOM");
            equal(43, f.world.player().respect());
            Command move = new CommandParser().parse("Old Dog leave N");
            equal("Old Dog", move.actorName()); equal(Direction.NORTH, move.direction());
            expect(IllegalArgumentException.class, () -> new CommandParser().parse("dance"));
        }));
        result.add(new TestCase("win strictly above 80 and ended game ignores further commands", () -> {
            Fixture f = new Fixture(RoomState.HALF_DIRTY); f.add(new Animal("Dog", "a dog"));
            f.world.player().changeRespect(40); // Set up the exact boundary.
            f.command("respect"); equal(GameStatus.PLAYING, f.game.status());
            f.command("clean room"); equal(81, f.world.player().respect());
            equal(GameStatus.PRAISED, f.game.status());
            f.command("dirty room"); equal(RoomState.CLEAN, f.start.state());
        }));
        result.add(new TestCase("loss at exactly zero", () -> {
            Fixture f = new Fixture(RoomState.CLEAN); f.add(new Animal("Dog", "a dog"));
            f.world.player().changeRespect(-39); f.command("dirty room");
            equal(0, f.world.player().respect()); equal(GameStatus.DISGRACED, f.game.status());
        }));
        result.add(new TestCase("loss below zero", () -> {
            Fixture f = new Fixture(RoomState.CLEAN); f.add(new Animal("Dog", "a dog"));
            f.world.player().changeRespect(-39); f.command("Dog dirty room");
            equal(-2, f.world.player().respect()); equal(GameStatus.DISGRACED, f.game.status());
        }));
        result.add(new TestCase("help and mixed-case Exit", () -> {
            Fixture f = new Fixture(RoomState.CLEAN); f.command("help");
            check(f.contains("<name> leave"), "Help must explain ordered movement");
            f.command("ExIt"); equal(GameStatus.EXITED, f.game.status());
        }));
        result.add(new TestCase("loader accepts standard XML declaration and world wrapper", () -> {
            World world = loadText("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<world><room name=\"A\" description=\"a &amp; b\" state=\"clean\">"
                    + "<PC name=\"P\" description=\"player\"/></room></world>");
            equal("a & b", world.room("A").description());
        }));
        result.add(new TestCase("loader rejects malformed XML", () -> invalid("<xml><room></xml>")));
        result.add(new TestCase("loader rejects missing and multiple PCs", () -> {
            invalid(xml("<animal name=\"Dog\" description=\"dog\"/>"));
            invalid(xml(pc("P") + pc("Q")));
        }));
        result.add(new TestCase("loader rejects duplicate names including case and room names", () -> {
            invalid(xml(pc("P") + "<animal name=\"p\" description=\"dog\"/>"));
            invalid(xml(pc("Start")));
            invalid("<xml><room name=\"A\" description=\"a\" state=\"clean\">" + pc("P")
                    + "</room><room name=\"a\" description=\"a\" state=\"clean\"/></xml>");
        }));
        result.add(new TestCase("loader rejects invalid state, absent attributes, and nonexistent neighbors", () -> {
            invalid(xml(pc("P")).replace("half-dirty", "muddy"));
            invalid(xml(pc("P")).replace("name=\"Start\"", ""));
            invalid(xml(pc("P")).replace("state=\"half-dirty\"", "state=\"clean\" north=\"Missing\""));
        }));
        result.add(new TestCase("loader rejects oversized rooms and unknown creature types", () -> {
            StringBuilder occupants = new StringBuilder(pc("P"));
            for (int i = 0; i < 10; i++) occupants.append("<animal name=\"A").append(i).append("\" description=\"dog\"/>");
            invalid(xml(occupants.toString()));
            invalid(xml(pc("P") + "<monster name=\"M\" description=\"monster\"/>"));
        }));
        result.add(new TestCase("loader rejects document type and external entity declarations", () -> {
            invalid("<!DOCTYPE xml [<!ENTITY external SYSTEM \"file:///does-not-exist\">]>"
                    + xml(pc("P")));
        }));
        result.add(new TestCase("loader rejects empty worlds and unexpected text", () -> {
            invalid("<xml/>"); invalid("<other/>"); invalid(xml(pc("P") + "unexpected text"));
        }));
        return List.copyOf(result);
    }

    public static void main(String[] args) throws Exception {
        passed = 0;
        for (TestCase item : cases()) test(item.name(), item.action());
        System.out.println("All " + passed + " tests passed.");
    }

    public record TestCase(String name, CheckedAction action) { }

    private static final class Fixture {
        final World world = new World();
        final Room start;
        final List<String> log = new ArrayList<>();
        final GameEngine game;
        Fixture(RoomState state) {
            start = new Room("Start", "starting room", state);
            world.addRoom(start); world.addCreature(new Player("Hero", "the player"), start);
            // Keep door order stable so the full-then-open fallback test exercises both attempts.
            Random random = new Random(0) {
                @Override public int nextInt(int bound) { return bound - 1; }
            };
            game = new GameEngine(world, random, log::add);
        }
        void add(Creature creature) { world.addCreature(creature, start); }
        Room exit(Direction direction, RoomState state) {
            Room room = new Room("Room " + direction, "another room", state);
            world.addRoom(room); start.connect(direction, room); return room;
        }
        void command(String text) { game.execute(new CommandParser().parse(text)); }
        boolean contains(String text) { return log.stream().anyMatch(s -> s.contains(text)); }
        int indexContaining(String text) {
            for (int i = 0; i < log.size(); i++) if (log.get(i).contains(text)) return i;
            return -1;
        }
    }

    private static void fill(World world, Room room, int count) {
        for (int i = 0; i < count; i++) world.addCreature(new Npc(room.name() + " filler " + i, "a resident"), room);
    }
    private static String pc(String name) { return "<PC name=\"" + name + "\" description=\"player\"/>"; }
    private static String xml(String occupants) {
        return "<xml><room name=\"Start\" description=\"a room\" state=\"half-dirty\">" + occupants + "</room></xml>";
    }
    private static World loadText(String text) throws Exception {
        Path path = Files.createTempFile("project1-world-", ".xml");
        try {
            Files.writeString(path, text, StandardCharsets.UTF_8);
            return new WorldLoader().load(path);
        } finally { Files.deleteIfExists(path); }
    }
    private static void invalid(String text) throws Exception { expect(WorldFormatException.class, () -> loadText(text)); }
    private static void equal(Object expected, Object actual) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    private static void expect(Class<? extends Throwable> type, CheckedAction action) throws Exception {
        try { action.run(); }
        catch (Exception ex) { if (type.isInstance(ex)) return; throw ex; }
        throw new AssertionError("Expected " + type.getSimpleName());
    }
    private static void test(String name, CheckedAction action) throws Exception {
        try { action.run(); passed++; System.out.println("PASS: " + name); }
        catch (Exception | AssertionError ex) { System.err.println("FAIL: " + name); throw ex; }
    }
    @FunctionalInterface public interface CheckedAction { void run() throws Exception; }
}
