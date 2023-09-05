package lotr.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lotr.Army;
import lotr.Constants.ArmyType;
import lotr.Game;
import lotr.TerritoryCard;
import static lotr.ai.HeuristicAI.MINIMUM_DIFFERENCE_TO_ATTACK;

public class HeuristicMap {

    private static class TerritoryWrapper {

        private final TerritoryCard territory;
        private final int battalionCount;
        private final boolean hasLeader;
        private final ArmyType armyType;

        public TerritoryWrapper(ArmyType armyType, TerritoryCard territory, int battalionCount, boolean hasLeader) {
            this.territory = territory;
            this.battalionCount = battalionCount;
            this.hasLeader = hasLeader;
            this.armyType = armyType;
        }
    }

    private final TerritoryWrapper[] territories = new TerritoryWrapper[64];

    public HeuristicMap(Game game) {
        for (int i = 2; i < 66; i++) {
            TerritoryCard t = TerritoryCard.values()[i];
            Army a = game.isClaimed(t);
            boolean hasLeader = (a.leader1 != null && a.leader1.territory == t) || (a.leader2 != null && a.leader2.territory == t);
            this.territories[i - 2] = new TerritoryWrapper(a.getArmyType(), t, game.battalionCount(t), hasLeader);
        }
    }

    public HeuristicMap(HeuristicMap copy) {
        for (int i = 0; i < 64; i++) {
            TerritoryWrapper c = copy.territories[i];
            this.territories[i] = new TerritoryWrapper(c.armyType, c.territory, c.battalionCount, c.hasLeader);
        }
    }

    public int currentTerritoryCount(ArmyType at) {
        int count = 0;
        for (TerritoryWrapper t : this.territories) {
            if (t.armyType == at) {
                count++;
            }
        }
        return count;
    }

    public boolean isBorderingEnemy(ArmyType at, TerritoryCard territory) {
        for (TerritoryCard adj : territory.adjacents()) {
            TerritoryWrapper neighbour = this.get(adj);
            if (at != neighbour.armyType) {
                return true;
            }
        }
        return false;
    }

    private void incrementTerritoryBattalion(TerritoryWrapper t, int amt) {
        for (int i = 0; i < this.territories.length; i++) {
            TerritoryWrapper tw = this.territories[i];
            if (tw.territory == t.territory) {
                this.territories[i] = new TerritoryWrapper(t.armyType, t.territory, t.battalionCount + amt, t.hasLeader);
            }
        }
    }

    private TerritoryWrapper get(TerritoryCard territory) {
        for (TerritoryWrapper t : this.territories) {
            if (territory == t.territory) {
                return t;
            }
        }
        return null;
    }

    /**
     * https://github.com/arman-aminian/risk-game-ai-agent
     *
     * 3.2.3.Prediction Tree Heuristics
     *
     * In order to evaluate how beneficial a tree leaf is for that specific
     * player; we have defined four heuristic features which their best possible
     * weights are found during the learning of the heuristic which will be
     * explained later.All features return a result between zero to one because
     * when the features are valued in close ratios to each other, the learning
     * and assigning weight would be more accurate.The features are:
     *
     * The ratio of the number of all the territories a player owns to all
     * territories that the map has.The ratio of the number of all the units a
     * player owns to all units that are in the map.For each territory x the
     * player owns, for each enemy territory y adjacent to it we calculate :
     *
     * After that we calculate the average of all z’s and the result is the
     * third feature we use. It shows our defensive power and the closer it gets
     * to one it shows we’ve got higher defensive strength.
     *
     * For each territory x the player owns, if y is number of neighbor enemy
     * territories, and z is the number of neighbor territories the player owns,
     * p will be calculated :
     *
     * After that we calculate the average of all p’s = AVGp. (1 – AVGp)
     * indicates how dense and close together our territories are which the
     * higher the better because a winning strategy for this game for example
     * when there are continents, is to focus on one continent and to conquer it
     * all and then move to other continents.
     *
     * @param at
     * @param map
     * @return
     */
    public static double[] heuristicFeatures(ArmyType at, HeuristicMap map) {

        double homeRatio = 0;
        double soldierRatio = 0;
        double totalSoldiers = 0;
        double totalRatioOfHouseEnemyNeighbours = 0;
        double totalRatioOfSoldiersEnemyNeighbours = 0;

        for (TerritoryWrapper t : map.territories) {
            totalSoldiers += t.battalionCount;
            if (t.armyType == at) {
                homeRatio++;
                soldierRatio += t.battalionCount;

                double[] temp = enemyNeighbourRatios(t, map);
                totalRatioOfHouseEnemyNeighbours += temp[0];
                totalRatioOfSoldiersEnemyNeighbours += temp[1];
            }
        }

        double[] features = {
            homeRatio / map.territories.length,
            soldierRatio / totalSoldiers,
            totalRatioOfHouseEnemyNeighbours / homeRatio,
            totalRatioOfSoldiersEnemyNeighbours / homeRatio
        };

        return features;
    }

