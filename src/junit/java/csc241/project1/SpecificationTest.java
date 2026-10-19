package csc241.project1;

import csc241.project1.cli.*;
import csc241.project1.game.*;
import csc241.project1.io.*;
import csc241.project1.model.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import static org.junit.jupiter.api.Assertions.*;

/** Case IDs and equivalence classes match the Quality Management Plan. */
public final class SpecificationTest {
    @TestFactory Stream<DynamicTest> gameRules() throws Exception {
        List<DynamicTest> result=new ArrayList<>();
        int number=1;
        for(GameTests.TestCase item:GameTests.cases()) {
            String id=String.format("TC-G%02d",number++);
            result.add(DynamicTest.dynamicTest(id+" | "+item.name(), () -> item.action().run()));
        }
        return result.stream();
    }

    @TestFactory Stream<DynamicTest> gameEngineControlFlow() {
        List<DynamicTest> result=new ArrayList<>();
        result.add(DynamicTest.dynamicTest("TC-CF01 | GameEngine.look | no exits and no other occupants", () -> {
            World world=new World(); Room solo=room("Solo"); Player player=new Player("Hero","player");
            List<String> log=new ArrayList<>(); world.addRoom(solo); world.addCreature(player,solo);
            GameEngine game=new GameEngine(world,new Random(0),log::add); game.look(player);
            assertTrue(log.contains("Exits: none"));
            assertTrue(log.contains("Occupancy: 1/10."));
            assertTrue(log.contains("No other creatures are here."));
        }));
        result.add(DynamicTest.dynamicTest("TC-CF02 | GameEngine.look | exits and another occupant", () -> {
            F f=new F();
            f.a.connect(Direction.NORTH,f.b);
            f.w.addCreature(new Animal("Dog","a dog"),f.a);
            f.game.look(f.p);
            assertTrue(f.log.contains("Exits: north -> B"));
            assertTrue(f.log.contains("Dog | animal | a dog"));
            assertFalse(f.log.stream().anyMatch(s -> s.startsWith("Hero | PC")));
        }));
        return result.stream();
    }

