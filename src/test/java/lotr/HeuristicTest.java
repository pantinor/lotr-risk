/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lotr;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lotr.ai.Attack;
import lotr.ai.HeuristicAI;
import lotr.ai.HeuristicBot;
import lotr.ai.StrongBot;
import org.testng.annotations.Test;

/**
 *
 * @author panti
 */
public class HeuristicTest {

    @Test
    public void testAI() throws Exception {

        Game game = new Game();

        TerritoryCard.init();

        Army red = new Army(Constants.ArmyType.RED, Constants.ClassType.EVIL, 45);
        Army green = new Army(Constants.ArmyType.GREEN, Constants.ClassType.GOOD, 45);
        Army black = new Army(Constants.ArmyType.BLACK, Constants.ClassType.EVIL, 45);
        Army yellow = new Army(Constants.ArmyType.YELLOW, Constants.ClassType.GOOD, 45);

        game.setRed(red);
        game.setGreen(green);
        game.setBlack(black);
        game.setYellow(yellow);

        game.red.bot = new HeuristicBot(game, game.red);
        game.black.bot = new HeuristicBot(game, game.black);
        game.green.bot = new HeuristicBot(game, game.green);
        game.yellow.bot = new HeuristicBot(game, game.yellow);

        List<TerritoryCard> evil = TerritoryCard.shuffledTerritoriesOfClass(Constants.ClassType.EVIL);
        List<TerritoryCard> good = TerritoryCard.shuffledTerritoriesOfClass(Constants.ClassType.GOOD);

        red.pickTerritories(evil, 8);
        black.pickTerritories(evil, 8);
        green.pickTerritories(good, 8);
        yellow.pickTerritories(good, 8);

        Random rand = new Random();

        List<TerritoryCard> temp = new ArrayList<>();
        for (TerritoryCard c : TerritoryCard.values()) {
            temp.add(c);
        }

        int count = temp.size();
        for (int i = 0; i < count; i++) {
            int r = rand.nextInt(temp.size());
            TerritoryCard c = temp.remove(r);
            game.territoryCards.add(c);
        }

        //claim empty territories
        int idx = rand.nextInt(4);
        while (true) {

            Army army = game.armies[idx];

            idx++;
            if (idx >= 4) {
                idx = 0;
            }

            TerritoryCard tc = game.findRandomEmptyTerritory(army.getClassType());
            if (tc != null) {
                army.assignTerritory(tc);
            } else {
                break;
            }

        }

        //reinforce territories
        while (true) {

            Army army = game.armies[idx];

            boolean done = true;
            for (Army a : game.armies) {
                for (Battalion b : a.battalions) {
                    if (b.territory == null) {
                        done = false;
                    }
                }
            }
            if (done) {
                break;
            }

            idx++;
            if (idx >= 4) {
                idx = 0;
            }

            List<TerritoryCard> terrs = army.claimedTerritories();

            if (terrs.isEmpty()) {
                continue;
            }

            TerritoryCard t = terrs.get(rand.nextInt(terrs.size()));
            army.assignTerritory(t);
        }

        int round = 0;
        while (round < 20) {
            for (Army army : game.armies) {
                army.bot.reinforce();
                army.bot.attack();
                army.bot.fortify();
            }

            game.updateStandings();
            
            System.out.printf("Round: %d\n", round);
            for (Army a : game.armies) {
                System.out.printf("%s - B: %d  T: %d  R: %d  S: %d Cards: %d  Threat: %d\n", a.armyType,
                        game.status[a.armyType.ordinal()].bcount, game.status[a.armyType.ordinal()].tcount,
                        game.status[a.armyType.ordinal()].rcount, game.status[a.armyType.ordinal()].scount,
                        game.status[a.armyType.ordinal()].ccount, game.status[a.armyType.ordinal()].threat);
            }
            round++;
        }

    }

}
