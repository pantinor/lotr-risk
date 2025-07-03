package lotr.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lotr.Army;
import lotr.Constants.ArmyType;
import lotr.Game;
import lotr.Location;
import lotr.Region;
import lotr.Risk;
import lotr.TerritoryCard;
import org.apache.commons.collections.CollectionUtils;

public class HeuristicBot extends BaseBot {

    private final int attackQuitThreshold;

    public HeuristicBot(Game game, Army army, int attackQuitThreshold) {
        super(game, army);
        this.attackQuitThreshold = attackQuitThreshold;
    }
    
    @Override
    public void attack() {
        AttackChoice choice = evaluateAttackAlternatives(army.armyType, game);

        // FIX: Lower the score threshold to make the bot more willing to attack.
        while (choice != null && choice.score > 0.5 && rand.nextInt(100) < computeDynamicAttackThreshold()) { //
            army.bot.attack(choice.from.territory, choice.to.territory); //
            // Re-evaluate the situation after the attack changes the board state
            choice = evaluateAttackAlternatives(army.armyType, game); //
        }
    }

    private int computeDynamicAttackThreshold() {
        int myTerritories = army.claimedTerritories().size();
        int myBattalions = army.battalions.size();
        int totalTerritories = TerritoryCard.values().length - 2;//minus the 2 wildcards

        int strongestOpponentBattalions = 0;
        for (Army other : game.armies) {
            if (other != null && other != army && !other.battalions.isEmpty()) {
                strongestOpponentBattalions = Math.max(strongestOpponentBattalions, other.battalions.size());
            }
        }

        // Start from base threshold
        int dynamicThreshold = this.attackQuitThreshold;

        // If we are dominating the map, be more aggressive
        if (myTerritories > totalTerritories * 0.6) {
            dynamicThreshold += 10;  // Confidence boost
        }

        // If we have a significantly larger army, increase aggression
        if (myBattalions > strongestOpponentBattalions * 1.3) {
            dynamicThreshold += 5;
        }

        // If we are behind, lower aggression slightly to preserve units
        if (myBattalions < strongestOpponentBattalions * 0.7) {
            dynamicThreshold -= 10;
        }

        // Clamp to safe bounds
        return Math.max(40, Math.min(dynamicThreshold, 95));
    }

    @Override
    public TerritoryCard pickTerritoryToAttack(TerritoryCard from) {
        return null;//unused
    }

    @Override
    public TerritoryCard pickClaimedTerritory(Game.Step step) {
        List<SortWrapper> sorted = sortedClaimedTerritories(step);
        TerritoryCard picked = !sorted.isEmpty() ? sorted.get(0).territory : null;
        return picked;
    }

    private class AttackChoice implements Comparable<AttackChoice> {

        public TerritoryWrapper from;
        public TerritoryWrapper to;
        public double score; // Use a double for more nuanced scoring

        public AttackChoice(TerritoryWrapper from, TerritoryWrapper to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public int compareTo(AttackChoice other) {
            // Sort in descending order of score
            return Double.compare(other.score, this.score);
        }

    }

    private AttackChoice evaluateAttackAlternatives(ArmyType at, Game game) {
        HeuristicMap map = new HeuristicMap(game);
        map.evaluate(at, game.armies);
        List<AttackChoice> attackables = map.attackableTerritories(at);
        AttackChoice choice = !attackables.isEmpty() ? attackables.get(0) : null;
        return choice;
    }

    private class TerritoryWrapper {

        private ArmyType armyType;
        private TerritoryCard territory;
        private int battalionCount;
        private boolean hasLeader;
        private boolean isStrongHold;
        private int valuePoints;

        public boolean canBeAttackedToBreakUpRegion;// CAN I BREAK UP A REGION BY CONQUERING THIS TERRITORY?
        public boolean closeToCaptureRegion;// AM I CLOSE TO CAPTURING A REGION?
        public boolean lastTerritoryLeftInRegion;// IS THIS THE ONLY TERRITORY I NEED TO CAPTURE THE ENTIRE REGION?
        public boolean belongsToBigThreat;// DOES TERRITORY BELONG TO A BIG THREAT?
        public boolean opportunityToEliminatePlayer;// IS THERE ANY PLAYER NEARBY THAT I CAN ELIMINATE?

