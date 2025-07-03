package lotr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.EnumMap;
import lotr.Constants.ArmyType;
import static lotr.Risk.BLACK_BATTALION;
import static lotr.Risk.BLACK_LEADER;
import static lotr.Risk.GREEN_BATTALION;
import static lotr.Risk.GREEN_LEADER;
import static lotr.Risk.RED_BATTALION;
import static lotr.Risk.RED_LEADER;
import static lotr.Risk.YELLOW_BATTALION;
import static lotr.Risk.YELLOW_LEADER;

public class Hud {

    private final Texture frame = new Texture(Gdx.files.classpath("assets/data/hud-frame.png"));
    private final Texture defeated = Risk.fillRectangle(296, 58, new Color(.7f, .7f, .7f, .7f));
    private final Texture highlighter;
    
    private final EnumMap<ArmyType, Texture> backgroundMap = new EnumMap<>(ArmyType.class);
    private final EnumMap<ArmyType, Animation<TextureRegion>> battalionMap = new EnumMap<>(ArmyType.class);
    private final EnumMap<ArmyType, Animation<TextureRegion>> leaderMap = new EnumMap<>(ArmyType.class);

    protected float time = 0;

    public Hud() {
        backgroundMap.put(ArmyType.RED, Risk.fillRectangle(296, 58, Color.SCARLET));
        backgroundMap.put(ArmyType.GREEN, Risk.fillRectangle(296, 58, Color.FOREST));
        backgroundMap.put(ArmyType.BLACK, Risk.fillRectangle(296, 58, Color.DARK_GRAY));
        backgroundMap.put(ArmyType.YELLOW, Risk.fillRectangle(296, 58, Color.GOLDENROD));

        battalionMap.put(ArmyType.RED, RED_BATTALION);
        battalionMap.put(ArmyType.GREEN, GREEN_BATTALION);
        battalionMap.put(ArmyType.BLACK, BLACK_BATTALION);
        battalionMap.put(ArmyType.YELLOW, YELLOW_BATTALION);

        leaderMap.put(ArmyType.RED, RED_LEADER);
        leaderMap.put(ArmyType.GREEN, GREEN_LEADER);
        leaderMap.put(ArmyType.BLACK, BLACK_LEADER);
        leaderMap.put(ArmyType.YELLOW, YELLOW_LEADER);

        Pixmap pix = new Pixmap(20, 19, Pixmap.Format.RGBA8888);
        pix.setColor(Color.BLUE);
        pix.fillRectangle(0, 6, 16, 8);
        pix.fillTriangle(10, 0, 19, 9, 10, 18);
        this.highlighter = new Texture(pix);
        pix.dispose();
    }

    public void render(Batch batch, Game game, float delta) {
        time += delta;

        int y = frame.getHeight() - 13;
        int py = frame.getHeight() - 55;

        batch.draw(this.frame, 0, 0);

        for (Army army : game.armies) {
            if (army == null) {
                continue;
            }

            ArmyType type = army.armyType;
            boolean isCurrent = (army == game.current());
            boolean isDefeated = army.battalions.isEmpty();

            Texture bg = isDefeated ? defeated : backgroundMap.get(type);

            batch.draw(bg, 2, y - 47);
            batch.draw(battalionMap.get(type).getKeyFrame(time, true), 0, py);
            batch.draw(leaderMap.get(type).getKeyFrame(time, true), 250, py);

            int idx = type.ordinal();
            Game.Status status = game.status[idx];

            String row1 = String.format("Cards: %d  Reg: %d  SH: %d", status.tcount, status.rcount, status.scount);
            String row2 = String.format("Cards Played: %d Score: %d", army.countAdventureCardsPlayed, status.score);

            Risk.defaultFont.draw(batch, row1, 75, y);
            Risk.defaultFont.draw(batch, row2, 75, y - 20);

            if (isCurrent) {
                batch.draw(highlighter, 50, y - 27);
            }

            y -= 62;
            py -= 62;
        }
    }
}
