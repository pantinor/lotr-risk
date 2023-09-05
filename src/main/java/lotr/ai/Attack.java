package lotr.ai;

import lotr.Constants.ArmyType;
import lotr.TerritoryCard;

public class Attack {

    private final TerritoryCard from;
    private final TerritoryCard to;
    private final int battalionCount;
    private final ArmyType attacker;

    public Attack(ArmyType attacker, TerritoryCard from, TerritoryCard to, int battalionCount) {
        this.from = from;
        this.to = to;
        this.battalionCount = battalionCount;
        this.attacker = attacker;
    }

    public TerritoryCard from() {
        return from;
    }

    public TerritoryCard to() {
        return to;
    }

    public int battalionCount() {
        return battalionCount;
    }

    public ArmyType attacker() {
        return attacker;
    }

}
