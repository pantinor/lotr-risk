package lotr;

import com.badlogic.gdx.Gdx;
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

    private final Texture frame = new Texture(Gdx.files.classpath("assets/data/hud-frame.png"));
    private final Texture background = Risk.fillRectangle(300, 62 * 4, new Color(0, 0, .62f, .8f));
    private final Texture highlighter = Risk.fillRectangle(296, 58, new Color(.7f, .3f, .5f, .7f));
    private final Texture defeated = Risk.fillRectangle(296, 58, new Color(.7f, .7f, .7f, .7f));
    
    protected float time = 0;

    public void render(Batch batch, Game game, float delta) {

        time += delta;

        int y = Risk.SCREEN_HEIGHT - 815;
        int py = Risk.SCREEN_HEIGHT - 855;

        batch.draw(this.background, 0, 0);
        batch.draw(this.frame, 0, 0);
        
        for (Army a : game.armies) {

            if (a == game.current()) {
                batch.draw(this.highlighter, 2, y - 47);
            }
            
            if (a.battalions.size() == 0) {
                batch.draw(this.defeated, 2, y - 47);
            }

            if (a == null) {
                continue;
            }

            List<TerritoryCard> claimedTerritories = a.claimedTerritories();

            switch (a.armyType) {
                case RED:
                    batch.draw(RED_BATTALION.getKeyFrame(time, true), 0, py);
                    batch.draw(RED_LEADER.getKeyFrame(time, true), 250, py);
                    break;
                case GREEN:
                    batch.draw(GREEN_BATTALION.getKeyFrame(time, true), 0, py);
                    batch.draw(GREEN_LEADER.getKeyFrame(time, true), 250, py);
                    break;
                case BLACK:
                    batch.draw(BLACK_BATTALION.getKeyFrame(time, true), 0, py);
                    batch.draw(BLACK_LEADER.getKeyFrame(time, true), 250, py);
                    break;
                case YELLOW:
                    batch.draw(YELLOW_BATTALION.getKeyFrame(time, true), 0, py);
                    batch.draw(YELLOW_LEADER.getKeyFrame(time, true), 250, py);
                    break;
            }

            String row1 = String.format("B: %d  T: %d  R: %d  S: %d", 
                    game.status[a.armyType.ordinal()].bcount, game.status[a.armyType.ordinal()].tcount,
                    game.status[a.armyType.ordinal()].rcount, game.status[a.armyType.ordinal()].scount);
            
            String row2 = String.format("Cards: %d  Threat: %d", 
                    game.status[a.armyType.ordinal()].ccount, game.status[a.armyType.ordinal()].threat);

            //Risk.font.setColor(a.armyType.color());
            Risk.font.draw(batch, row1, 50, y);
            Risk.font.draw(batch, row2, 50, y - 20);

            y -= 62;
            py -= 62;

        }

        Risk.font.setColor(Color.WHITE);

    }
}
