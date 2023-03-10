package lotr;

import com.google.gson.annotations.Expose;
import java.util.ArrayList;
import java.util.List;
import lotr.Constants.ArmyType;

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

    @Expose
    public int turnIndex = 0;

    @Expose
    public final List<TerritoryCard> territoryCards = new ArrayList<>();

    @Expose
    public List<AdventureCard> adventureCards = new ArrayList<>();

    public Game() {

    }

    public Army current() {
        return armies[turnIndex];
    }

    public Army next() {
        this.turnIndex++;
        if (this.turnIndex > 3 || (this.turnIndex == 3 && this.yellow == null)) {
            this.turnIndex = 0;
        }
        return armies[turnIndex];
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

    public ArmyType getOccupyingArmy(TerritoryCard tc) {

        if (this.red != null) {
            for (Battalion b : this.red.getBattalions()) {
                if (b.territory == tc) {
                    return ArmyType.RED;
                }
            }
        }
        if (this.black != null) {
            for (Battalion b : this.black.getBattalions()) {
                if (b.territory == tc) {
                    return ArmyType.BLACK;
                }
            }
        }
        if (this.green != null) {
            for (Battalion b : this.green.getBattalions()) {
                if (b.territory == tc) {
                    return ArmyType.GREEN;
                }
            }
        }
        if (this.yellow != null) {
            if (this.yellow != null) {
                for (Battalion b : this.yellow.getBattalions()) {
                    if (b.territory == tc) {
                        return ArmyType.YELLOW;
                    }
                }
            }
        }

        return null;
    }

}
