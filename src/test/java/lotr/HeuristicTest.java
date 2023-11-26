/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lotr;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lotr.ai.HeuristicBot;
import lotr.ai.RandomBot;
import lotr.ai.StrongBot;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

/**
 *
 * @author panti
 */
public class HeuristicTest {

    @Test
    public void testAI() throws Exception {

        TerritoryCard.init();

        /*
        InputStream is = null;
        String json = null;

        is = new FileInputStream("savedGame.json");
        json = IOUtils.toString(is);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.excludeFieldsWithoutExposeAnnotation().create();

        Game game = gson.fromJson(json, new TypeToken<lotr.Game>() {
        }.getType());
         */
        int games = 0;
        while (games < 10) {
            Game game = createNewGame();

            game.setRed(game.red);
            game.setGreen(game.green);
            game.setBlack(game.black);
            game.setYellow(game.yellow);

            game.red.bot = new StrongBot(game, game.red);
            game.green.bot = new StrongBot(game, game.green);
            game.black.bot = new StrongBot(game, game.black);
            game.yellow.bot = new HeuristicBot(game, game.yellow);

            List<Army> players = new ArrayList<>();
            Random r = new Random();
            while (true) {
                int selected = r.nextInt(4);
                if (!players.contains(game.armies[selected])) {
                    players.add(game.armies[selected]);
                }
                if (players.size() == 4) {
                    break;
                }
            }

            int round = 0;

            while (round < 40) {
                for (Army army : players) {
                    army.bot.reinforce();
                    army.bot.attack();
                    army.bot.fortify();

                    if (army.leader1.territory == null && army.leader2.territory == null) {
                        List<TerritoryCard> claimedTerritories = army.claimedTerritories();
                        List<Location> strongholds = army.ownedStrongholds(claimedTerritories);
                        army.leader1.territory = !strongholds.isEmpty() ? strongholds.get(0).getTerritory() : claimedTerritories.get(0);
                    }
                }

                game.updateStandings();

//                System.out.printf("Round: %d\n", round);
//                for (Army a : game.armies) {
//                    System.out.printf("%s - B: %d  T: %d  R: %d  S: %d Cards: %d  Threat: %d\n", a.armyType,
//                            game.status[a.armyType.ordinal()].bcount, game.status[a.armyType.ordinal()].tcount,
//                            game.status[a.armyType.ordinal()].rcount, game.status[a.armyType.ordinal()].scount,
//                            game.status[a.armyType.ordinal()].ccount, game.status[a.armyType.ordinal()].threat);
//                }

                round++;
            }

            System.out.printf("Game: %d\n", games);
            for (Army a : game.armies) {
                System.out.printf("%s - B: %d  T: %d  R: %d  S: %d Cards: %d  Threat: %d\n", a.armyType,
                        game.status[a.armyType.ordinal()].bcount, game.status[a.armyType.ordinal()].tcount,
                        game.status[a.armyType.ordinal()].rcount, game.status[a.armyType.ordinal()].scount,
                        game.status[a.armyType.ordinal()].ccount, game.status[a.armyType.ordinal()].threat);
            }

            games++;
        }

    }

    private Game createNewGame() throws Exception {
        Game game = new Game();

        Army red = new Army(Constants.ArmyType.RED, Constants.ClassType.EVIL, 45);
        Army green = new Army(Constants.ArmyType.GREEN, Constants.ClassType.GOOD, 45);
        Army black = new Army(Constants.ArmyType.BLACK, Constants.ClassType.EVIL, 45);
        Army yellow = new Army(Constants.ArmyType.YELLOW, Constants.ClassType.GOOD, 45);

        game.setRed(red);
        game.setGreen(green);
        game.setBlack(black);
        game.setYellow(yellow);

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

        //add leaders
        for (Army army : game.armies) {
            List<TerritoryCard> terrs = army.claimedTerritories();
            TerritoryCard t = terrs.remove(rand.nextInt(terrs.size()));
            army.leader1.territory = t;
            t = terrs.remove(rand.nextInt(terrs.size()));
            army.leader2.territory = t;
        }

        return game;
    }

}
