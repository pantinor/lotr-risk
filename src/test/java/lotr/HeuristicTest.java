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
        game.green.bot = new HeuristicBot(game, game.green);
        game.black.bot = new HeuristicBot(game, game.black);
        game.yellow.bot = new HeuristicBot(game, game.yellow);

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
