package lotr.ai;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import lotr.AdventureCard;
import lotr.Army;
import lotr.Battalion;
import lotr.Constants;
import lotr.Game;
import lotr.Game.Step;
import lotr.GameScreen;
import lotr.Location;
import lotr.Region;
import static lotr.Risk.GAME;
import lotr.TerritoryCard;
import lotr.util.CardAction;
import lotr.util.Dice;
import lotr.util.Logger;
import lotr.util.RingPathAction;

public abstract class BaseBot {

    public static class SortWrapper implements Comparable {

        int factor;
        TerritoryCard territory;

        public SortWrapper(int factor, TerritoryCard c) {
            this.factor = factor;
            this.territory = c;
        }

        @Override
        public int compareTo(Object obj) {
            SortWrapper other = (SortWrapper) obj;
            if (this.factor > other.factor) {
                return 1;
            } else if (this.factor < other.factor) {
                return -1;
            } else {
                return 0;
            }
        }

    }

    public static enum Type {
        STRONG, RANDOM, WEAK, HEURISTIC;
    }

    final Dice dice = new Dice();
    final Random rand = new Random();

    final Game game;
    final Army army;
    private GameScreen gameScreen;
    private Logger logger;
    private RingPathAction rpa;
    private CardAction cardAction;

    boolean conqueredTerritory, conqueredSOPWithLeader;

    public BaseBot(Game game, Army army) {
        this.game = game;
        this.army = army;
    }

    public void set(GameScreen gameScreen, RingPathAction rpa, CardAction cardAction) {
        this.gameScreen = gameScreen;
        this.logger = gameScreen.logs;
        this.rpa = rpa;
        this.cardAction = cardAction;
    }

    public SequenceAction run() {

        SequenceAction s = Actions.sequence();

        if (army.claimedTerritories().isEmpty()) {
            //TODO gome over remove me from game
            s.addAction(Actions.delay(1));
            return s;
        }

        conqueredTerritory = false;
        conqueredSOPWithLeader = false;

        RunnableAction r1 = new RunnableAction();
        r1.setRunnable(() -> {
            reinforce();
            game.nextStep();//attack
        });
        s.addAction(r1);

        s.addAction(Actions.delay(1));

        RunnableAction r2 = new RunnableAction();
        r2.setRunnable(() -> {
            attack();
            game.nextStep();//fortify
        });
        s.addAction(r2);

        s.addAction(Actions.delay(1));

        RunnableAction r3 = new RunnableAction();
        r3.setRunnable(() -> {
            fortify();
            game.nextStep();//tcard
        });
        s.addAction(r3);

        s.addAction(Actions.delay(1));

        RunnableAction r4 = new RunnableAction();
        r4.setRunnable(() -> {
            if (conqueredTerritory) {
                if (!game.territoryCards.isEmpty()) {
                    TerritoryCard newCard = game.territoryCards.remove(0);
                    game.current().territoryCards.add(newCard);
                    log(String.format("%s collected territory card [%s].", game.current().armyType, newCard.title()), game.current().armyType.color());
                }
            }
            game.nextStep();//acard
        });
        s.addAction(r4);

        s.addAction(Actions.delay(1));

        RunnableAction r5 = new RunnableAction();
        r5.setRunnable(() -> {
            if (conqueredSOPWithLeader) {
                cardAction.drawAdventureCard();
            }

            int count = game.current().adventureCards.size();
            if (rand.nextInt(2) == 1 && count > 0) {
                AdventureCard c = game.current().adventureCards.get(rand.nextInt(count));
                cardAction.process(c);
            }
            game.nextStep();//replace
        });
        s.addAction(r5);

        s.addAction(Actions.delay(1));

        RunnableAction r6 = new RunnableAction();
        r6.setRunnable(() -> {
            if (game.current().leader1.territory == null && game.current().leader2.territory == null) {
                List<TerritoryCard> claimedTerritories = game.current().claimedTerritories();
                List<Location> strongholds = game.current().ownedStrongholds(claimedTerritories);
                game.current().leader1.territory = !strongholds.isEmpty() ? strongholds.get(0).getTerritory() : claimedTerritories.get(0);
                log(String.format("%s replaced a leader to %s.", army.armyType, game.current().leader1.territory), army.armyType.color());
            } else if (game.current().leader1.territory != null && game.current().leader2.territory != null) {
                log(String.format("%s has both leaders on the map.", army.armyType), army.armyType.color());
            } else {
                log(String.format("%s is only missing one leader, and cannot replace a leader at this time.", army.armyType), army.armyType.color());
            }
            game.nextStep();//ring
        });
        s.addAction(r6);

        s.addAction(Actions.delay(1));

        RunnableAction r7 = new RunnableAction();
        r7.setRunnable(() -> {
            rpa.advance();//advance the ring
            game.nextStep();//draft next player and start turn
            if (GAME.current().isBot()) {
                s.addAction(game.current().bot.run());
            }
        });
        s.addAction(r7);

        return s;

    }

