package csc241.project1;

import csc241.project1.cli.Command;
import csc241.project1.cli.CommandParser;
import csc241.project1.game.GameEngine;
import csc241.project1.io.WorldFormatException;
import csc241.project1.io.WorldLoader;
import csc241.project1.model.World;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Random;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("Usage: java csc241.project1.Main [world.xml]");
            System.exit(1);
        }
        try {
            run(args, new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)));
        } catch (IOException ex) {
            System.err.println("Could not read console input: " + ex.getMessage());
            System.exit(1);
        }
    }

    private static void run(String[] args, BufferedReader input) throws IOException {
        System.out.println("CSC241 Virtual World");
        World world = null;
        while (world == null) {
            String filename;
            if (args.length == 1) filename = args[0];
            else {
                System.out.print("World XML file [input.xml]: ");
                System.out.flush();
                filename = input.readLine();
                if (filename == null) return;
                if (filename.isBlank()) filename = "input.xml";
            }
            try {
                world = new WorldLoader().load(Path.of(filename.trim()));
            } catch (IOException | WorldFormatException | InvalidPathException ex) {
                System.out.println("Could not load world: " + ex.getMessage());
                if (args.length == 1) {
                    System.exit(1);
                    return;
                }
            }
        }
        GameEngine game = new GameEngine(world, new Random(), System.out::println);
        CommandParser parser = new CommandParser();
        System.out.println("Welcome, " + world.player().name() + ". Type help for commands.");
        game.look(world.player());
        while (game.isRunning()) {
            System.out.print("> ");
            System.out.flush();
            String line = input.readLine();
            if (line == null) {
                game.execute(new Command(Command.Kind.EXIT, null, null));
                break;
            }
            if (line.isBlank()) continue;
            try {
                game.execute(parser.parse(line));
            } catch (IllegalArgumentException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}
