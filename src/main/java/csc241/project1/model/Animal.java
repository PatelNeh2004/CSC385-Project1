package csc241.project1.model;

public final class Animal extends Creature {
    public Animal(String name, String description) { super(name, description); }
    @Override public String type() { return "animal"; }
    @Override public boolean accepts(RoomState state) { return state != RoomState.DIRTY; }
    @Override public RoomAction preferredAction() { return RoomAction.CLEAN; }
    @Override public Reaction reactionTo(RoomAction action) {
        return action == RoomAction.CLEAN ? Reaction.LICK_FACE : Reaction.GROWL;
    }
    @Override public Reaction discontentReaction() { return Reaction.GROWL; }
}
