package lotr;

import java.util.List;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import static lotr.Risk.GREEN_BATTALION;
import static lotr.Risk.BLACK_BATTALION;
import static lotr.Risk.BLACK_LEADER;
import static lotr.Risk.GREEN_LEADER;
import static lotr.Risk.RED_BATTALION;
import static lotr.Risk.RED_LEADER;
import static lotr.Risk.YELLOW_BATTALION;
import static lotr.Risk.YELLOW_LEADER;

public class Hud {

    private final Texture background = Risk.fillRectangle(300, 250, Color.GRAY, .5f);

    public void render(Batch batch, Game game) {

        int y = Risk.SCREEN_HEIGHT - 810;
        int py = Risk.SCREEN_HEIGHT - 855;

        batch.draw(this.background, 0, 0);

        for (Army a : game.armies) {

            if (a == null) {
                continue;
            }

            List<TerritoryCard> claimedTerritories = a.claimedTerritories();

            switch (a.armyType) {
                case RED:
                    Risk.regionLabelFont.setColor(Color.RED);
                    batch.draw(RED_BATTALION.getKeyFrame(0), 0, py);
                    batch.draw(RED_LEADER.getKeyFrame(0), 250, py);

                    break;
                case GREEN:
                    Risk.regionLabelFont.setColor(Color.GREEN);
                    batch.draw(GREEN_BATTALION.getKeyFrame(0), 0, py);
                    batch.draw(GREEN_LEADER.getKeyFrame(0), 250, py);

                    break;
                case BLACK:
                    Risk.regionLabelFont.setColor(Color.BLACK);
                    batch.draw(BLACK_BATTALION.getKeyFrame(0), 0, py);
                    batch.draw(BLACK_LEADER.getKeyFrame(0), 250, py);

                    break;
                case YELLOW:
                    Risk.regionLabelFont.setColor(Color.YELLOW);
                    batch.draw(YELLOW_BATTALION.getKeyFrame(0), 0, py);
                    batch.draw(YELLOW_LEADER.getKeyFrame(0), 250, py);

                    break;
            }

            String row1 = String.format("Battalions %d Territories %d", a.battalions.size(), claimedTerritories.size());
            String row2 = String.format("Regions %s  Strongholds %d", a.ownedRegions(claimedTerritories).size(), a.ownedStrongholds(claimedTerritories).size());

            Risk.regionLabelFont.draw(batch, a.armyType.toString(), 50, y);
            Risk.regionLabelFont.draw(batch, row1, 50, y - 17);
            Risk.regionLabelFont.draw(batch, row2, 50, y - 34);

            y -= 60;
            py -= 60;

        }

        Risk.regionLabelFont.setColor(Color.WHITE);

    }
}
