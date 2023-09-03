package lotr.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lotr.Army;
import lotr.Game;
import lotr.TerritoryCard;

public class WeakBot extends BaseBot {

    public WeakBot(Game game, Army army) {
        super(game, army);
    }

    @Override
    public void attack() {
        TerritoryCard pickedFromTerritory = pickClaimedTerritory(Game.Step.COMBAT);
        if (pickedFromTerritory != null) {
            TerritoryCard pickedToTerritory = pickTerritoryToAttack(pickedFromTerritory);
            attack(pickedFromTerritory, pickedToTerritory);
        }
    }

    @Override
    public TerritoryCard pickClaimedTerritory(Game.Step step) {
        List<SortWrapper> sorted = sortedClaimedTerritories(step);
        TerritoryCard picked = !sorted.isEmpty() ? sorted.get(sorted.size() - 1).territory : null;
        return picked;
    }

    @Override
    public TerritoryCard pickTerritoryToAttack(TerritoryCard from) {
        TerritoryCard picked = null;
        List<SortWrapper> sorted = new ArrayList<>();
        for (TerritoryCard adj : from.adjacents()) {
            Army defender = game.getOccupyingArmy(adj);
            if (defender != army) {
                int count = game.battalionCount(adj);
                boolean hasLeader = game.hasLeader(defender, adj);
                sorted.add(new SortWrapper(count, hasLeader, adj));
            }
        }
        Collections.sort(sorted, Collections.reverseOrder());
        picked = !sorted.isEmpty() ? sorted.get(sorted.size() - 1).territory : null;
        return picked;
    }

}
