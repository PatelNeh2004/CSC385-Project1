package csc241.project1.model;

public final class Npc extends Creature {
    public Npc(String name, String description) { super(name, description); }
    @Override public String type() { return "NPC"; }
    @Override public boolean accepts(RoomState state) { return state != RoomState.CLEAN; }
    @Override public RoomAction preferredAction() { return RoomAction.DIRTY; }
    @Override public Reaction reactionTo(RoomAction action) {
        return action == RoomAction.DIRTY ? Reaction.SMILE : Reaction.GRUMBLE;
    }
    @Override public Reaction discontentReaction() { return Reaction.GRUMBLE; }
}