        public TerritoryWrapper(ArmyType armyType, TerritoryCard territory, int battalionCount, boolean hasLeader) {
            this.territory = territory;
            this.battalionCount = battalionCount;
            this.hasLeader = hasLeader;
            this.armyType = armyType;
        }

    }

    private class HeuristicMap {

        private final TerritoryWrapper[] territories = new TerritoryWrapper[64];

        public HeuristicMap(Game game) {
            for (int i = 2; i < 66; i++) {
                TerritoryCard t = TerritoryCard.values()[i];
                Army a = game.isClaimed(t);
                boolean hasLeader = (a.leader1 != null && a.leader1.territory == t) || (a.leader2 != null && a.leader2.territory == t);
                this.territories[i - 2] = new TerritoryWrapper(a.getArmyType(), t, game.battalionCount(t), hasLeader);
            }
        }

        private void evaluate(ArmyType player, Army[] armies) {

            int[] threats = new int[4];
            List<TerritoryWrapper>[] claimedTerritories = new ArrayList[4];
            List<Location>[] ownedStrongholds = new ArrayList[4];
            List<Region>[] ownedRegions = new ArrayList[4];
            Map<Region, Integer>[] percentOwnershipInEachRegion = new HashMap[4];

            for (Army a : armies) {
                if (a == null) {
                    continue;
                }
                claimedTerritories[a.armyType.ordinal()] = claimedTerritories(a.armyType);
                ownedStrongholds[a.armyType.ordinal()] = ownedStrongholds(claimedTerritories[a.armyType.ordinal()]);
                ownedRegions[a.armyType.ordinal()] = ownedRegions(claimedTerritories[a.armyType.ordinal()]);
                percentOwnershipInEachRegion[a.armyType.ordinal()] = percentOwnershipInEachRegion(claimedTerritories[a.armyType.ordinal()]);

                int bcount = 0;
                for (TerritoryWrapper tw : claimedTerritories[a.armyType.ordinal()]) {
                    bcount += tw.battalionCount;
                }

                int tcount = claimedTerritories[a.armyType.ordinal()].size();
                int scount = ownedStrongholds[a.armyType.ordinal()].size();

                int threat = 0;
                threat += bcount;
                threat += tcount;
                threat += scount * 2;

                for (Region r : ownedRegions[a.armyType.ordinal()]) {
                    threat += r.reinforcements() * 10;
                }

                threats[a.armyType.ordinal()] = threat;
            }

            for (TerritoryWrapper tw : this.territories) {
                tw.canBeAttackedToBreakUpRegion = tw.armyType != player && ownedRegions[tw.armyType.ordinal()].contains(tw.territory.region());
                tw.belongsToBigThreat = tw.armyType != player && threats[tw.armyType.ordinal()] >= threats[player.ordinal()];
                tw.closeToCaptureRegion = tw.armyType == player && percentOwnershipInEachRegion[player.ordinal()].get(tw.territory.region()) >= 69;
                tw.lastTerritoryLeftInRegion = territoriesInRegionNotOwnedByPlayer(tw.territory.region(), claimedTerritories[player.ordinal()]) == 1;
                tw.opportunityToEliminatePlayer = tw.armyType != player && claimedTerritories[tw.armyType.ordinal()].size() == 1;

                for (Location l : Location.values()) {
                    if (!l.isSiteOfPower()) {
                        if (tw.territory == l.getTerritory()) {
                            tw.isStrongHold = true;
                        }
                    }
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

        private List<AttackChoice> attackableTerritories(ArmyType at) {
            List<AttackChoice> attackables = new ArrayList<>();

            for (TerritoryWrapper tw : this.territories) {
                if (tw.armyType == at && tw.battalionCount > 1) { //
                    for (TerritoryCard adj : tw.territory.adjacents()) { //
                        TerritoryWrapper neighbour = get(adj);
                        if (at != neighbour.armyType) { //
                            attackables.add(new AttackChoice(tw, neighbour)); //
                        }
                    }
                }
            }

            for (AttackChoice choice : attackables) {
                // 1. Calculate the strategic value of the target
                // FIX: Add a base value of 1 for any attack.
                int strategicValue = 1;
                strategicValue += choice.to.hasLeader ? 3 : 0; //
                strategicValue += choice.to.opportunityToEliminatePlayer ? 5 : 0; //
                strategicValue += choice.to.belongsToBigThreat ? 1 : 0; //
                strategicValue += choice.to.lastTerritoryLeftInRegion ? 3 : 0; //
                strategicValue += choice.to.canBeAttackedToBreakUpRegion ? 2 : 0; //
                strategicValue += choice.to.isStrongHold ? 2 : 0; //

                // 2. Estimate tactical advantage
                double odds = calculateWinAdvantage(choice.from.battalionCount, choice.to.battalionCount, choice.from.hasLeader, choice.to.hasLeader, choice.to.isStrongHold);

                // 3. Combine value and odds for a final score
                choice.score = strategicValue * odds;
            }

            Collections.sort(attackables);
            return attackables;
        }

        // Helper method to estimate the advantage in a fight
        private double calculateWinAdvantage(int attackers, int defenders, boolean attackerHasLeader, boolean defenderHasLeader, boolean isStronghold) {
            if (attackers <= 1) {
                return 0;
            }

            // Effective strength considers bonuses
            double attackerStrength = attackers + (attackerHasLeader ? 1 : 0);
            double defenderStrength = defenders + (defenderHasLeader ? 1 : 0) + (isStronghold ? 1 : 0);

            if (attackerStrength <= defenderStrength) {
                return 0.3; // Low odds for an unfavorable attack
            }

            // A simple ratio can represent the advantage
            double advantageRatio = attackerStrength / defenderStrength;

            // Normalize to a value (e.g., between 0 and 1) to serve as a weight
            return advantageRatio / (advantageRatio + 1.0);
        }

        private List<TerritoryWrapper> claimedTerritories(ArmyType at) {
            List<TerritoryWrapper> tmp = new ArrayList<>();
            for (TerritoryWrapper tw : this.territories) {
                if (tw.armyType == at) {
                    tmp.add(tw);
                }
            }
            return tmp;
        }

        private List<Location> ownedStrongholds(List<TerritoryWrapper> claimedTerritories) {
            List<Location> tmp = new ArrayList<>();
            for (Location l : Location.values()) {
                if (!l.isSiteOfPower()) {
                    for (TerritoryWrapper t : claimedTerritories) {
                        if (t.territory == l.getTerritory()) {
                            tmp.add(l);
                        }
                    }
                }
            }
            return tmp;
        }

        private List<Region> ownedRegions(List<TerritoryWrapper> claimedTerritories) {
            List<Region> regions = new ArrayList<>();
            List<TerritoryCard> terrs = new ArrayList<>();
            for (TerritoryWrapper t : claimedTerritories) {
                terrs.add(t.territory);
            }
            for (Region r : Region.values()) {
                if (terrs.containsAll(r.territories())) {
                    regions.add(r);
                }
            }
            return regions;
        }

        private Map<Region, Integer> percentOwnershipInEachRegion(List<TerritoryWrapper> claimedTerritories) {
            Map<Region, Integer> tmp = new HashMap<>();
            List<TerritoryCard> terrs = new ArrayList<>();
            for (TerritoryWrapper t : claimedTerritories) {
                terrs.add(t.territory);
            }
            for (Region r : Region.values()) {
                int numberOfTerritoriesInRegion = r.territories().size();
                int territoriesOwnedInRegion = CollectionUtils.intersection(r.territories(), terrs).size();
                tmp.put(r, numberOfTerritoriesInRegion / (territoriesOwnedInRegion + 1));
            }
            return Risk.sortByDescendingValues(tmp);
        }

        private int territoriesInRegionNotOwnedByPlayer(Region region, List<TerritoryWrapper> claimedTerritories) {
            List<TerritoryCard> terrs = new ArrayList<>();
            for (TerritoryWrapper t : claimedTerritories) {
                terrs.add(t.territory);
            }
            int numberOfTerritoriesInRegion = region.territories().size();
            int territoriesOwnedInRegion = CollectionUtils.intersection(region.territories(), terrs).size();
            return numberOfTerritoriesInRegion - territoriesOwnedInRegion;
        }

    }

}