    public void attack(TerritoryCard from, TerritoryCard to) {

        if (to == null) {
            return;
        }

        while (rollAttack(from, to)) {

            int defenderCount = game.battalionCount(to);
            if (defenderCount == 0) {
                int tempCount;
                int reinforceCount = tempCount = game.battalionCount(from) - 1;
                for (Battalion b : army.getBattalions()) {
                    if (b.territory == from && reinforceCount > 0) {
                        b.territory = to;
                        reinforceCount--;
                    }
                }

                if (game.hasLeader(army, from)) {
                    game.moveLeader(army, from, to);
                    if (Location.getSiteOfPower(to) != null) {
                        conqueredSOPWithLeader = true;
                    }
                }

                conqueredTerritory = true;

                if (this.gameScreen != null) {
                    this.gameScreen.lookAt(to);
                }

                log(String.format("%s conquered %s and reinforced with %d battalions.", army.armyType, to.title(), tempCount), army.armyType.color());

            }

            //TODO defeated totally remove from game
        }
    }

    protected boolean rollAttack(TerritoryCard from, TerritoryCard to) {

        int invaderCount = game.battalionCount(from);
        int defenderCount = game.battalionCount(to);
        Army defender = game.getOccupyingArmy(to);

        if (invaderCount == 1) {
            return false;
        }

        if (defenderCount == 0) {
            return false;
        }

        int attackerLosses = 0;
        int defenderLosses = 0;
        int attackerDice = invaderCount == 2 ? 1 : invaderCount == 3 ? 2 : 3;
        int defenderDice = defenderCount == 1 ? 1 : 2;

        int[] attackerRolls = new int[attackerDice];
        int[] defenderRolls = new int[defenderDice];

        for (int i = 0; i < attackerDice; i++) {
            int r = dice.roll();
            if (game.hasLeader(army, from)) {
                r++;
            }
            attackerRolls[i] = r;
        }

        for (int i = 0; i < defenderDice; i++) {
            int r = dice.roll();
            if (game.hasLeader(defender, to)) {
                r++;
            }
            if (game.isDefendingStrongHold(to)) {
                r++;
            }
            defenderRolls[i] = r;
        }

        if (attackerRolls[0] > defenderRolls[0]) {
            defenderLosses++;
        } else {
            attackerLosses++;
        }

        if (attackerDice > 1 && defenderDice > 1) {
            if (attackerRolls[1] > defenderRolls[1]) {
                defenderLosses++;
            } else {
                attackerLosses++;
            }
        }

        for (int i = 0; i < attackerLosses; i++) {
            army.removeBattalion(from);
        }

        for (int i = 0; i < defenderLosses; i++) {
            defender.removeBattalion(to);
        }

        //log(String.format("%s lost %d battalion(s) from %s and %s lost %d battalion(s) from %s.",
        //        army.armyType, attackerLosses, from, defender.armyType, defenderLosses, to), army.armyType.color());
        return true;
    }

    public abstract void attack();

    public void reinforce() {

        List<TerritoryCard> claimedTerritories = army.claimedTerritories();

        int[] r = getReinforcementCounts();

        int strongholdReinforcements = r[0];
        int territoryReinforcements = r[1];
        int regionReinforcements = r[2];
        int cardReinforcements = r[3];
        int sumArchers = r[4];
        int sumRiders = r[5];
        int sumEagles = r[6];

        if (strongholdReinforcements > 0) {
            for (TerritoryCard c : claimedTerritories) {
                if (Location.getStronghold(c) != null) {
                    army.addBattalion(c);
                    strongholdReinforcements--;
                }
            }
        }

        List<SortWrapper> sorted = sortedClaimedTerritories(Step.COMBAT);

        if (territoryReinforcements > 0 && !sorted.isEmpty()) {
            for (int i = 0; i < territoryReinforcements; i++) {
                army.addBattalion(sorted.get(rand.nextInt(sorted.size())).territory);
            }
        }

        if (regionReinforcements > 0 && !sorted.isEmpty()) {
            for (int i = 0; i < territoryReinforcements; i++) {
                army.addBattalion(sorted.get(rand.nextInt(sorted.size())).territory);
            }
        }

        if (cardReinforcements > 0 && !sorted.isEmpty()) {
            for (int i = 0; i < territoryReinforcements; i++) {
                army.addBattalion(sorted.get(rand.nextInt(sorted.size())).territory);
            }
            game.turnInTerritoryCards(army, sumArchers, sumRiders, sumEagles);
        }
    }

