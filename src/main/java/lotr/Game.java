package lotr;

import com.google.gson.annotations.Expose;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Game {

    @Expose
    public Army red;
    @Expose
    public Army green;
    @Expose
    public Army black;
    @Expose
    public Army yellow;

    public final Army[] armies = new Army[4];

    public Step currentStep = Step.DRAFT;
    private List<GameStepListener> listeners = new ArrayList<>();

    @Expose
    public int turnIndex = 0;

    @Expose
    public final List<TerritoryCard> territoryCards = new ArrayList<>();

    @Expose
    public List<AdventureCard> adventureCards = new ArrayList<>();

    public static enum Step {
        DRAFT("Receive and Place Reinforcements"),
        COMBAT("Combat"),
        FORTIFY("Fortify Your Position"),
        TCARD("Collect a Terriritory Card"),
        ACARD("Collect a Adventure Card"),
        REPLACE("Replace a Leader"),
        RING("Move the Fellowship");

        private final String desc;

        private Step(String desc) {
            this.desc = desc;
        }

        public String desc() {
            return desc;
        }

    }

    public static interface GameStepListener {

        public void nextStep(Step step);
    }

    public Game() {

    }

    public Army current() {
        return armies[turnIndex];
    }

    public Army nextPlayer() {
        this.turnIndex++;
        if (this.turnIndex > 3 || (this.turnIndex == 3 && this.yellow == null)) {
            this.turnIndex = 0;
        }
        return armies[turnIndex];
    }
    
    public void registerListener(GameStepListener l) {
        this.listeners.add(l);
    }
    
    public void nextStep() {
        
        int next = this.currentStep.ordinal() + 1;
        if (next >= Step.values().length) {
            next = 0;
            nextPlayer();
        }
        
        this.currentStep = Step.values()[next];
        
        for(GameStepListener l : this.listeners) {
            l.nextStep(currentStep);
        }
    }

    public Army getRed() {
        return red;
    }

    public void setRed(Army a) {
        this.armies[0] = a;
        this.red = a;
    }

    public Army getGreen() {
        return green;
    }

    public void setGreen(Army a) {
        this.armies[1] = a;
        this.green = a;
    }

    public Army getBlack() {
        return black;
    }

    public void setBlack(Army a) {
        this.armies[2] = a;
        this.black = a;
    }

    public Army getYellow() {
        return yellow;
    }

    public void setYellow(Army a) {
        this.armies[3] = a;
        this.yellow = a;
    }

    public boolean hasLeader(Army a, TerritoryCard tc) {

        if (a.leader1 != null && a.leader1.territory == tc) {
            return true;
        }

        if (a.leader2 != null && a.leader2.territory == tc) {
            return true;
        }
        return false;
    }

    public void removeLeader(Army a, TerritoryCard tc) {

        if (a.leader1 != null && a.leader1.territory == tc) {
            a.leader1.territory = null;
        }

        if (a.leader2 != null && a.leader2.territory == tc) {
            a.leader2.territory = null;
        }
    }

    public void moveLeader(Army a, TerritoryCard from, TerritoryCard to) {

        if (a.leader1 != null && a.leader1.territory == from) {
            a.leader1.territory = to;
        }

        if (a.leader2 != null && a.leader2.territory == from) {
            a.leader2.territory = to;
        }
    }

    public boolean isDefendingStrongHold(TerritoryCard to) {
        for (Location l : Location.values()) {
            if (!l.isSiteOfPower() && l.getTerritory() == to) {
                return true;
            }
        }
        return false;
    }

    public Army isClaimed(TerritoryCard tc) {

        if (this.red == null) {
            return null;
        }

        for (Battalion b : this.red.getBattalions()) {
            if (b.territory == tc) {
                return this.red;
            }
        }

        for (Battalion b : this.black.getBattalions()) {
            if (b.territory == tc) {
                return this.black;
            }
        }

        for (Battalion b : this.green.getBattalions()) {
            if (b.territory == tc) {
                return this.green;
            }
        }

        if (this.yellow != null) {
            for (Battalion b : this.yellow.getBattalions()) {
                if (b.territory == tc) {
                    return this.yellow;
                }
            }
        }

        return null;
    }

    public int battalionCount(TerritoryCard tc) {

        if (this.red == null) {
            return 0;
        }

        int count = 0;

        for (Battalion b : this.red.getBattalions()) {
            if (b.territory == tc) {
                count++;
            }
        }

        if (count > 0) {
            return count;
        }

        for (Battalion b : this.black.getBattalions()) {
            if (b.territory == tc) {
                count++;
            }
        }

        if (count > 0) {
            return count;
        }

        for (Battalion b : this.green.getBattalions()) {
            if (b.territory == tc) {
                count++;
            }
        }

        if (count > 0) {
            return count;
        }

        if (this.yellow != null) {
            for (Battalion b : this.yellow.getBattalions()) {
                if (b.territory == tc) {
                    count++;
                }
            }
        }

        return count;
    }

    public Army getOccupyingArmy(TerritoryCard tc) {

        if (this.red != null) {
            for (Battalion b : this.red.getBattalions()) {
                if (b.territory == tc) {
                    return this.red;
                }
            }
        }
        if (this.black != null) {
            for (Battalion b : this.black.getBattalions()) {
                if (b.territory == tc) {
                    return this.black;
                }
            }
        }
        if (this.green != null) {
            for (Battalion b : this.green.getBattalions()) {
                if (b.territory == tc) {
                    return this.green;
                }
            }
        }
        if (this.yellow != null) {
            if (this.yellow != null) {
                for (Battalion b : this.yellow.getBattalions()) {
                    if (b.territory == tc) {
                        return this.yellow;
                    }
                }
            }
        }

        return null;
    }

    public void turnInTerritoryCards(Army army, int sumArchers, int sumRiders, int sumEagles) {
        if (sumArchers >= 1 && sumRiders >= 1 && sumEagles >= 1) {
            Iterator<TerritoryCard> iter = army.territoryCards.iterator();
            while (iter.hasNext()) {
                TerritoryCard c = iter.next();
                if (c.battalionType() == Constants.BattalionType.EAGLE || c.battalionType() == null) {
                    iter.remove();
                    break;
                }
            }
            iter = army.territoryCards.iterator();
            while (iter.hasNext()) {
                TerritoryCard c = iter.next();
                if (c.battalionType() == Constants.BattalionType.DARK_RIDER || c.battalionType() == null) {
                    iter.remove();
                    territoryCards.add(c);
                }
            }
            iter = army.territoryCards.iterator();
            while (iter.hasNext()) {
                TerritoryCard c = iter.next();
                if (c.battalionType() == Constants.BattalionType.ELVEN_ARCHER || c.battalionType() == null) {
                    iter.remove();
                    territoryCards.add(c);
                }
            }
        } else if (sumEagles >= 3) {
            Iterator<TerritoryCard> iter = army.territoryCards.iterator();
            int removed = 0;
            while (iter.hasNext()) {
                TerritoryCard c = iter.next();
                if (removed < 3 && c.battalionType() == Constants.BattalionType.EAGLE || c.battalionType() == null) {
                    iter.remove();
                    territoryCards.add(c);
                    removed++;
                }
            }
        } else if (sumRiders >= 3) {
            Iterator<TerritoryCard> iter = army.territoryCards.iterator();
            int removed = 0;
            while (iter.hasNext()) {
                TerritoryCard c = iter.next();
                if (removed < 3 && c.battalionType() == Constants.BattalionType.DARK_RIDER || c.battalionType() == null) {
                    iter.remove();
                    territoryCards.add(c);
                    removed++;
                }
            }
        } else if (sumArchers >= 3) {
            Iterator<TerritoryCard> iter = army.territoryCards.iterator();
            int removed = 0;
            while (iter.hasNext()) {
                TerritoryCard c = iter.next();
                if (removed < 3 && c.battalionType() == Constants.BattalionType.ELVEN_ARCHER || c.battalionType() == null) {
                    iter.remove();
                    territoryCards.add(c);
                    removed++;
                }
            }
        }
    }

}
