package lotr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
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
    private final Texture highlighter = Risk.fillRectangle(296, 58, new Color(.7f, .3f, .5f, .7f));
    private final Texture defeated = Risk.fillRectangle(296, 58, new Color(.7f, .7f, .7f, .7f));

    private final EnumMap<ArmyType, Texture> backgroundMap = new EnumMap<>(ArmyType.class);
    private final EnumMap<ArmyType, Animation<TextureRegion>> battalionMap = new EnumMap<>(ArmyType.class);
    private final EnumMap<ArmyType, Animation<TextureRegion>> leaderMap = new EnumMap<>(ArmyType.class);

    protected float time = 0;

    public Hud() {
        backgroundMap.put(ArmyType.RED, Risk.fillRectangle(296, 58, new Color(Color.RED.r, Color.RED.g, Color.RED.b, .7f)));
        backgroundMap.put(ArmyType.GREEN, Risk.fillRectangle(296, 58, new Color(Color.GREEN.r, Color.GREEN.g, Color.GREEN.b, .7f)));
        backgroundMap.put(ArmyType.BLACK, Risk.fillRectangle(296, 58, new Color(Color.DARK_GRAY.r, Color.DARK_GRAY.g, Color.DARK_GRAY.b, .7f)));
        backgroundMap.put(ArmyType.YELLOW, Risk.fillRectangle(296, 58, new Color(Color.GOLDENROD.r, Color.GOLDENROD.g, Color.GOLDENROD.b, .7f)));

        battalionMap.put(ArmyType.RED, RED_BATTALION);
        battalionMap.put(ArmyType.GREEN, GREEN_BATTALION);
        battalionMap.put(ArmyType.BLACK, BLACK_BATTALION);
        battalionMap.put(ArmyType.YELLOW, YELLOW_BATTALION);

        leaderMap.put(ArmyType.RED, RED_LEADER);
        leaderMap.put(ArmyType.GREEN, GREEN_LEADER);
        leaderMap.put(ArmyType.BLACK, BLACK_LEADER);
        leaderMap.put(ArmyType.YELLOW, YELLOW_LEADER);
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

            Texture bg = isCurrent ? highlighter : (isDefeated ? defeated : backgroundMap.get(type));

            batch.draw(bg, 2, y - 47);
            batch.draw(battalionMap.get(type).getKeyFrame(time, true), 0, py);
            batch.draw(leaderMap.get(type).getKeyFrame(time, true), 250, py);

            int idx = type.ordinal();
            Game.Status status = game.status[idx];

            String row1 = String.format("B: %d  T: %d  R: %d  S: %d", status.bcount, status.tcount, status.rcount, status.scount);
            String row2 = String.format("Cards: %d  Threat: %d", status.ccount, status.threat);

            Risk.defaultFont.draw(batch, row1, 50, y);
            Risk.defaultFont.draw(batch, row2, 50, y - 20);

            y -= 62;
            py -= 62;
        }
    }
}