    private static double[] enemyNeighbourRatios(TerritoryWrapper t, HeuristicMap map) {

        int neighbours = 0;
        double enemyNeighbours = 0;
        double ratio = 0;
        double[] answers = new double[2];

        for (TerritoryCard adj : t.territory.adjacents()) {
            TerritoryWrapper neighbour = map.get(adj);
            neighbours++;
            if (t.armyType != neighbour.armyType) {
                enemyNeighbours++;
                ratio += (double) t.battalionCount / (double) (t.battalionCount + neighbour.battalionCount);
            }
        }

        answers[0] = enemyNeighbours / neighbours;

        if (enemyNeighbours == 0) {
            answers[1] = 1;
        } else {
            answers[1] = ratio / enemyNeighbours;
        }

        return answers;

    }

    public List<Attack> attackChances(ArmyType player, HeuristicMap map) {
        List<Attack> chances = new ArrayList<>();

        for (TerritoryWrapper tw : this.territories) {

            if (tw.armyType != player) {
                continue;
            }

            if (isBorderingEnemy(player, tw.territory)) {
                for (TerritoryCard adj : tw.territory.adjacents()) {
                    TerritoryWrapper neighbour = this.get(adj);
                    if (player != neighbour.armyType) {
                        int max = MINIMUM_DIFFERENCE_TO_ATTACK;
                        if (tw.battalionCount - neighbour.battalionCount > max) {
                            max = tw.battalionCount - neighbour.battalionCount;
                            if (max > MINIMUM_DIFFERENCE_TO_ATTACK) {
                                Attack attack = new Attack(player, tw.territory, neighbour.territory, neighbour.battalionCount + (max + 1) / 2);
                                chances.add(attack);
                            }
                        }
                    }
                }
            }
        }

        return chances;
    }

    public void updateMap(Attack chance) {
        TerritoryWrapper from = this.get(chance.from());
        for (int i = 0; i < this.territories.length; i++) {
            TerritoryWrapper tw = this.territories[i];
            if (tw.territory == chance.from()) {
                this.territories[i] = new TerritoryWrapper(chance.attacker(), chance.from(), tw.battalionCount - chance.battalionCount(), false);
            }
            if (tw.territory == chance.to()) {
                this.territories[i] = new TerritoryWrapper(chance.attacker(), chance.to(), chance.battalionCount() - tw.battalionCount, from.hasLeader);
            }
        }

    }

    private static final int DRAFT_TRESHOLD = 2;

    private static final List<Pair> NBSRarr1 = new ArrayList<>();
    private static final List<Pair> NBSRarr2 = new ArrayList<>();
    private static final List<Pair> BSRXarr = new ArrayList<>();
    private static final List<TerritoryWrapper> CLAIMED = new ArrayList<>();

