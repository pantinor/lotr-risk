package lotr;

import com.badlogic.gdx.graphics.Color;

public class Constants {

    public static enum ArmyType {
        RED(Color.RED), GREEN(Color.GREEN), BLACK(Color.GRAY), YELLOW(Color.GOLDENROD);
        private Color color;

        private ArmyType(Color c) {
            this.color = c;
        }

        public Color color() {
            return color;
        }

    }

    public static enum BattalionType {
        ELVEN_ARCHER, DARK_RIDER, EAGLE;
    }

    public static enum ClassType {
        GOOD, EVIL, NEUTRAL;
    }

}
