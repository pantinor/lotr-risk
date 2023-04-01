package lotr.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lotr.Army;
import lotr.Game;
import lotr.Game.Step;
import lotr.TerritoryCard;

public class StrongBot extends BaseBot {

    public StrongBot(Game game, Army army) {
        super(game, army);
    }

    @Override
    public void attack() {
        TerritoryCard pickedFromTerritory = null;
        while ((pickedFromTerritory = pickClaimedTerritory(Step.COMBAT)) != null && rand.nextInt(100) < 75) {
            TerritoryCard pickedToTerritory = pickTerritoryToAttack(pickedFromTerritory);
            attack(pickedFromTerritory, pickedToTerritory);
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
        picked = !sorted.isEmpty() ? sorted.get(0).territory : null;
        return picked;
    }

    @Override
    public TerritoryCard pickClaimedTerritory(Game.Step step) {
        List<SortWrapper> sorted = sortedClaimedTerritories(step);
        TerritoryCard picked = !sorted.isEmpty() ? sorted.get(0).territory : null;
        return picked;
    }

}
