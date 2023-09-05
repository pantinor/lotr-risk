package lotr;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lotr.Game.Step;
import lotr.ai.BaseBot;
import lotr.ai.RandomBot;
import lotr.ai.StrongBot;
import lotr.ai.WeakBot;
import org.apache.commons.io.IOUtils;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

public class FourPlayerGameTest {

    @Test
    public void testInit4PlayerGame() {

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

        for (Army army : game.armies) {
            List<TerritoryCard> terrs = army.claimedTerritories();

            TerritoryCard t = terrs.remove(rand.nextInt(terrs.size()));
            army.leader1.territory = t;

            t = terrs.remove(rand.nextInt(terrs.size()));
            army.leader2.territory = t;

            army.adventureCards.add(AdventureCard.BREE);
            army.territoryCards.add(t);
        }

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.excludeFieldsWithoutExposeAnnotation().create();

        String json = gson.toJson(game);

        //System.out.println(json);
        Game test = gson.fromJson(json, new TypeToken<Game>() {
        }.getType());

        String json2 = gson.toJson(test);

        //System.out.println(json2);
        assertEquals(json, json2);

    }

    @Test
    public void testBotAttack() throws Exception {

        TerritoryCard.init();

        InputStream is = null;
        String json = null;

        is = new FileInputStream("savedGame.json");
        json = IOUtils.toString(is);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.excludeFieldsWithoutExposeAnnotation().create();

        Game game = gson.fromJson(json, new TypeToken<lotr.Game>() {
        }.getType());

        game.setRed(game.red);
        game.setGreen(game.green);
        game.setBlack(game.black);
        game.setYellow(game.yellow);

        game.red.bot = new StrongBot(game, game.red);
        game.green.bot = new WeakBot(game, game.red);
        game.black.bot = new RandomBot(game, game.red);
        game.yellow.bot = new StrongBot(game, game.red);

        TerritoryCard pickedFromTerritory = game.red.bot.pickClaimedTerritory(Step.COMBAT);
        assertNotNull(pickedFromTerritory);
        TerritoryCard pickedToTerritory = game.red.bot.pickTerritoryToAttack(pickedFromTerritory);
        assertNotNull(pickedToTerritory);
        game.black.bot.reinforce();
        game.black.bot.attack(pickedFromTerritory, pickedToTerritory);
        
        TerritoryCard f = game.red.bot.pickClaimedTerritory(Step.FORTIFY);
        assertNotNull(f);

    }

    @Test
    public void testConnected() throws Exception {

        TerritoryCard.init();
        for (TerritoryCard t : TerritoryCard.values()) {
            //System.out.printf("%s(%s, %s, Region.%s),\n", t, t.type(), t.battalionType(), t.region());
        }
        System.out.println("-------------");
        for (Region r : Region.values()) {
            for (TerritoryCard t : TerritoryCard.values()) {
                if (t.region() == r) {
                    //System.out.printf("%s(%s, %s, Region.%s),\n", t, t.type(), t.battalionType(), r);
                }
            }
        }

        Army a = new Army();
        a.battalions = new ArrayList<>();

        a.addBattalion(TerritoryCard.BARAD_DUR);
        a.addBattalion(TerritoryCard.GORGOROTH);
        a.addBattalion(TerritoryCard.MOUNT_DOOM);
        a.addBattalion(TerritoryCard.NURN);
        a.addBattalion(TerritoryCard.MINAS_MORGUL);
        a.addBattalion(TerritoryCard.BARAD_DUR);
        a.addBattalion(TerritoryCard.ITHILIEN);
        a.addBattalion(TerritoryCard.MINAS_TIRITH);
        a.addBattalion(TerritoryCard.HARONDOR);

        assertTrue(a.isConnected(TerritoryCard.BARAD_DUR, TerritoryCard.GORGOROTH));
        assertTrue(a.isConnected(TerritoryCard.BARAD_DUR, TerritoryCard.MINAS_TIRITH));
        assertFalse(a.isConnected(TerritoryCard.BARAD_DUR, TerritoryCard.HARONDOR));

        for (TerritoryCard t : TerritoryCard.values()) {
            a.addBattalion(t);
        }

        assertEquals(a.ownedRegions(a.claimedTerritories()).size(), 9);

        for (Region r : Region.values()) {

        }

    }

