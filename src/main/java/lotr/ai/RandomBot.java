package lotr.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lotr.Army;
import lotr.Constants;
import lotr.Game;
import lotr.GameScreen;
import lotr.Location;
import lotr.Region;
import lotr.TerritoryCard;

public class RandomBot extends BaseBot {

    public RandomBot(Game game, Army army, GameScreen gameScreen) {
        super(game, army, gameScreen);
    }

    @Override
    public TerritoryCard findClaimedTerritory(boolean mayInvadeFrom) {
        List<TerritoryCard> claimedTerritories = army.claimedTerritories();
        List<SortWrapper> sorted = new ArrayList<>();
        for (TerritoryCard c : TerritoryCard.values()) {
            if (claimedTerritories.contains(c)) {
                int count = game.battalionCount(c);
                if (mayInvadeFrom && count > 1) {
                    sorted.add(new SortWrapper(count, c));
                } else {
                    sorted.add(new SortWrapper(count, c));
                }
            }
        }
        Collections.sort(sorted);
        return sorted.get(rand.nextInt(sorted.size())).territory;
    }

    @Override
    public void reinforce(TerritoryCard country) {

        List<TerritoryCard> claimedTerritories = army.claimedTerritories();
        List<Location> strongholds = army.ownedStrongholds(claimedTerritories);
        List<Region> ownedRegions = new ArrayList<>();

        int strongholdReinforcements = 0, territoryReinforcements = 0, regionReinforcements = 0, cardReinforcements = 0;
        int sumArchers = 0, sumRiders = 0, sumEagles = 0;

        strongholdReinforcements = strongholds.size();
        territoryReinforcements = claimedTerritories.size() / 3 < 3 ? 3 : claimedTerritories.size() / 3;

        for (Region r : Region.values()) {
            if (claimedTerritories.containsAll(r.territories())) {
                regionReinforcements += r.reinforcements();
                ownedRegions.add(r);
            }
        }

        for (TerritoryCard c : army.territoryCards) {
            if (c.battalionType() == Constants.BattalionType.ELVEN_ARCHER || c.battalionType() == null) {
                sumArchers++;
            }
            if (c.battalionType() == Constants.BattalionType.DARK_RIDER || c.battalionType() == null) {
                sumRiders++;
            }
            if (c.battalionType() == Constants.BattalionType.EAGLE || c.battalionType() == null) {
                sumEagles++;
            }
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

        if (strongholdReinforcements > 0) {
            for (TerritoryCard c : TerritoryCard.values()) {
                if (claimedTerritories.contains(c) && Location.getStronghold(c) != null) {
                    army.addBattalion(c);
                    strongholdReinforcements--;
                }
            }
        }

        if (territoryReinforcements > 0) {
            List<SortWrapper> sorted = new ArrayList<>();
            for (TerritoryCard c : TerritoryCard.values()) {
                if (claimedTerritories.contains(c)) {
                    sorted.add(new SortWrapper(game.battalionCount(c), c));
                }
            }
            //randomy pick territories to reinforce
            Collections.sort(sorted);
            for (int i = 0; i < territoryReinforcements; i++) {
                army.addBattalion(sorted.get(rand.nextInt(sorted.size())).territory);
            }
        }

        if (regionReinforcements > 0) {
            List<SortWrapper> sorted = new ArrayList<>();
            for (Region region : ownedRegions) {
                for (TerritoryCard c : TerritoryCard.values()) {
                    if (claimedTerritories.contains(c) && region.territories().contains(c)) {
                        sorted.add(new SortWrapper(game.battalionCount(c), c));
                    }
                }

            }
            //randomly reinforce
            Collections.sort(sorted);
            for (int i = 0; i < regionReinforcements; i++) {
                army.addBattalion(sorted.get(rand.nextInt(sorted.size())).territory);
            }
        }

        if (cardReinforcements > 0) {
            List<SortWrapper> sorted = new ArrayList<>();
            for (TerritoryCard c : TerritoryCard.values()) {
                if (claimedTerritories.contains(c)) {
                    sorted.add(new SortWrapper(game.battalionCount(c), c));
                }
            }
            //randomly reinforce
            Collections.sort(sorted);
            for (int i = 0; i < cardReinforcements; i++) {
                army.addBattalion(sorted.get(rand.nextInt(sorted.size())).territory);
            }

            game.turnInTerritoryCards(army, sumArchers, sumRiders, sumEagles);
        }

    }

    @Override
    public TerritoryCard pickTerritoryToAttack(TerritoryCard from) {
        TerritoryCard picked = null;
        List<SortWrapper> sorted = new ArrayList<>();
        for (TerritoryCard adj : from.adjacents()) {
            Army defender = game.getOccupyingArmy(adj);
            if (defender != army) {
                int count = game.battalionCount(adj);
                sorted.add(new SortWrapper(count, adj));
            }
        }
        Collections.sort(sorted);
        picked = !sorted.isEmpty() ? sorted.get(rand.nextInt(sorted.size())).territory : null;
        return picked;
    }

}
