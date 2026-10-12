package csc241.project1.model;

public final class Player extends Creature {
    public static final int INITIAL_RESPECT = 40;
    private int respect = INITIAL_RESPECT;

    public Player(String name, String description) { super(name, description); }
    public int respect() { return respect; }
    public void changeRespect(int amount) { respect += amount; }
    @Override public String type() { return "PC"; }
    @Override public boolean accepts(RoomState state) { return true; }
    @Override public RoomAction preferredAction() { return null; }
    @Override public Reaction reactionTo(RoomAction action) { return null; }
    @Override public Reaction discontentReaction() { return null; }
    @Override public boolean isPlayer() { return true; }
}
