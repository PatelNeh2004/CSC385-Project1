package csc241.project1.model;

public enum Reaction {
    SMILE("smiles at you", 1),
    GRUMBLE("grumbles at you", -1),
    LICK_FACE("licks your face", 1),
    GROWL("growls at you", -1);

    private final String message;
    private final int respectChange;

    Reaction(String message, int respectChange) {
        this.message = message;
        this.respectChange = respectChange;
    }

    public String message() { return message; }
    public int respectChange() { return respectChange; }
}