    @Test
    public void createCustomGame() throws Exception {

        TerritoryCard.init();

        Game game = new Game();

        Army red = new Army(Constants.ArmyType.RED, Constants.ClassType.EVIL, 45);
        Army green = new Army(Constants.ArmyType.GREEN, Constants.ClassType.GOOD, 45);
        Army black = new Army(Constants.ArmyType.BLACK, Constants.ClassType.EVIL, 45);
        Army yellow = new Army(Constants.ArmyType.YELLOW, Constants.ClassType.GOOD, 45);

        game.setRed(red);
        game.setGreen(green);
        game.setBlack(black);
        game.setYellow(yellow);

        for (TerritoryCard t : Region.ARNOR.territories()) {
            green.assignTerritory(t);
        }

        for (TerritoryCard t : Region.ERIADOR.territories()) {
            green.assignTerritory(t);
        }

        for (TerritoryCard t : Region.MORDOR.territories()) {
            red.assignTerritory(t);
        }

        for (TerritoryCard t : Region.HARADAWAITH.territories()) {
            red.assignTerritory(t);
        }

        for (TerritoryCard t : Region.RHOVANION.territories()) {
            yellow.assignTerritory(t);
        }

        for (TerritoryCard t : Region.MIRKWOOD.territories()) {
            yellow.assignTerritory(t);
        }

        for (TerritoryCard t : Region.GONDOR.territories()) {
            black.assignTerritory(t);
        }

        for (TerritoryCard t : Region.ROHAN.territories()) {
            black.assignTerritory(t);
        }

        for (TerritoryCard t : Region.RHUN.territories()) {
            black.assignTerritory(t);
        }

        for (Battalion b : green.battalions) {
            if (b.territory == null) {
                b.territory = TerritoryCard.RHUDAUR;
            }
        }

        for (Battalion b : red.battalions) {
            if (b.territory == null) {
                b.territory = TerritoryCard.UDUN_VALE;
            }
        }

        for (Battalion b : yellow.battalions) {
            if (b.territory == null) {
                b.territory = TerritoryCard.SOUTH_MIRKWOOD;
            }
        }

        for (Battalion b : black.battalions) {
            if (b.territory == null) {
                b.territory = TerritoryCard.MINAS_TIRITH;
            }
        }

        Random rand = new Random();

        for (Army army : game.armies) {
            List<TerritoryCard> terrs = army.claimedTerritories();
            TerritoryCard t = terrs.remove(rand.nextInt(terrs.size()));
            army.leader1.territory = t;
            t = terrs.remove(rand.nextInt(terrs.size()));
            army.leader2.territory = t;
        }

        List<TerritoryCard> temp = new ArrayList<>();
        for (TerritoryCard c : TerritoryCard.values()) {
            temp.add(c);
        }
        while (!temp.isEmpty()) {
            int r = rand.nextInt(temp.size());
            TerritoryCard c = temp.remove(r);
            game.territoryCards.add(c);
        }

        List<AdventureCard> adventureCards = AdventureCard.shuffledCardsWithoutEvents();

        //deal 1 territory card and 4 adventure cards to each player
        for (Army a : game.armies) {
            if (a != null) {
                TerritoryCard c = game.territoryCards.remove(0);
                a.addTerritoryCard(c);
                a.addAdventureCard(adventureCards.remove(0));
                a.addAdventureCard(adventureCards.remove(0));
                a.addAdventureCard(adventureCards.remove(0));
                a.addAdventureCard(adventureCards.remove(0));
            }
        }

        adventureCards = AdventureCard.shuffledCards();
        for (Army a : game.armies) {
            if (a != null) {
                for (AdventureCard c : a.adventureCards) {
                    adventureCards.remove(c);
                }
            }
        }

        game.adventureCards.addAll(adventureCards);

        red.botType = BaseBot.Type.HEURISTIC;
        black.botType = BaseBot.Type.HEURISTIC;
        yellow.botType = BaseBot.Type.HEURISTIC;
        green.botType = null;

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

        String json = gson.toJson(game);

        FileOutputStream fos = new FileOutputStream("savedGame.json");
        fos.write(json.getBytes("UTF-8"));
        fos.close();

    }

}
