package lotr;

import com.google.gson.annotations.Expose;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import lotr.Constants.ClassType;
import lotr.util.Sound;
import lotr.util.Sounds;

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
    public final Status[] status = new Status[]{new Status(), new Status(), new Status(), new Status()};

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
        int attempts = 0;
        int totalPlayers = armies.length;

        do {
            turnIndex = (turnIndex + 1) % totalPlayers;
            attempts++;
        } while ((armies[turnIndex] == null || armies[turnIndex].battalions.isEmpty()) && attempts <= totalPlayers);

        updateStandings();

        for (AdventureCard c : AdventureCard.values()) {
            c.setUsed(false);
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

        for (GameStepListener l : this.listeners) {
            l.nextStep(currentStep);
        }

        updateStandings();

        if (isGameOver()) {
            Sounds.play(Sound.FANFARE);
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

        if (a.leader1.territory == tc) {
            return true;
        }

        if (a.leader2.territory == tc) {
            return true;
        }
        
        return false;
    }

    public void removeLeader(Army a, TerritoryCard tc) {

        if (a.leader1.territory == tc) {
            a.leader1.territory = null;
        }

        if (a.leader2.territory == tc) {
            a.leader2.territory = null;
        }
    }

    public void moveLeader(Army a, TerritoryCard from, TerritoryCard to) {

        if (a.leader1.territory == from) {
            a.leader1.territory = to;
            Location sop = Location.getSiteOfPower(to);
            if (sop != null) {
                a.missionIndication1 = to;
            }
        }

        if (a.leader2.territory == from) {
            a.leader2.territory = to;
            Location sop = Location.getSiteOfPower(to);
            if (sop != null) {
                a.missionIndication2 = to;
            }
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

        for (Army army : armies) {
            if (army == null) {
                continue;
            }
            for (Battalion b : army.getBattalions()) {
                if (b.territory == tc) {
                    return army;
                }
            }
        }

        return null;
    }

    public int battalionCount(TerritoryCard tc) {
        int total = 0;

        for (Army army : armies) {
            if (army == null) {
                continue;
            }
            for (Battalion b : army.getBattalions()) {
                if (b.territory == tc) {
                    total++;
                }
            }
        }

        return total;
    }

    public Army getOccupyingArmy(TerritoryCard tc) {
        for (Army army : armies) {
            if (army == null) {
                continue;
            }
            for (Battalion b : army.getBattalions()) {
                if (b.territory == tc) {
                    return army;
                }
            }
        }
        return null;
    }

    public boolean isGameOver() {
        int aliveArmies = 0;

        for (Army army : armies) {
            if (army != null && !army.getBattalions().isEmpty()) {
                if (++aliveArmies > 1) {
                    return false; // More than one army still alive - game not over
                }
            }
        }

        return aliveArmies == 1; // Game is over if only one army is left standing
    }

    public Army tallyGameWinner() {
        Army winner = null;
        int highestScore = Integer.MIN_VALUE;

        for (Status s : status) {
            if (s.army != null && s.score > highestScore) {
                highestScore = s.score;
                winner = s.army;
            }
        }

        return winner;
    }

    public void turnInTerritoryCards(Army army, int sumArchers, int sumRiders, int sumEagles, int wildcardsUsed) {
        List<TerritoryCard> cards = army.territoryCards;

        AtomicInteger wildcardsUsedAtomic = new AtomicInteger(wildcardsUsed);

        if (sumArchers >= 1 && sumRiders >= 1 && sumEagles >= 1) {
            removeCardsOfType(cards, Constants.BattalionType.EAGLE, 1, wildcardsUsedAtomic);
            removeCardsOfType(cards, Constants.BattalionType.DARK_RIDER, 1, wildcardsUsedAtomic);
            removeCardsOfType(cards, Constants.BattalionType.ELVEN_ARCHER, 1, wildcardsUsedAtomic);
        } else if (sumEagles >= 3) {
            removeCardsOfType(cards, Constants.BattalionType.EAGLE, 3, wildcardsUsedAtomic);
        } else if (sumRiders >= 3) {
            removeCardsOfType(cards, Constants.BattalionType.DARK_RIDER, 3, wildcardsUsedAtomic);
        } else if (sumArchers >= 3) {
            removeCardsOfType(cards, Constants.BattalionType.ELVEN_ARCHER, 3, wildcardsUsedAtomic);
        }
    }

    private void removeCardsOfType(List<TerritoryCard> cards, Constants.BattalionType type, int count, AtomicInteger wildcardsUsed) {
        Iterator<TerritoryCard> iter = cards.iterator();
        int removed = 0;

        while (iter.hasNext() && removed < count) {
            TerritoryCard c = iter.next();
            Constants.BattalionType bt = c.battalionType();

            if (bt == type) {
                iter.remove();
                territoryCards.add(c);
                removed++;
            } else if (bt == null && wildcardsUsed.get() > 0) {
                iter.remove();
                territoryCards.add(c);
                removed++;
                wildcardsUsed.decrementAndGet();
            }
        }
    }

    public TerritoryCard findRandomEmptyTerritory(ClassType hint) {

        List<TerritoryCard> temp = new ArrayList<>();
        Collections.addAll(temp, TerritoryCard.values());
        temp.remove(TerritoryCard.WILD_CARD_1);
        temp.remove(TerritoryCard.WILD_CARD_2);

        if (red != null) {
            for (Battalion b : red.getBattalions()) {
                if (temp.contains(b.territory)) {
                    temp.remove(b.territory);
                }
            }
        }

        if (black != null) {
            for (Battalion b : black.getBattalions()) {
                if (temp.contains(b.territory)) {
                    temp.remove(b.territory);
                }
            }
        }

        if (green != null) {
            for (Battalion b : green.getBattalions()) {
                if (temp.contains(b.territory)) {
                    temp.remove(b.territory);
                }
            }
        }

        if (yellow != null) {
            for (Battalion b : yellow.getBattalions()) {
                if (temp.contains(b.territory)) {
                    temp.remove(b.territory);
                }
            }
        }

        if (temp.isEmpty()) {
            return null;
        }

        Random rand = new Random();

        //first try find one with the same class type as the army and is a stronghold with 2 chances
        for (int i = 0; i < 2; i++) {
            TerritoryCard t = temp.get(rand.nextInt(temp.size()));
            if (t.type() == hint && isStronghold(t)) {
                return t;
            }
        }

        //first try find one with the same class type as the army with 1 chance
        for (int i = 0; i < 1; i++) {
            TerritoryCard t = temp.get(rand.nextInt(temp.size()));
            if (t.type() == hint) {
                return t;
            }
        }

        //otherwise try find one that is neutral with 1 chance
        for (int i = 0; i < 1; i++) {
            TerritoryCard t = temp.get(rand.nextInt(temp.size()));
            if (t.type() == ClassType.NEUTRAL) {
                return t;
            }
        }

        //otherwise pick any random one
        return temp.get(rand.nextInt(temp.size()));
    }

    public boolean isStronghold(TerritoryCard t) {
        for (Location l : Location.values()) {
            if (l.getTerritory() == t && l.isStronghold()) {
                return true;
            }
        }
        return false;
    }

    public static class Status {

        public Army army;
        public int score;
        public int rcount;
        public int tcount;
        public int ccount;
        public int scount;
        public Map<Region, Integer> percentOwnershipInEachRegion;
    }

    public void updateStandings() {
        if (red != null) {
            setStatus(status[0], red);
        }
        if (green != null) {
            setStatus(status[1], green);
        }
        if (black != null) {
            setStatus(status[2], black);
        }
        if (yellow != null) {
            setStatus(status[3], yellow);
        }
    }

    private void setStatus(Status status, Army a) {
        status.army = a;

        List<TerritoryCard> claimedTerritories = a.claimedTerritories();
        List<Region> ownedRegions = a.ownedRegions(claimedTerritories);

        status.rcount = ownedRegions.size();
        status.tcount = claimedTerritories.size();
        status.ccount = a.territoryCards.size();
        status.scount = a.ownedStrongholds(claimedTerritories).size();

        status.score = status.tcount + status.scount * 2 + status.ccount + a.countAdventureCardsPlayed;

        for (Region r : ownedRegions) {
            status.score += r.reinforcements() * 2;
        }

        status.percentOwnershipInEachRegion = a.percentOwnershipInEachRegion(claimedTerritories);
    }

}
