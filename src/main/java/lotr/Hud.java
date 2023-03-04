package lotr;

import java.util.List;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Align;
import static lotr.Risk.GREEN_BATTALION;
import static lotr.Risk.BLACK_BATTALION;
import static lotr.Risk.RED_BATTALION;
import static lotr.Risk.YELLOW_BATTALION;

public class Hud {

    final List<String> logs = new FixedSizeArrayList<>(25);

    static final int LOG_AREA_WIDTH = 270;
    static final int LOG_AREA_TOP = 355;
    static final int LOG_X = 735;
    private final GlyphLayout layout = new GlyphLayout(Risk.font, "", Color.WHITE, LOG_AREA_WIDTH - 5, Align.left, true);
    private final Texture background = Risk.fillRectangle(280, 250, Color.GRAY, .5f);

    public void append(String s) {
        synchronized (logs) {
            if (logs.isEmpty()) {
                logs.add("");
            }
            String l = logs.get(logs.size() - 1);
            l = l + s;
            logs.remove(logs.size() - 1);
            logs.add(l);
        }
    }

    public void logDeleteLastChar() {
        synchronized (logs) {
            if (logs.isEmpty()) {
                return;
            }
            String l = logs.get(logs.size() - 1);
            l = l.substring(0, l.length() - 1);
            logs.remove(logs.size() - 1);
            logs.add(l);
        }
    }

    public void add(String s) {
        synchronized (logs) {
            logs.add(s);
        }
    }

    public void render(Batch batch, Game game) {

        int y = Risk.SCREEN_HEIGHT - 830;
        int py = Risk.SCREEN_HEIGHT - 875;

        batch.draw(this.background, 0, 0);

        for (Army a : game.armies) {

            if (a == null) {
                continue;
            }

            List<TerritoryCard> claimedTerritories = a.claimedTerritories();

            switch (a.armyType) {
                case RED:
                    Risk.fontSmall.setColor(Color.RED);
                    batch.draw(RED_BATTALION.getKeyFrame(0), 0, py);
                    break;
                case GREEN:
                    Risk.fontSmall.setColor(Color.GREEN);
                    batch.draw(GREEN_BATTALION.getKeyFrame(0), 0, py);
                    break;
                case BLACK:
                    Risk.fontSmall.setColor(Color.WHITE);
                    batch.draw(BLACK_BATTALION.getKeyFrame(0), 0, py);
                    break;
                case YELLOW:
                    Risk.fontSmall.setColor(Color.YELLOW);
                    batch.draw(YELLOW_BATTALION.getKeyFrame(0), 0, py);
                    break;
            }

            String row1 = String.format("%s  Battalions %d Territories %d", a.armyType, a.battalions.size(), claimedTerritories.size());
            String row2 = String.format("Regions %s  Strongholds %d", a.ownedRegions(claimedTerritories).size(), a.ownedStrongholds(claimedTerritories).size());

            Risk.fontSmall.draw(batch, row1, 50, y);
            Risk.fontSmall.draw(batch, row2, 50, y - 17);

            y -= 55;
            py -= 55;

        }

        Risk.font.setColor(Color.WHITE);
        Risk.fontSmall.setColor(Color.WHITE);

        y = 28;

        synchronized (logs) {
            ReverseListIterator iter = new ReverseListIterator(logs);
            while (iter.hasNext()) {
                String next = (String) iter.next();
                layout.setText(Risk.font, next);
                y += layout.height + 4;
                if (y > LOG_AREA_TOP) {
                    break;
                }
                Risk.font.draw(batch, layout, LOG_X, y);
            }
        }
    }
}
