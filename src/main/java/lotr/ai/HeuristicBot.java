package lotr.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lotr.Army;
import lotr.Game;
import lotr.Location;
import lotr.TerritoryCard;
import lotr.ai.HeuristicMap.Pair;
import lotr.ai.HeuristicMap.SortArrDesc;

public class HeuristicBot extends BaseBot {

    public HeuristicBot(Game game, Army army) {
        super(game, army);
    }

    @Override
    public void reinforce() {

        List<TerritoryCard> claimedTerritories = army.claimedTerritories();

        int[] r = getReinforcementCounts();

        int reinforcements = r[0] + r[1] + r[2] + r[3];

        int strongholdReinforcements = r[0];
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

        List<Pair> NBSRarr1 = new ArrayList<>();
        List<Pair> NBSRarr2 = new ArrayList<>();
        List<Pair> BSRXarr = new ArrayList<>();

        int BSTx = 0;//Border Security Threat
        double BSRx = 0;//Border Security Ratio
        double sumBSRz = 0;
        double sumNBSRz = 0;

        for (int i = 0; i < claimedTerritories.size(); i++) {
            BSTx = 0;
            BSRx = 0;
            TerritoryCard t = claimedTerritories.get(i);
            for (TerritoryCard adj : t.adjacents()) {
                if (game.isClaimed(adj) != army) {
                    BSTx += game.battalionCount(adj);
                }
            }
            BSRx = BSTx / game.battalionCount(t);
            BSRXarr.add(new Pair(BSRx, i));
        }

        Collections.sort(BSRXarr, new SortArrDesc());

        for (int i = 0; i < BSRXarr.size(); i++) {
            HeuristicMap.Pair tempPair = BSRXarr.get(i);
            sumBSRz += tempPair.xBSR;
            if (NBSRarr1.size() < BSRXarr.size() / 2) {
                NBSRarr1.add(tempPair);
            }
        }

        //Normalized Border Security Ratio
        for (HeuristicMap.Pair pair : NBSRarr1) {
            NBSRarr2.add(new HeuristicMap.Pair(pair.xBSR / sumBSRz, pair.xIndex));
            sumNBSRz += pair.xBSR / sumBSRz;
        }

        if (!NBSRarr2.isEmpty()) {

            while (reinforcements > 0) {

                int tempUnitsToAdd = reinforcements;

                for (Pair nbsrPair : NBSRarr2) {

                    double toAddDouble = (nbsrPair.xBSR / sumNBSRz) * reinforcements;
                    int rounded = (int) Math.round(toAddDouble);
                    if (rounded == 0) {
                        rounded = 1;
                    }

                    TerritoryCard t = claimedTerritories.get(nbsrPair.xIndex);

                    if (tempUnitsToAdd - rounded >= 0) {
                        for (int i = 0; i < rounded; i++) {
                            army.addBattalion(t);
                        }
                        log(String.format("%s reinforced %s with %d battalion(s).", army.armyType, t, rounded), army.armyType.color());
                        tempUnitsToAdd -= rounded;
                        if (tempUnitsToAdd == 0) {
                            break;
                        }
                    } else {
                        for (int i = 0; i < tempUnitsToAdd; i++) {
                            army.addBattalion(t);
                        }
                        log(String.format("%s reinforced %s with %d battalion(s).", army.armyType, t, rounded), army.armyType.color());
                        break;
                    }
                }

                reinforcements = tempUnitsToAdd;
            }
        }

        if (cardReinforcements > 0) {
            game.turnInTerritoryCards(army, sumArchers, sumRiders, sumEagles);
        }
    }

    @Override
    public void attack() {
        HeuristicAI ai = new HeuristicAI();
        Attack a = ai.attack(army.armyType, game);
        while (a != null) {
            army.bot.attack(a.from(), a.to());
            ai = new HeuristicAI();
            a = ai.attack(army.armyType, game);
        }
    }

    @Override
    public TerritoryCard pickTerritoryToAttack(TerritoryCard from) {
        return null;
    }

    @Override
    public TerritoryCard pickClaimedTerritory(Game.Step step) {
        List<SortWrapper> sorted = sortedClaimedTerritories(step);
        TerritoryCard picked = !sorted.isEmpty() ? sorted.get(0).territory : null;
        return picked;
    }

}
