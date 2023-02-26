package lotr;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import static org.testng.Assert.assertEquals;
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
            game.deck.add(c);
        }

        //claim empty territories
        int idx = rand.nextInt(4);
        while (true) {

            Army army = game.armies[idx];

            idx++;
            if (idx >= 4) {
                idx = 0;
            }

            TerritoryCard tc = findRandomEmptyTerritory(game);
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

    public TerritoryCard findRandomEmptyTerritory(Game game) {

        List<TerritoryCard> temp = new ArrayList<>();
        Collections.addAll(temp, TerritoryCard.values());

        for (Battalion b : game.red.getBattalions()) {
            if (temp.contains(b.territory)) {
                temp.remove(b.territory);
            }
        }

        for (Battalion b : game.black.getBattalions()) {
            if (temp.contains(b.territory)) {
                temp.remove(b.territory);
            }
        }

        for (Battalion b : game.green.getBattalions()) {
            if (temp.contains(b.territory)) {
                temp.remove(b.territory);
            }
        }

        if (game.yellow != null) {
            for (Battalion b : game.yellow.getBattalions()) {
                if (temp.contains(b.territory)) {
                    temp.remove(b.territory);
                }
            }
        }

        if (temp.isEmpty()) {
            return null;
        }

        Random rand = new Random();
        int r = rand.nextInt(temp.size());

        return temp.get(r);
    }

}
