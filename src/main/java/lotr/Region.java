package lotr;

import com.badlogic.gdx.graphics.Color;
import java.util.ArrayList;
import java.util.List;

public enum Region {

    ARNOR(Color.RED, 7),
    GONDOR(Color.BROWN, 7),
    RHOVANION(Color.ORANGE, 5),
    ROHAN(Color.BLUE, 4),
    ERIADOR(Color.YELLOW, 3),
    MORDOR(Color.GRAY, 2),
    HARADAWAITH(Color.TAN, 2),
    MIRKWOOD(Color.GREEN, 4),
    RHUN(Color.PURPLE, 2);

    private Color color;
    private final List<TerritoryCard> territories = new ArrayList<>();
    private int reinforcements;

    private Region(Color color, int r) {
        this.color = color;
        this.reinforcements = r;
    }

    public Color color() {
        return color;
    }

    public List<TerritoryCard> territories() {
        return territories;
    }

    public int reinforcements() {
        return reinforcements;
    }

}
