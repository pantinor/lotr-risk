package lotr;

import com.badlogic.gdx.graphics.Color;

public enum Region {

    ARNOR(Color.RED),
    GONDOR(Color.BROWN),
    RHOVANION(Color.ORANGE),
    ROHAN(Color.BLUE),
    ERIADOR(Color.YELLOW),
    MORDOR(Color.GRAY),
    HARDAWAITH(Color.TAN),
    MIRKWOOD(Color.GREEN),
    RHUN(Color.PURPLE);

    private Color color;

    private Region(Color color) {
        this.color = color;
    }

    public Color color() {
        return color;
    }

}
