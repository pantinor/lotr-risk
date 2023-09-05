package lotr.ai;

import java.util.List;
import lotr.Army;
import lotr.Game;
import lotr.TerritoryCard;

public class HeuristicBot extends BaseBot {

    public HeuristicBot(Game game, Army army) {
        super(game, army);
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
