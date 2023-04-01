package lotr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import static lotr.Risk.FRODO;
import static lotr.Risk.SAM;
import lotr.util.Dice;
import lotr.util.Logger;
import lotr.util.RingPathAction;
import lotr.util.Sound;
import lotr.util.Sounds;

public class RingPathActor extends Actor implements RingPathAction {

    private final ShapeRenderer shapeRenderer;
    private final CatmullRomSpline<Vector2> path;
    private final Logger logger;
    private final AnimatedSpriteActor frodo, sam;
    private static final List<RingPathWrapper> RING_PATHS = new ArrayList<>();

    public RingPathActor(Stage stage, ShapeRenderer shapeRenderer, MapLayer pathLayer, Logger logger) {
        this.shapeRenderer = shapeRenderer;
        this.logger = logger;

        Iterator<MapObject> pathIter = pathLayer.getObjects().iterator();
        while (pathIter.hasNext()) {
            RectangleMapObject obj = (RectangleMapObject) pathIter.next();
            RingPathWrapper w = new RingPathWrapper();
            w.name = obj.getName();
            w.id = obj.getProperties().get("id", Integer.class);
            w.x = obj.getProperties().get("x", Float.class);
            w.y = obj.getProperties().get("y", Float.class);
            w.roll = obj.getProperties().get("roll") != null;
            RING_PATHS.add(w);
        }
        Collections.sort(RING_PATHS);
        RING_PATHS.get(0).current = true;

        Vector2[] points = new Vector2[RING_PATHS.size() + 2];
        points[0] = new Vector2(RING_PATHS.get(0).x, RING_PATHS.get(0).y); //need to duplicate the first and last points
        for (int i = 0; i < RING_PATHS.size(); i++) {
            RingPathWrapper w = RING_PATHS.get(i);
            points[i + 1] = new Vector2(w.x, w.y);
            stage.addActor(new PathDotActor(w, w.x, w.y));
        }
        points[points.length - 1] = points[points.length - 2]; //need to duplicate the first and last points

        path = new CatmullRomSpline<>(points, false);

        frodo = new AnimatedSpriteActor(FRODO);
        sam = new AnimatedSpriteActor(SAM);

        frodo.setPosition(RING_PATHS.get(0).x + 10, RING_PATHS.get(0).y + 10);
        sam.setPosition(RING_PATHS.get(0).x - 10, RING_PATHS.get(0).y - 10);

        stage.addActor(frodo);
        stage.addActor(sam);
    }

    @Override
    public boolean advance() {
        for (int i = 0; i < RING_PATHS.size(); i++) {
            RingPathWrapper w = RING_PATHS.get(i);
            if (w.current) {
                if (w.roll) {
                    Dice d = new Dice();
                    int r = d.roll();
                    logger.log("The FELLOWSHIP rolls a " + r + " at " + w.name.toUpperCase(), Color.YELLOW);
                    if (r > 3) {
                        //advance
                        Sounds.play(Sound.POSITIVE_EFFECT);
                    } else {
                        Sounds.play(Sound.NEGATIVE_EFFECT);
                        return false;
                    }
                }
                w.current = false;
                if (i < RING_PATHS.size() - 1) {
                    RING_PATHS.get(i + 1).current = true;
                    frodo.addAction(moveTo(RING_PATHS.get(i + 1).x + 10, RING_PATHS.get(i + 1).y + 10, 6));
                    sam.addAction(moveTo(RING_PATHS.get(i + 1).x - 10, RING_PATHS.get(i + 1).y - 10, 6));
                    logger.log("The FELLOWSHIP advances to " + RING_PATHS.get(i + 1).name.toUpperCase(), Color.YELLOW);
                } else {
                    logger.log("Sam and Frodo have thrown the ONE RING into MOUNT DOOM!", Color.YELLOW);
                    return true;
                }
                break;
            }
        }
        return false;
    }

    public void render() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.getRenderer().begin(shapeRenderer.getProjectionMatrix(), GL20.GL_LINE_STRIP);
        Gdx.gl.glLineWidth(10);
        for (int i = 0; i < 100; ++i) {
            float t = i / 100f;
            Vector2 st = new Vector2();
            path.valueAt(st, t);
            shapeRenderer.getRenderer().color(new Color(0xd2b48c80));
            shapeRenderer.getRenderer().vertex(st.x, st.y, 0);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public static class RingPathWrapper implements Comparable {

        public int id;
        public String name;
        public float x;
        public float y;
        public boolean roll;
        public boolean current;

        @Override
        public int compareTo(Object obj) {
            final RingPathWrapper other = (RingPathWrapper) obj;
            if (this.id > other.id) {
                return 1;
            } else if (this.id < other.id) {
                return -1;
            } else {
                return 0;
            }
        }

    }

    private class PathDotActor extends Actor {

        private TextureRegion r1, r2, r3;
        private RingPathWrapper wrapper;

        public PathDotActor(RingPathWrapper w, float x, float y) {
            this.r1 = Risk.skin.getRegion("circle-tan");
            this.r2 = Risk.skin.getRegion("circle-yellow");
            this.r3 = Risk.skin.getRegion("circle-blue");
            this.wrapper = w;
            this.setX(x - 6);
            this.setY(y - 6);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (wrapper.current) {
                batch.draw(this.r2, getX(), getY());
            } else if (wrapper.roll) {
                batch.draw(this.r3, getX(), getY());
            } else {
                batch.draw(this.r1, getX(), getY());
            }
        }

    }

    private class AnimatedSpriteActor extends Actor {

        private float stateTime;
        private final Animation<TextureRegion> anim;

        public AnimatedSpriteActor(Animation anim) {
            this.anim = anim;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            stateTime += Gdx.graphics.getDeltaTime();
            batch.draw(anim.getKeyFrame(stateTime, true), getX(), getY());
        }
    }

}