    int[] getReinforcementCounts() {

        List<TerritoryCard> claimedTerritories = army.claimedTerritories();
        List<Location> strongholds = army.ownedStrongholds(claimedTerritories);

        int strongholdReinforcements = 0, territoryReinforcements = 0, regionReinforcements = 0, cardReinforcements = 0;
        int sumArchers = 0, sumRiders = 0, sumEagles = 0;

        strongholdReinforcements = strongholds.size();
        territoryReinforcements = claimedTerritories.size() / 3 < 3 ? 3 : claimedTerritories.size() / 3;

        for (Region r : Region.values()) {
            if (claimedTerritories.containsAll(r.territories())) {
                regionReinforcements += r.reinforcements();
            }
        }

        int wildcardcount = 0;
        for (TerritoryCard c : army.territoryCards) {
            if (c.battalionType() == Constants.BattalionType.ELVEN_ARCHER) {
                sumArchers++;
            }
            if (c.battalionType() == Constants.BattalionType.DARK_RIDER) {
                sumRiders++;
            }
            if (c.battalionType() == Constants.BattalionType.EAGLE) {
                sumEagles++;
            }
            if (c.battalionType() == null) {
                wildcardcount++;
            }
        }

        if (sumEagles == 2 && wildcardcount > 0) {
            sumEagles++;
            wildcardcount--;
        }
        if (sumRiders == 2 && wildcardcount > 0) {
            sumRiders++;
            wildcardcount--;
        }
        if (sumArchers == 2 && wildcardcount > 0) {
            sumArchers++;
            wildcardcount--;
        }

        if (sumArchers >= 3) {
            cardReinforcements = 4;
        }
        if (sumRiders >= 3) {
            cardReinforcements = 6;
        }
        if (sumEagles >= 3) {
            cardReinforcements = 8;
        }
        if (sumArchers >= 1 && sumRiders >= 1 && sumEagles >= 1) {
            cardReinforcements = 10;
        }

        log(String.format("%s is reinforcing territories with %d battalion(s).", army.armyType,
                strongholdReinforcements + territoryReinforcements + regionReinforcements + cardReinforcements), army.armyType.color());

        return new int[]{strongholdReinforcements, territoryReinforcements, regionReinforcements, cardReinforcements, sumArchers, sumRiders, sumEagles};
    }

    /**
     * The purpose of fortification is to strengthen territories which are
     * potentially attacked by enemies, so it is natural to limit the action
     * space to only these.
     *
     * @param step the step
     * @return the territory
     */
    public abstract TerritoryCard pickClaimedTerritory(Step step);

    /**
     * Pick a territory from which battalions may be sent from in the fortify
     * phase, or from which an attack may be made from in the attack phase.
     *
     * For fortify phase, return a list of owned territories sorted by battalion
     * count which have friendly adjacents.
     *
     * For combat phase, return a list of owned territories sorted by battalion
     * count which have enemy adjacents.
     *
     * @param step
     * @return
     */
    protected List<SortWrapper> sortedClaimedTerritories(Step step) {

        List<SortWrapper> sorted = new ArrayList<>();
        List<TerritoryCard> owned = army.claimedTerritories();

        for (TerritoryCard t : owned) {

            boolean hasLeader = game.hasLeader(army, t);
            int count = game.battalionCount(t);

            //check if territory has connected adjacents
            boolean friendlyadjacent = false;
            for (TerritoryCard adj : t.adjacents()) {
                if (game.isClaimed(adj) == army) {
                    friendlyadjacent = true;
                    break;
                }
            }

            //check if territory has enemy adjacents
            boolean enemyadjacent = false;
            for (TerritoryCard adj : t.adjacents()) {
                if (game.isClaimed(adj) != army) {
                    enemyadjacent = true;
                    break;
                }
            }

            if (step == Step.FORTIFY) {

                //a territory with a leader and single battalion with no enemy adjacents 
                //should qualify highest to fortify the leader to a better territory
                if (count == 1 && hasLeader && !enemyadjacent && friendlyadjacent) {
                    sorted.clear();
                    sorted.add(new SortWrapper(count, t));
                    break;
                } else if (count > 1 && hasLeader && !enemyadjacent && friendlyadjacent) {
                    //move the leader to a better territory that has enemy adjacents
                    sorted.clear();
                    sorted.add(new SortWrapper(count, t));
                    break;
                } else if (hasLeader && enemyadjacent) {
                    //leave the leader there - no need to move him
                } else if (friendlyadjacent && count > 1) {
                    sorted.add(new SortWrapper(count, t));
                }

            }

            if (step == Step.COMBAT) {
                if (enemyadjacent && count == 1 && hasLeader) {
                    sorted.clear();
                    sorted.add(new SortWrapper(count * 2, t));
                    break;
                } else if (enemyadjacent && count > 1 && hasLeader) {
                    sorted.add(new SortWrapper(count * 2, t));
                } else if (enemyadjacent && count > 1) {
                    sorted.add(new SortWrapper(count, t));
                }
            }

        }
        Collections.sort(sorted, Collections.reverseOrder());
        return sorted;
    }

