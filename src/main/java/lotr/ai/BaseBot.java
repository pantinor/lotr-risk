package lotr.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import lotr.Army;
import lotr.Battalion;
import lotr.Game;
import lotr.GameScreen;
import lotr.TerritoryCard;
import lotr.util.Dice;

public abstract class BaseBot {

    public static class SortWrapper implements Comparable {

        int count;
        TerritoryCard territory;

        public SortWrapper(int count, TerritoryCard c) {
            this.count = count;
            this.territory = c;
        }

        @Override
        public int compareTo(Object obj) {
            SortWrapper other = (SortWrapper) obj;
            if (this.count > other.count) {
                return 1;
            } else if (this.count < other.count) {
                return -1;
            } else {
                return 0;
            }
        }

    }

    public static enum Type {
        STRONG, RANDOM, WEAK;
    }

    final Dice dice = new Dice();
    final Random rand = new Random();

    final Game game;
    final Army army;
    final GameScreen gameScreen;

    int attackerLosses;
    int defenderLosses;
    int attackerDice;
    int defenderDice;

    int[] attackerRolls;
    int[] defenderRolls;

    public BaseBot(Game game, Army army, GameScreen gameScreen) {
        this.game = game;
        this.army = army;
        this.gameScreen = gameScreen;
    }

    public void attack(TerritoryCard from, TerritoryCard to) {
        if (to == null) {
            //Sounds.play(Sound.NEGATIVE_EFFECT);
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
                    //TODO check mission card
                }

                if (this.gameScreen != null) {
                    this.gameScreen.turnWidget.setConqueredTerritory(true);
                }

                log(String.format("%s conquered %s and reinforced with %d battalions.", army.armyType, to.title(), tempCount));

            }

            //TODO defeated totally remove from game
        }
    }

    public void fortify(TerritoryCard from, TerritoryCard to) {

        if (to == null) {
            //Sounds.play(Sound.NEGATIVE_EFFECT);
            return;
        }

        int fortifyCount = game.battalionCount(from) - 1;
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
    }

    public abstract void reinforce(TerritoryCard to);

    public abstract TerritoryCard findClaimedTerritory(boolean mayInvadeFrom);

    public abstract TerritoryCard pickTerritoryToAttack(TerritoryCard from);

    public TerritoryCard pickTerritoryToFortify(TerritoryCard from) {
        TerritoryCard picked = null;
        List<SortWrapper> sorted = new ArrayList<>();
        for (TerritoryCard adj : from.adjacents()) {
            Army defender = game.getOccupyingArmy(adj);
            if (defender == army) {
                int count = game.battalionCount(adj);
                sorted.add(new SortWrapper(count, adj));
            }
        }
        Collections.sort(sorted);
        picked = !sorted.isEmpty() ? sorted.get(0).territory : null;
        return picked;
    }

    protected boolean rollAttack(TerritoryCard from, TerritoryCard to) {
        attackerLosses = 0;
        defenderLosses = 0;

        int invaderCount = game.battalionCount(from);
        int defenderCount = game.battalionCount(to);
        Army defender = game.getOccupyingArmy(to);

        if (invaderCount == 1) {
            return false;
        }

        if (defenderCount == 0) {
            return false;
        }

        attackerDice = invaderCount == 2 ? 1 : invaderCount == 3 ? 2 : 3;
        defenderDice = defenderCount == 1 ? 1 : 2;

        attackerRolls = new int[attackerDice];
        for (int i = 0; i < attackerDice; i++) {
            int r = dice.roll();
            if (game.hasLeader(army, from)) {
                r++;
            }
            attackerRolls[i] = r;
        }
        defenderRolls = new int[defenderDice];
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
            //Sounds.play(Sound.NEGATIVE_EFFECT);
            army.removeBattalion(from);
        }

        for (int i = 0; i < defenderLosses; i++) {
            //Sounds.play(Sound.POSITIVE_EFFECT);
            defender.removeBattalion(to);
        }

        log(String.format("%s lost %d battalion(s) from %s and %s lost %d battalion(s) from %s.",
                army.armyType, attackerLosses, from, defender.armyType, defenderLosses, to));

        return true;
    }

    public void log(String text) {
        if (this.gameScreen != null) {
            this.gameScreen.logs.add(text);
        } else {
            System.out.println(text);
        }
    }

}