    /**
     * 3.2.1. Drafting Phase
     *
     * Supplying units to territories can be a tricky task; in the sense that we
     * want to improve our attack power but also make sure of having sufficient
     * defense power in territories that are in danger of getting attacked.
     * Deploying units in countries adjacent to enemy territories could be a
     * smart way of keeping balance between these two goals.
     *
     * Draft Simplification: Predicting and including all possible draft
     * scenarios in the prediction tree will lead to a complex tree that would
     * face the problems mentioned before; therefore, one of the simplifications
     * made is that we use a drafting heuristic which is proved to always have
     * the best possible result and predict that the opponents make this
     * approach in drafting as well ; In other words , we remove the drafting
     * results as possibilities form the prediction tree and change them to
     * definitive draft scenarios.
     *
     * In order to do so, we take the following steps:
     *
     * Draft Heuristic:
     *
     * Step 1:
     *
     * aking the summation of all units in enemy countries y adjacent to country
     * x will give a measure which we call Border Security Threat (BST) in x.
     *
     * Step 2:
     *
     * Dividing this BST by the units situated in x gives a Border Security
     * Ratio (BSR) which can be compared among all border countries.
     *
     * Countries with a high BSR are more likely to be conquered by an enemy
     * player, since the number of enemy units in adjacent enemy countries are
     * relatively higher than the number of units on the country itself.
     * Choosing countries with a high BSR to supply to will increase their
     * defensive strength by lowering the BSR. Supplying units to countries with
     * a lower BSR, meaning that they already have a better defensive stance,
     * will increase their offensive strength, raising the chances of a
     * successful attack from these countries.
     *
     * Step 3:
     *
     * Normalizing the BSR by dividing it by the sum of all BSRs of countries, a
     * player owns, will give a direct measurement by which someone could
     * arrange units. The Normalized Border Security Ratio (NBSR) is calculated
     * by:
     *
     * It gives a direct ratio of how the units could be distributed among
     * countries. At this point we can see there would be a problem with this
     * ratios because some data is irrelevant , and we don’t want to add units
     * to all our territories so we set a threshold in between steps two and
     * three by sorting the BSRx data in a descending order(we are focusing more
     * on making the defense power stronger) , divide the data from middle and
     * set the numbers in the lower half to zero.
     *
     * Step 4 will be continued until no more available units are left to add.
     *
     * @param currentPlayer
     * @param currentMap
     * @return
     */
    public static TerritoryWrapper heuristicReinforce(ArmyType currentPlayer, HeuristicMap currentMap) {

        NBSRarr1.clear();
        NBSRarr2.clear();
        BSRXarr.clear();
        CLAIMED.clear();

        for (TerritoryWrapper t : currentMap.territories) {
            if (t.armyType == currentPlayer) {
                CLAIMED.add(t);
            }
        }
        int territoryCount = CLAIMED.size();

        int BSTx = 0;//Border Security Threat
        double BSRx = 0;//Border Security Ratio
        double sumBSRz = 0;
        double sumNBSRz = 0;

        for (int i = 0; i < territoryCount; i++) {
            BSTx = 0;
            BSRx = 0;
            TerritoryWrapper tx = CLAIMED.get(i);
            for (TerritoryCard adj : tx.territory.adjacents()) {
                TerritoryWrapper neighbour = currentMap.get(adj);
                if (currentPlayer != neighbour.armyType) {
                    BSTx += neighbour.battalionCount;
                }
            }
            BSRx = BSTx / tx.battalionCount;
            BSRXarr.add(new Pair(BSRx, i));
        }

        Collections.sort(BSRXarr, new SortArrDesc());

        for (int i = 0; i < BSRXarr.size(); i++) {
            Pair tempPair = BSRXarr.get(i);
            sumBSRz += tempPair.xBSR;
            if (NBSRarr1.size() < BSRXarr.size() / DRAFT_TRESHOLD) {
                NBSRarr1.add(tempPair);
            }
        }

        //Normalized Border Security Ratio
        for (Pair pair : NBSRarr1) {
            NBSRarr2.add(new Pair(pair.xBSR / sumBSRz, pair.xIndex));
            sumNBSRz += pair.xBSR / sumBSRz;
        }

        if (territoryCount == 1) {
            TerritoryWrapper fortify = CLAIMED.get(0);
            currentMap.incrementTerritoryBattalion(fortify, 1);
            return fortify;
        } else {
            while (territoryCount > 0) {

                int tempUnitsToAdd = territoryCount;
                for (Pair nbsrPair : NBSRarr2) {

                    double toAddDouble = (nbsrPair.xBSR / sumNBSRz) * territoryCount;
                    int rounded = (int) Math.round(toAddDouble);
                    if (rounded == 0) {
                        rounded = 1;
                    }

                    TerritoryWrapper eqTerr = CLAIMED.get(nbsrPair.xIndex);

                    if (tempUnitsToAdd - rounded >= 0) {
                        currentMap.incrementTerritoryBattalion(eqTerr, rounded);
                        tempUnitsToAdd -= rounded;
                        if (tempUnitsToAdd == 0) {
                            break;
                        }
                    } else {
                        currentMap.incrementTerritoryBattalion(eqTerr, tempUnitsToAdd);
                        break;
                    }
                }

                territoryCount = tempUnitsToAdd;
            }
        }

        return null;
    }

    private static class Pair {

        double xBSR;
        int xIndex;

        private Pair(double xBSR, int xIndex) {
            this.xBSR = xBSR;
            this.xIndex = xIndex;
        }
    }

    private static class SortArrDesc implements Comparator<Pair> {

        @Override
        public int compare(Pair a, Pair b) {
            return (int) (b.xBSR - a.xBSR);
        }
    }

}
