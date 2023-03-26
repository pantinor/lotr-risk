package lotr.util;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import lotr.Location;
import lotr.Risk;

public class LocationActor extends Actor {

    private final TextureRegion tr;
    private final Location location;
    private final String label;

    public LocationActor(TextureRegion tr, Location location, float x, float y) {
        this.tr = tr;
        this.location = location;
        this.label = location.capitalized();
        this.setX(x);
        this.setY(y);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(tr, getX(), getY());
        if (Risk.textToggle) {
            Risk.font.draw(batch, this.label, getX() - 20, getY() + 43);
        }
    }
}
