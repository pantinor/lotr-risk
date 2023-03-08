package lotr.util;

public enum Sound {
    DICE("dice.wav", false, 0.3f),
    BLOCKED("blocked.ogg", false, 0.3f),
    FLEE("flee.ogg", false, 0.3f),
    ERROR("error.ogg", false, 0.3f),
    TRIGGER("trigger.ogg", false, 0.3f),
    PC_ATTACK("pc_attack.ogg", false, 0.3f),
    PC_STRUCK("pc_struck.ogg", false, 0.3f),
    NPC_ATTACK("npc_attack.ogg", false, 0.3f),
    NPC_STRUCK("npc_struck.ogg", false, 0.3f),
    EVADE("evade.ogg", false, 0.3f),
    POSITIVE_EFFECT("PositiveEffect.ogg", false, 0.3f),
    NEGATIVE_EFFECT("NegativeEffect.ogg", false, 0.3f),
    DIVINE_INTERVENTION("divineint.ogg", false, 0.3f);

    String file;
    boolean looping;
    float volume;

    private Sound(String name, boolean looping, float volume) {
        this.file = name;
        this.looping = looping;
        this.volume = volume;
    }

    public String getFile() {
        return this.file;
    }

    public boolean getLooping() {
        return this.looping;
    }

    public float getVolume() {
        return this.volume;
    }

}
