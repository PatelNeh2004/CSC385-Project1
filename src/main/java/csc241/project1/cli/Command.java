package csc241.project1.cli;

import csc241.project1.model.Direction;

/** A missing actor name means the PC performs the command. */
public record Command(Kind kind, String actorName, Direction direction) {
    public enum Kind { LOOK, CLEAN, DIRTY, MOVE, HELP, RESPECT, EXIT }
}