    @TestFactory Stream<DynamicTest> parameterClasses() {
        List<DynamicTest> result=new ArrayList<>();
        result.add(DynamicTest.dynamicTest("TC-A001 | EC-D | 'n'", () -> { assertEquals(Direction.NORTH, Direction.parse("n")); }));
        result.add(DynamicTest.dynamicTest("TC-A002 | EC-D | 'north'", () -> { assertEquals(Direction.NORTH, Direction.parse("north")); }));
        result.add(DynamicTest.dynamicTest("TC-A003 | EC-D | 's'", () -> { assertEquals(Direction.SOUTH, Direction.parse("s")); }));
        result.add(DynamicTest.dynamicTest("TC-A004 | EC-D | 'south'", () -> { assertEquals(Direction.SOUTH, Direction.parse("south")); }));
        result.add(DynamicTest.dynamicTest("TC-A005 | EC-D | 'e'", () -> { assertEquals(Direction.EAST, Direction.parse("e")); }));
        result.add(DynamicTest.dynamicTest("TC-A006 | EC-D | 'east'", () -> { assertEquals(Direction.EAST, Direction.parse("east")); }));
        result.add(DynamicTest.dynamicTest("TC-A007 | EC-D | 'w'", () -> { assertEquals(Direction.WEST, Direction.parse("w")); }));
        result.add(DynamicTest.dynamicTest("TC-A008 | EC-D | 'west'", () -> { assertEquals(Direction.WEST, Direction.parse("west")); }));
        result.add(DynamicTest.dynamicTest("TC-A009 | EC-D | \" NORTH \"", () -> { assertEquals(Direction.NORTH, Direction.parse(" NORTH ")); }));
        result.add(DynamicTest.dynamicTest("TC-A010 | EC-D | ''", () -> { assertThrows(IllegalArgumentException.class, () -> Direction.parse("")); }));
        result.add(DynamicTest.dynamicTest("TC-A011 | EC-D | ' '", () -> { assertThrows(IllegalArgumentException.class, () -> Direction.parse(" ")); }));
        result.add(DynamicTest.dynamicTest("TC-A012 | EC-D | 'up'", () -> { assertThrows(IllegalArgumentException.class, () -> Direction.parse("up")); }));
        result.add(DynamicTest.dynamicTest("TC-A013 | EC-D | null", () -> { assertThrows(NullPointerException.class, () -> Direction.parse(null)); }));
        result.add(DynamicTest.dynamicTest("TC-A014 | EC-D | all four enum values", () -> { assertArrayEquals(new String[]{"north","south","east","west"}, Arrays.stream(Direction.values()).map(Direction::toString).toArray(String[]::new)); }));
        result.add(DynamicTest.dynamicTest("TC-A015 | EC-S | clean", () -> { assertEquals(RoomState.CLEAN, RoomState.parse("clean")); assertEquals("clean", RoomState.CLEAN.toString()); }));
        result.add(DynamicTest.dynamicTest("TC-A016 | EC-S | half-dirty", () -> { assertEquals(RoomState.HALF_DIRTY, RoomState.parse("half-dirty")); assertEquals("half-dirty", RoomState.HALF_DIRTY.toString()); }));
        result.add(DynamicTest.dynamicTest("TC-A017 | EC-S | dirty", () -> { assertEquals(RoomState.DIRTY, RoomState.parse("dirty")); assertEquals("dirty", RoomState.DIRTY.toString()); }));
        result.add(DynamicTest.dynamicTest("TC-A018 | EC-S | \" HALF-DIRTY \"", () -> { assertEquals(RoomState.HALF_DIRTY, RoomState.parse(" HALF-DIRTY ")); }));
        result.add(DynamicTest.dynamicTest("TC-A019 | EC-S | ''", () -> { assertThrows(IllegalArgumentException.class, () -> RoomState.parse("")); }));
        result.add(DynamicTest.dynamicTest("TC-A020 | EC-S | 'muddy'", () -> { assertThrows(IllegalArgumentException.class, () -> RoomState.parse("muddy")); }));
        result.add(DynamicTest.dynamicTest("TC-A021 | EC-S | parse(null)", () -> { assertThrows(NullPointerException.class, () -> RoomState.parse(null)); }));
        result.add(DynamicTest.dynamicTest("TC-A022 | EC-S | CLEAN.after(CLEAN)", () -> { assertEquals(RoomState.CLEAN, RoomState.CLEAN.after(RoomAction.CLEAN)); }));
        result.add(DynamicTest.dynamicTest("TC-A023 | EC-S | CLEAN.after(DIRTY)", () -> { assertEquals(RoomState.HALF_DIRTY, RoomState.CLEAN.after(RoomAction.DIRTY)); }));
        result.add(DynamicTest.dynamicTest("TC-A024 | EC-S | HALF_DIRTY.after(CLEAN)", () -> { assertEquals(RoomState.CLEAN, RoomState.HALF_DIRTY.after(RoomAction.CLEAN)); }));
        result.add(DynamicTest.dynamicTest("TC-A025 | EC-S | HALF_DIRTY.after(DIRTY)", () -> { assertEquals(RoomState.DIRTY, RoomState.HALF_DIRTY.after(RoomAction.DIRTY)); }));
        result.add(DynamicTest.dynamicTest("TC-A026 | EC-S | DIRTY.after(CLEAN)", () -> { assertEquals(RoomState.HALF_DIRTY, RoomState.DIRTY.after(RoomAction.CLEAN)); }));
        result.add(DynamicTest.dynamicTest("TC-A027 | EC-S | DIRTY.after(DIRTY)", () -> { assertEquals(RoomState.DIRTY, RoomState.DIRTY.after(RoomAction.DIRTY)); }));
        result.add(DynamicTest.dynamicTest("TC-A028 | EC-S | CLEAN.after(null)", () -> { assertEquals(RoomState.HALF_DIRTY, RoomState.CLEAN.after(null)); }));
        result.add(DynamicTest.dynamicTest("TC-A029 | EC-N | Room name=\" A \"", () -> { assertEquals("A", (new Room(" A ", "description", RoomState.CLEAN)).name()); }));
        result.add(DynamicTest.dynamicTest("TC-A030 | EC-N | Room name=None", () -> { assertThrows(IllegalArgumentException.class, () -> new Room(null, "description", RoomState.CLEAN)); }));
        result.add(DynamicTest.dynamicTest("TC-A031 | EC-N | Room name=''", () -> { assertThrows(IllegalArgumentException.class, () -> new Room("", "description", RoomState.CLEAN)); }));
        result.add(DynamicTest.dynamicTest("TC-A032 | EC-N | Room name='  '", () -> { assertThrows(IllegalArgumentException.class, () -> new Room("  ", "description", RoomState.CLEAN)); }));
        result.add(DynamicTest.dynamicTest("TC-A033 | EC-N | Player name=\" A \"", () -> { assertEquals("A", (new Player(" A ", "description")).name()); }));
        result.add(DynamicTest.dynamicTest("TC-A034 | EC-N | Player name=None", () -> { assertThrows(IllegalArgumentException.class, () -> new Player(null, "description")); }));
        result.add(DynamicTest.dynamicTest("TC-A035 | EC-N | Player name=''", () -> { assertThrows(IllegalArgumentException.class, () -> new Player("", "description")); }));
        result.add(DynamicTest.dynamicTest("TC-A036 | EC-N | Player name='  '", () -> { assertThrows(IllegalArgumentException.class, () -> new Player("  ", "description")); }));
        result.add(DynamicTest.dynamicTest("TC-A037 | EC-N | Animal name=\" A \"", () -> { assertEquals("A", (new Animal(" A ", "description")).name()); }));
        result.add(DynamicTest.dynamicTest("TC-A038 | EC-N | Animal name=None", () -> { assertThrows(IllegalArgumentException.class, () -> new Animal(null, "description")); }));
        result.add(DynamicTest.dynamicTest("TC-A039 | EC-N | Animal name=''", () -> { assertThrows(IllegalArgumentException.class, () -> new Animal("", "description")); }));
        result.add(DynamicTest.dynamicTest("TC-A040 | EC-N | Animal name='  '", () -> { assertThrows(IllegalArgumentException.class, () -> new Animal("  ", "description")); }));
        result.add(DynamicTest.dynamicTest("TC-A041 | EC-N | Npc name=\" A \"", () -> { assertEquals("A", (new Npc(" A ", "description")).name()); }));
        result.add(DynamicTest.dynamicTest("TC-A042 | EC-N | Npc name=None", () -> { assertThrows(IllegalArgumentException.class, () -> new Npc(null, "description")); }));
        result.add(DynamicTest.dynamicTest("TC-A043 | EC-N | Npc name=''", () -> { assertThrows(IllegalArgumentException.class, () -> new Npc("", "description")); }));
        result.add(DynamicTest.dynamicTest("TC-A044 | EC-N | Npc name='  '", () -> { assertThrows(IllegalArgumentException.class, () -> new Npc("  ", "description")); }));
        result.add(DynamicTest.dynamicTest("TC-A045 | EC-N | Room description=\"\"", () -> { assertEquals("", (new Room("A", "", RoomState.CLEAN)).description()); }));
        result.add(DynamicTest.dynamicTest("TC-A046 | EC-N | Room description=null", () -> { assertThrows(NullPointerException.class, () -> new Room("A", null, RoomState.CLEAN)); }));
        result.add(DynamicTest.dynamicTest("TC-A047 | EC-N | Player description=\"\"", () -> { assertEquals("", (new Player("A", "")).description()); }));
        result.add(DynamicTest.dynamicTest("TC-A048 | EC-N | Player description=null", () -> { assertThrows(NullPointerException.class, () -> new Player("A", null)); }));
        result.add(DynamicTest.dynamicTest("TC-A049 | EC-N | Animal description=\"\"", () -> { assertEquals("", (new Animal("A", "")).description()); }));
        result.add(DynamicTest.dynamicTest("TC-A050 | EC-N | Animal description=null", () -> { assertThrows(NullPointerException.class, () -> new Animal("A", null)); }));
        result.add(DynamicTest.dynamicTest("TC-A051 | EC-N | Npc description=\"\"", () -> { assertEquals("", (new Npc("A", "")).description()); }));
        result.add(DynamicTest.dynamicTest("TC-A052 | EC-N | Npc description=null", () -> { assertThrows(NullPointerException.class, () -> new Npc("A", null)); }));
        result.add(DynamicTest.dynamicTest("TC-A053 | EC-N | Room state=null", () -> { assertThrows(NullPointerException.class, () -> new Room("A", "description", null)); }));
        result.add(DynamicTest.dynamicTest("TC-A054 | EC-C | Player.accepts(CLEAN)", () -> { assertEquals(true, new Player("A", "desc").accepts(RoomState.CLEAN)); }));
        result.add(DynamicTest.dynamicTest("TC-A055 | EC-C | Player.accepts(HALF_DIRTY)", () -> { assertEquals(true, new Player("A", "desc").accepts(RoomState.HALF_DIRTY)); }));
        result.add(DynamicTest.dynamicTest("TC-A056 | EC-C | Player.accepts(DIRTY)", () -> { assertEquals(true, new Player("A", "desc").accepts(RoomState.DIRTY)); }));
        result.add(DynamicTest.dynamicTest("TC-A057 | EC-C | Player.reactionTo(CLEAN)", () -> { assertEquals(null, new Player("A", "desc").reactionTo(RoomAction.CLEAN)); }));
        result.add(DynamicTest.dynamicTest("TC-A058 | EC-C | Player.reactionTo(DIRTY)", () -> { assertEquals(null, new Player("A", "desc").reactionTo(RoomAction.DIRTY)); }));
        result.add(DynamicTest.dynamicTest("TC-A059 | EC-C | Player constant behavior", () -> { Creature c = new Player("A", "desc"); assertEquals("PC",c.type()); assertEquals(true,c.isPlayer()); assertEquals(null,c.preferredAction()); assertEquals(null,c.discontentReaction()); }));
        result.add(DynamicTest.dynamicTest("TC-A060 | EC-C | Animal.accepts(CLEAN)", () -> { assertEquals(true, new Animal("A", "desc").accepts(RoomState.CLEAN)); }));
        result.add(DynamicTest.dynamicTest("TC-A061 | EC-C | Animal.accepts(HALF_DIRTY)", () -> { assertEquals(true, new Animal("A", "desc").accepts(RoomState.HALF_DIRTY)); }));
        result.add(DynamicTest.dynamicTest("TC-A062 | EC-C | Animal.accepts(DIRTY)", () -> { assertEquals(false, new Animal("A", "desc").accepts(RoomState.DIRTY)); }));
        result.add(DynamicTest.dynamicTest("TC-A063 | EC-C | Animal.reactionTo(CLEAN)", () -> { assertEquals(Reaction.LICK_FACE, new Animal("A", "desc").reactionTo(RoomAction.CLEAN)); }));
        result.add(DynamicTest.dynamicTest("TC-A064 | EC-C | Animal.reactionTo(DIRTY)", () -> { assertEquals(Reaction.GROWL, new Animal("A", "desc").reactionTo(RoomAction.DIRTY)); }));
        result.add(DynamicTest.dynamicTest("TC-A065 | EC-C | Animal constant behavior", () -> { Creature c = new Animal("A", "desc"); assertEquals("animal",c.type()); assertEquals(false,c.isPlayer()); assertEquals(RoomAction.CLEAN,c.preferredAction()); assertEquals(Reaction.GROWL,c.discontentReaction()); }));
        result.add(DynamicTest.dynamicTest("TC-A066 | EC-C | Npc.accepts(CLEAN)", () -> { assertEquals(false, new Npc("A", "desc").accepts(RoomState.CLEAN)); }));
        result.add(DynamicTest.dynamicTest("TC-A067 | EC-C | Npc.accepts(HALF_DIRTY)", () -> { assertEquals(true, new Npc("A", "desc").accepts(RoomState.HALF_DIRTY)); }));
        result.add(DynamicTest.dynamicTest("TC-A068 | EC-C | Npc.accepts(DIRTY)", () -> { assertEquals(true, new Npc("A", "desc").accepts(RoomState.DIRTY)); }));
        result.add(DynamicTest.dynamicTest("TC-A069 | EC-C | Npc.reactionTo(CLEAN)", () -> { assertEquals(Reaction.GRUMBLE, new Npc("A", "desc").reactionTo(RoomAction.CLEAN)); }));
        result.add(DynamicTest.dynamicTest("TC-A070 | EC-C | Npc.reactionTo(DIRTY)", () -> { assertEquals(Reaction.SMILE, new Npc("A", "desc").reactionTo(RoomAction.DIRTY)); }));
        result.add(DynamicTest.dynamicTest("TC-A071 | EC-C | Npc constant behavior", () -> { Creature c = new Npc("A", "desc"); assertEquals("NPC",c.type()); assertEquals(false,c.isPlayer()); assertEquals(RoomAction.DIRTY,c.preferredAction()); assertEquals(Reaction.GRUMBLE,c.discontentReaction()); }));
        result.add(DynamicTest.dynamicTest("TC-A072 | EC-C | Animal.accepts(null), action/state invalid references", () -> { assertTrue(new Animal("A", "desc").accepts(null)); assertEquals(Reaction.GROWL,new Animal("A","desc").reactionTo(null)); }));
        result.add(DynamicTest.dynamicTest("TC-A073 | EC-C | NPC null state/action references", () -> { assertTrue(new Npc("A", "desc").accepts(null)); assertEquals(Reaction.GRUMBLE,new Npc("A","desc").reactionTo(null)); }));
        result.add(DynamicTest.dynamicTest("TC-A074 | EC-C | PC null state/action references", () -> { assertTrue(new Player("A", "desc").accepts(null)); assertNull(new Player("A","desc").reactionTo(null)); }));
        result.add(DynamicTest.dynamicTest("TC-A075 | EC-C | SMILE", () -> { assertEquals(1,Reaction.SMILE.respectChange()); assertEquals("smiles at you",Reaction.SMILE.message()); }));
        result.add(DynamicTest.dynamicTest("TC-A076 | EC-C | GRUMBLE", () -> { assertEquals(-1,Reaction.GRUMBLE.respectChange()); assertEquals("grumbles at you",Reaction.GRUMBLE.message()); }));
        result.add(DynamicTest.dynamicTest("TC-A077 | EC-C | LICK_FACE", () -> { assertEquals(1,Reaction.LICK_FACE.respectChange()); assertEquals("licks your face",Reaction.LICK_FACE.message()); }));
        result.add(DynamicTest.dynamicTest("TC-A078 | EC-C | GROWL", () -> { assertEquals(-1,Reaction.GROWL.respectChange()); assertEquals("growls at you",Reaction.GROWL.message()); }));
        result.add(DynamicTest.dynamicTest("TC-A079 | EC-R | connect NORTH to destination", () -> { Room a=room("A"), b=room("B"); a.connect(Direction.NORTH,b); assertSame(b,a.neighbor(Direction.NORTH)); assertEquals(1,a.neighbors().size()); }));
        result.add(DynamicTest.dynamicTest("TC-A080 | EC-R | connect SOUTH to destination", () -> { Room a=room("A"), b=room("B"); a.connect(Direction.SOUTH,b); assertSame(b,a.neighbor(Direction.SOUTH)); assertEquals(1,a.neighbors().size()); }));
        result.add(DynamicTest.dynamicTest("TC-A081 | EC-R | connect EAST to destination", () -> { Room a=room("A"), b=room("B"); a.connect(Direction.EAST,b); assertSame(b,a.neighbor(Direction.EAST)); assertEquals(1,a.neighbors().size()); }));
        result.add(DynamicTest.dynamicTest("TC-A082 | EC-R | connect WEST to destination", () -> { Room a=room("A"), b=room("B"); a.connect(Direction.WEST,b); assertSame(b,a.neighbor(Direction.WEST)); assertEquals(1,a.neighbors().size()); }));
        result.add(DynamicTest.dynamicTest("TC-A083 | EC-R | lookup absent door", () -> { assertNull(room("A").neighbor(Direction.NORTH)); }));
        result.add(DynamicTest.dynamicTest("TC-A084 | EC-R | lookup null direction", () -> { assertNull(room("A").neighbor(null)); }));
        result.add(DynamicTest.dynamicTest("TC-A085 | EC-R | connect null direction with valid room", () -> { assertThrows(NullPointerException.class, () -> room("A").connect(null,room("B"))); }));
        result.add(DynamicTest.dynamicTest("TC-A086 | EC-R | connect valid direction with null destination", () -> { assertThrows(NullPointerException.class, () -> room("A").connect(Direction.NORTH,null)); }));
        result.add(DynamicTest.dynamicTest("TC-A087 | EC-R | apply null action", () -> { Room a=room("A"); assertThrows(NullPointerException.class, () -> a.apply(null)); assertEquals(RoomState.HALF_DIRTY,a.state()); }));
        result.add(DynamicTest.dynamicTest("TC-A088 | EC-R | Room getters and readonly collections", () -> { Room a=room("A"); assertEquals("A",a.name()); assertEquals("description",a.description()); assertFalse(a.isFull()); assertThrows(UnsupportedOperationException.class, () -> a.occupants().add(new Animal("D","dog"))); assertThrows(UnsupportedOperationException.class, () -> a.neighbors().put(Direction.NORTH,room("B"))); }));
        result.add(DynamicTest.dynamicTest("TC-A089 | EC-R | occupancy n=0", () -> { World w=new World(); Room a=room("A"); w.addRoom(a); fill(w,a,0); assertEquals(0,a.occupants().size()); assertEquals(false,a.isFull()); }));
        result.add(DynamicTest.dynamicTest("TC-A090 | EC-R | occupancy n=1", () -> { World w=new World(); Room a=room("A"); w.addRoom(a); fill(w,a,1); assertEquals(1,a.occupants().size()); assertEquals(false,a.isFull()); }));
        result.add(DynamicTest.dynamicTest("TC-A091 | EC-R | occupancy n=9", () -> { World w=new World(); Room a=room("A"); w.addRoom(a); fill(w,a,9); assertEquals(9,a.occupants().size()); assertEquals(false,a.isFull()); }));
        result.add(DynamicTest.dynamicTest("TC-A092 | EC-R | occupancy n=10", () -> { World w=new World(); Room a=room("A"); w.addRoom(a); fill(w,a,10); assertEquals(10,a.occupants().size()); assertEquals(true,a.isFull()); }));
        result.add(DynamicTest.dynamicTest("TC-A093 | EC-R | attempt occupant 11 with current n=10", () -> { World w=new World(); Room a=room("A"); w.addRoom(a); fill(w,a,10); assertThrows(IllegalArgumentException.class, () -> w.addCreature(new Animal("Extra","dog"),a)); assertEquals(10,a.occupants().size()); }));
        result.add(DynamicTest.dynamicTest("TC-A094 | EC-W | fresh room and normalized lookup", () -> { World w=new World(); Room a=room("A"); w.addRoom(a); assertSame(a,w.room(" a ")); assertNull(w.room("Missing")); assertThrows(UnsupportedOperationException.class, () -> w.rooms().clear()); }));
        result.add(DynamicTest.dynamicTest("TC-A095 | EC-W | duplicate room name differing in case", () -> { World w=new World(); w.addRoom(room("A")); assertThrows(IllegalArgumentException.class, () -> w.addRoom(room("a"))); }));
        result.add(DynamicTest.dynamicTest("TC-A096 | EC-W | null room to addRoom", () -> { assertThrows(NullPointerException.class, () -> new World().addRoom(null)); }));
        result.add(DynamicTest.dynamicTest("TC-A097 | EC-W | fresh creature with valid room", () -> { F f=new F(); Animal d=new Animal("Dog","brown"); f.w.addCreature(d,f.a); assertSame(f.a,d.room()); assertEquals("brown",d.description()); assertSame(d,f.w.creature(" dog ")); assertNull(f.w.creature("Ghost")); assertSame(f.p,f.w.player()); assertThrows(UnsupportedOperationException.class, () -> f.w.creatures().clear()); }));
        result.add(DynamicTest.dynamicTest("TC-A098 | EC-W | duplicate creature name", () -> { F f=new F(); assertThrows(IllegalArgumentException.class, () -> f.w.addCreature(new Animal("Hero","dog"),f.a)); }));
        result.add(DynamicTest.dynamicTest("TC-A099 | EC-W | unknown destination room for placement", () -> { F f=new F(); assertThrows(IllegalArgumentException.class, () -> f.w.addCreature(new Animal("D","dog"),room("Foreign"))); }));
        result.add(DynamicTest.dynamicTest("TC-A100 | EC-W | already placed creature, unique name in receiving world", () -> { F f=new F(); World other=new World(); Room b=room("B"); other.addRoom(b); Animal d=new Animal("D","dog"); other.addCreature(d,b); assertThrows(IllegalArgumentException.class, () -> f.w.addCreature(d,f.a)); }));
        result.add(DynamicTest.dynamicTest("TC-A101 | EC-W | second PC", () -> { F f=new F(); assertThrows(IllegalArgumentException.class, () -> f.w.addCreature(new Player("Other","player"),f.a)); }));
        result.add(DynamicTest.dynamicTest("TC-A102 | EC-W | null creature, registered room", () -> { F f=new F(); assertThrows(NullPointerException.class, () -> f.w.addCreature(null,f.a)); }));
        result.add(DynamicTest.dynamicTest("TC-A103 | EC-W | valid creature, null room", () -> { F f=new F(); assertThrows(NullPointerException.class, () -> f.w.addCreature(new Animal("D","dog"),null)); }));
        result.add(DynamicTest.dynamicTest("TC-A104 | EC-W | move registered PC into room with 9 occupants", () -> { F f=new F(); fill(f.w,f.b,9); assertTrue(f.w.move(f.p,f.b)); assertEquals(10,f.b.occupants().size()); assertFalse(f.a.occupants().contains(f.p)); assertSame(f.b,f.p.room()); }));
        result.add(DynamicTest.dynamicTest("TC-A105 | EC-W | move into full room", () -> { F f=new F(); fill(f.w,f.b,10); assertFalse(f.w.move(f.p,f.b)); assertSame(f.a,f.p.room()); assertEquals(10,f.b.occupants().size()); }));
        result.add(DynamicTest.dynamicTest("TC-A106 | EC-W | move to same room", () -> { F f=new F(); assertTrue(f.w.move(f.p,f.a)); assertEquals(1,f.a.occupants().size()); }));
        result.add(DynamicTest.dynamicTest("TC-A107 | EC-W | unregistered creature with registered destination", () -> { F f=new F(); assertThrows(IllegalArgumentException.class, () -> f.w.move(new Animal("D","dog"),f.b)); }));
        result.add(DynamicTest.dynamicTest("TC-A108 | EC-W | registered creature with unregistered destination", () -> { F f=new F(); assertThrows(IllegalArgumentException.class, () -> f.w.move(f.p,room("Foreign"))); }));
        result.add(DynamicTest.dynamicTest("TC-A109 | EC-W | registered creature with null destination", () -> { F f=new F(); assertThrows(IllegalArgumentException.class, () -> f.w.move(f.p,null)); }));
        result.add(DynamicTest.dynamicTest("TC-A110 | EC-W | null creature with valid destination", () -> { F f=new F(); assertThrows(NullPointerException.class, () -> f.w.move(null,f.b)); }));
        result.add(DynamicTest.dynamicTest("TC-A111 | EC-W | remove registered animal; remove again", () -> { F f=new F(); Animal d=new Animal("D","dog"); f.w.addCreature(d,f.a); f.w.remove(d); assertNull(d.room()); assertNull(f.w.creature("D")); f.w.remove(d); assertEquals(1,f.a.occupants().size()); }));
        result.add(DynamicTest.dynamicTest("TC-A112 | EC-W | remove PC", () -> { F f=new F(); assertThrows(IllegalArgumentException.class, () -> f.w.remove(f.p)); }));
        result.add(DynamicTest.dynamicTest("TC-A113 | EC-W | remove null", () -> { assertThrows(NullPointerException.class, () -> new World().remove(null)); }));
        result.add(DynamicTest.dynamicTest("TC-A114 | EC-W | lookup null room name", () -> { assertThrows(NullPointerException.class, () -> new World().room(null)); }));
        result.add(DynamicTest.dynamicTest("TC-A115 | EC-W | lookup null creature name", () -> { assertThrows(NullPointerException.class, () -> new World().creature(null)); }));
        result.add(DynamicTest.dynamicTest("TC-A116 | EC-P | changeRespect(-1) from 40", () -> { Player p=new Player("P","player"); p.changeRespect(-1); assertEquals(39,p.respect()); }));
        result.add(DynamicTest.dynamicTest("TC-A117 | EC-P | changeRespect(0) from 40", () -> { Player p=new Player("P","player"); p.changeRespect(0); assertEquals(40,p.respect()); }));
        result.add(DynamicTest.dynamicTest("TC-A118 | EC-P | changeRespect(1) from 40", () -> { Player p=new Player("P","player"); p.changeRespect(1); assertEquals(41,p.respect()); }));
        result.add(DynamicTest.dynamicTest("TC-A119 | EC-P | final respect=-1", () -> { F f=new F(); f.a.apply(RoomAction.CLEAN); f.p.changeRespect(-1-40); f.run("clean room"); assertEquals(GameStatus.DISGRACED,f.game.status()); assertEquals(-1,f.p.respect()); }));
        result.add(DynamicTest.dynamicTest("TC-A120 | EC-P | final respect=0", () -> { F f=new F(); f.a.apply(RoomAction.CLEAN); f.p.changeRespect(0-40); f.run("clean room"); assertEquals(GameStatus.DISGRACED,f.game.status()); assertEquals(0,f.p.respect()); }));
        result.add(DynamicTest.dynamicTest("TC-A121 | EC-P | final respect=1", () -> { F f=new F(); f.a.apply(RoomAction.CLEAN); f.p.changeRespect(1-40); f.run("clean room"); assertEquals(GameStatus.PLAYING,f.game.status()); assertEquals(1,f.p.respect()); }));
        result.add(DynamicTest.dynamicTest("TC-A122 | EC-P | final respect=79", () -> { F f=new F(); f.a.apply(RoomAction.CLEAN); f.p.changeRespect(79-40); f.run("clean room"); assertEquals(GameStatus.PLAYING,f.game.status()); assertEquals(79,f.p.respect()); }));
        result.add(DynamicTest.dynamicTest("TC-A123 | EC-P | final respect=80", () -> { F f=new F(); f.a.apply(RoomAction.CLEAN); f.p.changeRespect(80-40); f.run("clean room"); assertEquals(GameStatus.PLAYING,f.game.status()); assertEquals(80,f.p.respect()); }));
        result.add(DynamicTest.dynamicTest("TC-A124 | EC-P | final respect=81", () -> { F f=new F(); f.a.apply(RoomAction.CLEAN); f.p.changeRespect(81-40); f.run("clean room"); assertEquals(GameStatus.PRAISED,f.game.status()); assertEquals(81,f.p.respect()); }));
        result.add(DynamicTest.dynamicTest("TC-A125 | EC-Q | 'look'", () -> { Command c=new CommandParser().parse("look"); assertEquals(Command.Kind.LOOK,c.kind()); assertEquals(null,c.actorName()); assertEquals(null,c.direction()); }));
        result.add(DynamicTest.dynamicTest("TC-A126 | EC-Q | 'help'", () -> { Command c=new CommandParser().parse("help"); assertEquals(Command.Kind.HELP,c.kind()); assertEquals(null,c.actorName()); assertEquals(null,c.direction()); }));
        result.add(DynamicTest.dynamicTest("TC-A127 | EC-Q | 'respect'", () -> { Command c=new CommandParser().parse("respect"); assertEquals(Command.Kind.RESPECT,c.kind()); assertEquals(null,c.actorName()); assertEquals(null,c.direction()); }));
        result.add(DynamicTest.dynamicTest("TC-A128 | EC-Q | 'Exit'", () -> { Command c=new CommandParser().parse("Exit"); assertEquals(Command.Kind.EXIT,c.kind()); assertEquals(null,c.actorName()); assertEquals(null,c.direction()); }));
        result.add(DynamicTest.dynamicTest("TC-A129 | EC-Q | 'quit'", () -> { Command c=new CommandParser().parse("quit"); assertEquals(Command.Kind.EXIT,c.kind()); assertEquals(null,c.actorName()); assertEquals(null,c.direction()); }));
        result.add(DynamicTest.dynamicTest("TC-A130 | EC-Q | 'clean room'", () -> { Command c=new CommandParser().parse("clean room"); assertEquals(Command.Kind.CLEAN,c.kind()); assertEquals(null,c.actorName()); assertEquals(null,c.direction()); }));
        result.add(DynamicTest.dynamicTest("TC-A131 | EC-Q | 'dirty room'", () -> { Command c=new CommandParser().parse("dirty room"); assertEquals(Command.Kind.DIRTY,c.kind()); assertEquals(null,c.actorName()); assertEquals(null,c.direction()); }));
        result.add(DynamicTest.dynamicTest("TC-A132 | EC-Q | 'north'", () -> { Command c=new CommandParser().parse("north"); assertEquals(Command.Kind.MOVE,c.kind()); assertEquals(null,c.actorName()); assertEquals(Direction.NORTH,c.direction()); }));
        result.add(DynamicTest.dynamicTest("TC-A133 | EC-Q | 'go s'", () -> { Command c=new CommandParser().parse("go s"); assertEquals(Command.Kind.MOVE,c.kind()); assertEquals(null,c.actorName()); assertEquals(Direction.SOUTH,c.direction()); }));
        result.add(DynamicTest.dynamicTest("TC-A134 | EC-Q | 'move east'", () -> { Command c=new CommandParser().parse("move east"); assertEquals(Command.Kind.MOVE,c.kind()); assertEquals(null,c.actorName()); assertEquals(Direction.EAST,c.direction()); }));
        result.add(DynamicTest.dynamicTest("TC-A135 | EC-Q | 'leave west'", () -> { Command c=new CommandParser().parse("leave west"); assertEquals(Command.Kind.MOVE,c.kind()); assertEquals(null,c.actorName()); assertEquals(Direction.WEST,c.direction()); }));
        result.add(DynamicTest.dynamicTest("TC-A136 | EC-Q | 'Mary clean room'", () -> { Command c=new CommandParser().parse("Mary clean room"); assertEquals(Command.Kind.CLEAN,c.kind()); assertEquals("Mary",c.actorName()); assertEquals(null,c.direction()); }));
        result.add(DynamicTest.dynamicTest("TC-A137 | EC-Q | 'Mary dirty room'", () -> { Command c=new CommandParser().parse("Mary dirty room"); assertEquals(Command.Kind.DIRTY,c.kind()); assertEquals("Mary",c.actorName()); assertEquals(null,c.direction()); }));
        result.add(DynamicTest.dynamicTest("TC-A138 | EC-Q | 'Old Dog leave n'", () -> { Command c=new CommandParser().parse("Old Dog leave n"); assertEquals(Command.Kind.MOVE,c.kind()); assertEquals("Old Dog",c.actorName()); assertEquals(Direction.NORTH,c.direction()); }));
        result.add(DynamicTest.dynamicTest("TC-A139 | EC-Q | '\"Old Dog\" look'", () -> { Command c=new CommandParser().parse("\"Old Dog\" look"); assertEquals(Command.Kind.LOOK,c.kind()); assertEquals("Old Dog",c.actorName()); assertEquals(null,c.direction()); }));
        result.add(DynamicTest.dynamicTest("TC-A140 | EC-Q | '  LOOK  '", () -> { Command c=new CommandParser().parse("  LOOK  "); assertEquals(Command.Kind.LOOK,c.kind()); assertEquals(null,c.actorName()); assertEquals(null,c.direction()); }));
        result.add(DynamicTest.dynamicTest("TC-A141 | EC-Q | ''", () -> { assertThrows(IllegalArgumentException.class, () -> new CommandParser().parse("")); }));
        result.add(DynamicTest.dynamicTest("TC-A142 | EC-Q | ' '", () -> { assertThrows(IllegalArgumentException.class, () -> new CommandParser().parse(" ")); }));
        result.add(DynamicTest.dynamicTest("TC-A143 | EC-Q | 'dance'", () -> { assertThrows(IllegalArgumentException.class, () -> new CommandParser().parse("dance")); }));
        result.add(DynamicTest.dynamicTest("TC-A144 | EC-Q | 'clean'", () -> { assertThrows(IllegalArgumentException.class, () -> new CommandParser().parse("clean")); }));
        result.add(DynamicTest.dynamicTest("TC-A145 | EC-Q | 'go'", () -> { assertThrows(IllegalArgumentException.class, () -> new CommandParser().parse("go")); }));
        result.add(DynamicTest.dynamicTest("TC-A146 | EC-Q | 'go up'", () -> { assertThrows(IllegalArgumentException.class, () -> new CommandParser().parse("go up")); }));
        result.add(DynamicTest.dynamicTest("TC-A147 | EC-Q | '\"\" clean room'", () -> { assertThrows(IllegalArgumentException.class, () -> new CommandParser().parse("\"\" clean room")); }));
        result.add(DynamicTest.dynamicTest("TC-A148 | EC-Q | parse(null)", () -> { assertThrows(NullPointerException.class, () -> new CommandParser().parse(null)); }));
        result.add(DynamicTest.dynamicTest("TC-A149 | EC-Q | well-formed record equality and accessors", () -> { Command a=new Command(Command.Kind.MOVE,"Dog",Direction.NORTH); Command b=new Command(Command.Kind.MOVE,"Dog",Direction.NORTH); assertEquals(a,b); assertEquals(a.hashCode(),b.hashCode()); assertTrue(a.toString().contains("MOVE")); }));
        result.add(DynamicTest.dynamicTest("TC-A150 | EC-L | provided input.xml", () -> { World w=new WorldLoader().load(Path.of("input.xml")); assertEquals(10,w.rooms().size()); assertEquals(27,w.creatures().size()); assertNotNull(w.player()); }));
        result.add(DynamicTest.dynamicTest("TC-A151 | EC-L | missing file", () -> { assertThrows(IOException.class, () -> new WorldLoader().load(Path.of("build/nonexistent-world-file.xml"))); }));
        result.add(DynamicTest.dynamicTest("TC-A152 | EC-L | directory instead of XML file", () -> { Exception e=assertThrows(Exception.class, () -> new WorldLoader().load(Path.of("src"))); assertTrue(e instanceof IOException || e instanceof WorldFormatException); }));
        result.add(DynamicTest.dynamicTest("TC-A153 | EC-L | null path", () -> { assertThrows(NullPointerException.class, () -> new WorldLoader().load(null)); }));
        result.add(DynamicTest.dynamicTest("TC-A154 | EC-L | world wrapper", () -> { assertNotNull(load("<world><room name=\"A\" description=\"room\" state=\"half-dirty\"><PC name=\"P\" description=\"player\"/></room></world>").player()); }));
        result.add(DynamicTest.dynamicTest("TC-A155 | EC-L | comments and whitespace", () -> { assertNotNull(load("<xml><room name=\"A\" description=\"room\" state=\"half-dirty\"><!-- comment -->\n <PC name=\"P\" description=\"player\"/></room></xml>").player()); }));
        result.add(DynamicTest.dynamicTest("TC-A156 | EC-L | forward reference", () -> { assertNotNull(load("<xml><room name=\"A\" description=\"a\" state=\"clean\" north=\"B\"><PC name=\"P\" description=\"p\"/></room><room name=\"B\" description=\"b\" state=\"dirty\"/></xml>").player()); }));
        result.add(DynamicTest.dynamicTest("TC-A157 | EC-L | empty XML; all other XML fields valid where applicable", () -> { assertThrows(WorldFormatException.class, () -> load("")); }));
        result.add(DynamicTest.dynamicTest("TC-A158 | EC-L | bad root; all other XML fields valid where applicable", () -> { assertThrows(WorldFormatException.class, () -> load("<other><room name=\"A\" description=\"room\" state=\"half-dirty\"><PC name=\"P\" description=\"player\"/></room></other>")); }));
        result.add(DynamicTest.dynamicTest("TC-A159 | EC-L | malformed XML; all other XML fields valid where applicable", () -> { assertThrows(WorldFormatException.class, () -> load("<xml><room></xml>")); }));
        result.add(DynamicTest.dynamicTest("TC-A160 | EC-L | missing state; all other XML fields valid where applicable", () -> { assertThrows(WorldFormatException.class, () -> load("<xml><room name=\"A\" description=\"room\"><PC name=\"P\" description=\"player\"/></room></xml>")); }));
        result.add(DynamicTest.dynamicTest("TC-A161 | EC-L | blank state; all other XML fields valid where applicable", () -> { assertThrows(WorldFormatException.class, () -> load("<xml><room name=\"A\" description=\"room\" state=\" \"><PC name=\"P\" description=\"player\"/></room></xml>")); }));
        result.add(DynamicTest.dynamicTest("TC-A162 | EC-L | bad state; all other XML fields valid where applicable", () -> { assertThrows(WorldFormatException.class, () -> load("<xml><room name=\"A\" description=\"room\" state=\"muddy\"><PC name=\"P\" description=\"player\"/></room></xml>")); }));
        result.add(DynamicTest.dynamicTest("TC-A163 | EC-L | missing room name; all other XML fields valid where applicable", () -> { assertThrows(WorldFormatException.class, () -> load("<xml><room  description=\"room\" state=\"half-dirty\"><PC name=\"P\" description=\"player\"/></room></xml>")); }));
        result.add(DynamicTest.dynamicTest("TC-A164 | EC-L | blank room description; all other XML fields valid where applicable", () -> { assertThrows(WorldFormatException.class, () -> load("<xml><room name=\"A\" description=\" \" state=\"half-dirty\"><PC name=\"P\" description=\"player\"/></room></xml>")); }));
        result.add(DynamicTest.dynamicTest("TC-A165 | EC-L | missing creature description; all other XML fields valid where applicable", () -> { assertThrows(WorldFormatException.class, () -> load("<xml><room name=\"A\" description=\"room\" state=\"half-dirty\"><PC name=\"P\" /></room></xml>")); }));
        result.add(DynamicTest.dynamicTest("TC-A166 | EC-L | unknown neighbor; all other XML fields valid where applicable", () -> { assertThrows(WorldFormatException.class, () -> load("<xml><room name=\"A\" description=\"room\" state=\"half-dirty\" north=\"Missing\"><PC name=\"P\" description=\"player\"/></room></xml>")); }));
        result.add(DynamicTest.dynamicTest("TC-A167 | EC-L | unknown type; all other XML fields valid where applicable", () -> { assertThrows(WorldFormatException.class, () -> load("<xml><room name=\"A\" description=\"room\" state=\"half-dirty\"><monster name=\"P\" description=\"player\"/></room></xml>")); }));
        result.add(DynamicTest.dynamicTest("TC-A168 | EC-L | no PC; all other XML fields valid where applicable", () -> { assertThrows(WorldFormatException.class, () -> load("<xml><room name=\"A\" description=\"room\" state=\"half-dirty\"></room></xml>")); }));
        result.add(DynamicTest.dynamicTest("TC-A169 | EC-L | two PCs; all other XML fields valid where applicable", () -> { assertThrows(WorldFormatException.class, () -> load("<xml><room name=\"A\" description=\"room\" state=\"half-dirty\"><PC name=\"P\" description=\"player\"/><PC name=\"Q\" description=\"p\"/></room></xml>")); }));
        result.add(DynamicTest.dynamicTest("TC-A170 | EC-L | duplicate name; all other XML fields valid where applicable", () -> { assertThrows(WorldFormatException.class, () -> load("<xml><room name=\"A\" description=\"room\" state=\"half-dirty\"><PC name=\"P\" description=\"player\"/><animal name=\"p\" description=\"dog\"/></room></xml>")); }));
        result.add(DynamicTest.dynamicTest("TC-A171 | EC-L | unexpected text; all other XML fields valid where applicable", () -> { assertThrows(WorldFormatException.class, () -> load("<xml><room name=\"A\" description=\"room\" state=\"half-dirty\"><PC name=\"P\" description=\"player\"/>junk</room></xml>")); }));
        result.add(DynamicTest.dynamicTest("TC-A172 | EC-L | nested creature child; all other XML fields valid where applicable", () -> { assertThrows(WorldFormatException.class, () -> load("<xml><room name=\"A\" description=\"room\" state=\"half-dirty\"><PC name=\"P\" description=\"player\"><animal name=\"D\" description=\"dog\"/></PC></room></xml>")); }));
        result.add(DynamicTest.dynamicTest("TC-A173 | EC-L | DTD declaration; all other XML fields valid where applicable", () -> { assertThrows(WorldFormatException.class, () -> load("<!DOCTYPE xml><xml><room name=\"A\" description=\"room\" state=\"half-dirty\"><PC name=\"P\" description=\"player\"/></room></xml>")); }));
        result.add(DynamicTest.dynamicTest("TC-A174 | EC-L | exception message and optional cause", () -> { Exception cause=new Exception("cause"); assertEquals("message",new WorldFormatException("message").getMessage()); WorldFormatException e=new WorldFormatException("message",cause); assertSame(cause,e.getCause()); assertNull(new WorldFormatException(null).getMessage()); }));
        result.add(DynamicTest.dynamicTest("TC-A175 | EC-E | valid engine dependencies", () -> { F f=new F(); assertEquals(GameStatus.PLAYING,f.game.status()); assertTrue(f.game.isRunning()); }));
        result.add(DynamicTest.dynamicTest("TC-A176 | EC-E | world lacks PC", () -> { assertThrows(IllegalArgumentException.class, () -> new GameEngine(new World(),new Random(0),s -> {})); }));
        result.add(DynamicTest.dynamicTest("TC-A177 | EC-E | world=null; other dependencies valid", () -> { assertThrows(NullPointerException.class, () -> new GameEngine(null,new Random(0),s -> {})); }));
        result.add(DynamicTest.dynamicTest("TC-A178 | EC-E | random=null; other dependencies valid", () -> { F f=new F(); assertThrows(NullPointerException.class, () -> new GameEngine(f.w,null,s -> {})); }));
        result.add(DynamicTest.dynamicTest("TC-A179 | EC-E | output=null; other dependencies valid", () -> { F f=new F(); assertThrows(NullPointerException.class, () -> new GameEngine(f.w,new Random(0),null)); }));
        result.add(DynamicTest.dynamicTest("TC-A180 | EC-E | execute(null) while playing", () -> { F f=new F(); assertThrows(NullPointerException.class, () -> f.game.execute(null)); }));
        result.add(DynamicTest.dynamicTest("TC-A181 | EC-E | look(null)", () -> { F f=new F(); assertThrows(NullPointerException.class, () -> f.game.look(null)); }));
        result.add(DynamicTest.dynamicTest("TC-A182 | EC-E | exit followed by clean command", () -> { F f=new F(); f.run("exit"); f.run("clean room"); assertEquals(GameStatus.EXITED,f.game.status()); assertFalse(f.game.isRunning()); assertEquals(RoomState.HALF_DIRTY,f.a.state()); }));
        result.add(DynamicTest.dynamicTest("TC-A183 | EC-E | help lists each public command family", () -> { F f=new F(); f.run("help"); for(String word:List.of("look","clean room","dirty room","<direction>","<name>","respect","help","exit")) assertTrue(String.join("\n",f.log).contains(word),word); }));
        result.add(DynamicTest.dynamicTest("TC-A184 | EC-M | zero arguments; blank default filename then Exit", () -> { ProcessResult r=console(List.of(),"\nExit\n"); assertEquals(0,r.code()); assertTrue(r.text().contains("World XML file")); assertTrue(r.text().contains("Goodbye!")); }));
        result.add(DynamicTest.dynamicTest("TC-A185 | EC-M | one valid filename and EOF", () -> { ProcessResult r=console(List.of("input.xml"),""); assertEquals(0,r.code()); assertTrue(r.text().contains("Welcome, Squash")); assertTrue(r.text().contains("Goodbye!")); }));
        result.add(DynamicTest.dynamicTest("TC-A186 | EC-M | two arguments", () -> { ProcessResult r=console(List.of("input.xml","extra"),""); assertEquals(1,r.code()); assertTrue(r.text().contains("Usage:")); }));
        result.add(DynamicTest.dynamicTest("TC-A187 | EC-M | one invalid filename", () -> { ProcessResult r=console(List.of("build/missing.xml"),""); assertEquals(1,r.code()); assertTrue(r.text().contains("Could not load world")); }));
        result.add(DynamicTest.dynamicTest("TC-A188 | EC-M | prompted bad filename then valid filename", () -> { ProcessResult r=console(List.of(),"build/missing.xml\ninput.xml\nexit\n"); assertEquals(0,r.code()); assertTrue(r.text().contains("Could not load world")); assertTrue(r.text().contains("Welcome, Squash")); }));
        result.add(DynamicTest.dynamicTest("TC-A189 | EC-M | unknown command and blank line after valid file", () -> { ProcessResult r=console(List.of("input.xml"),"\ndance\nexit\n"); assertEquals(0,r.code()); assertTrue(r.text().contains("Unknown command")); assertTrue(r.text().contains("Goodbye!")); }));
        return result.stream();
    }
    private static Room room(String name) { return new Room(name,"description",RoomState.HALF_DIRTY); }
    private static void fill(World w,Room r,int n) {
        for(int i=0;i<n;i++) w.addCreature(new Npc(r.name()+" resident "+i,"resident"),r);
    }
    private static World load(String xml) throws Exception {
        Path path=Files.createTempFile("csc385-unit-world-",".xml");
        try { Files.writeString(path,xml,StandardCharsets.UTF_8); return new WorldLoader().load(path); }
        finally { Files.deleteIfExists(path); }
    }
    private static final class F {
        final World w=new World(); final Room a=room("A"),b=room("B");
        final Player p=new Player("Hero","player"); final List<String> log=new ArrayList<>();
        final GameEngine game;
        F() { w.addRoom(a); w.addRoom(b); w.addCreature(p,a); a.connect(Direction.NORTH,b); game=new GameEngine(w,new Random(0),log::add); }
        void run(String text) { game.execute(new CommandParser().parse(text)); }
    }
    private record ProcessResult(int code,String text) { }
    private static ProcessResult console(List<String> args,String stdin) throws Exception {
        List<String> command=new ArrayList<>();
        command.add(Path.of(System.getProperty("java.home"),"bin","java").toString());
        command.add("-cp"); command.add("build/classes"); command.add("csc241.project1.Main"); command.addAll(args);
        Process process=new ProcessBuilder(command).redirectErrorStream(true).start();
        try(OutputStream out=process.getOutputStream()) { out.write(stdin.getBytes(StandardCharsets.UTF_8)); }
        assertTrue(process.waitFor(10,TimeUnit.SECONDS),"console must finish");
        return new ProcessResult(process.exitValue(),new String(process.getInputStream().readAllBytes(),StandardCharsets.UTF_8));
    }
}