    public abstract TerritoryCard pickTerritoryToAttack(TerritoryCard from);

    public void fortify() {
        TerritoryCard from = pickClaimedTerritory(Step.FORTIFY);
        if (from != null) {
            TerritoryCard to = pickTerritoryToFortify(from);
            fortify(from, to);
        } else {
            log(String.format("%s could not fortify any territories.", army.armyType), army.armyType.color());
        }
    }

    private void fortify(TerritoryCard from, TerritoryCard to) {

        int fortifyCount = game.battalionCount(from) - 1;

        if (to == null) {
            log(String.format("%s did NOT find territory to fortify from %s with %d battalion(s).", army.armyType, from.title(), fortifyCount), army.armyType.color());
            return;
        }

        log(String.format("%s fortified from %s to %s with %d battalion(s).", army.armyType, from.title(), to.title(), fortifyCount), army.armyType.color());

        for (Battalion b : army.getBattalions()) {
            if (b.territory == from && fortifyCount > 0) {
                b.territory = to;
                fortifyCount--;
            }
        }

        if (game.hasLeader(army, from)) {
            game.moveLeader(army, from, to);
            //TODO check mission card
        }

        if (this.gameScreen != null) {
            this.gameScreen.lookAt(to);
        }
    }

    private TerritoryCard pickTerritoryToFortify(TerritoryCard from) {
        List<TerritoryCard> targets = new ArrayList<>();
        List<TerritoryCard> owned = army.claimedTerritories();

        boolean fromTerritoryIsStronghold = game.isDefendingStrongHold(from);

        boolean fromTerritoryHasEnemyadjacents = false;
        for (TerritoryCard adj : from.adjacents()) {
            if (game.isClaimed(adj) != army) {
                fromTerritoryHasEnemyadjacents = true;
                break;
            }
        }

        for (TerritoryCard t : owned) {

            if (from == t) {
                continue;
            }

            if (!army.isConnected(from, t)) {
                continue;
            }

            boolean toTerritoryIsStronghold = game.isDefendingStrongHold(t);

            boolean toTerritoryHasEnemyadjacents = false;
            for (TerritoryCard adj : t.adjacents()) {
                if (game.isClaimed(adj) != army) {
                    toTerritoryHasEnemyadjacents = true;
                    break;
                }
            }

            if (toTerritoryIsStronghold && toTerritoryHasEnemyadjacents && !fromTerritoryIsStronghold && !fromTerritoryHasEnemyadjacents) {
                targets.clear();
                targets.add(t);
                break;
            } else if (!fromTerritoryHasEnemyadjacents && toTerritoryHasEnemyadjacents) {
                int fromcount = game.battalionCount(from);
                int tocount = game.battalionCount(t);
                if (tocount < fromcount) {
                    targets.add(t);
                }
            }
        }

        TerritoryCard picked = !targets.isEmpty() ? targets.get(0) : null;

        log(String.format("%s picked territory %s to fortify to.", army.armyType, picked != null ? picked.title() : "NONE"), army.armyType.color());

        return picked;
    }

    public void log(String text, Color color) {
        if (this.logger != null) {
            this.logger.log(text, color);
        } else {
            //System.out.println(text);
        }
    }

}
