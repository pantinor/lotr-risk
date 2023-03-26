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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import static lotr.Risk.FRODO;
import static lotr.Risk.SAM;

public class RingPathActor extends Actor {

    private final ShapeRenderer renderer;
    private final CatmullRomSpline<Vector2> path;
    public static final List<Risk.RingPathWrapper> RING_PATHS = new ArrayList<>();

    public AnimatedSpriteActor frodo, sam;

    public RingPathActor(Stage stage, ShapeRenderer renderer, MapLayer pathLayer) {
        this.renderer = renderer;

        Iterator<MapObject> pathIter = pathLayer.getObjects().iterator();
        while (pathIter.hasNext()) {
            RectangleMapObject obj = (RectangleMapObject) pathIter.next();
            Risk.RingPathWrapper w = new Risk.RingPathWrapper();
            w.name = obj.getName();
            w.id = obj.getProperties().get("id", Integer.class);
            w.x = obj.getProperties().get("x", Float.class);
            w.y = obj.getProperties().get("y", Float.class);
            RING_PATHS.add(w);
        }
        Collections.sort(RING_PATHS);
        RING_PATHS.get(0).selected = true;

        Vector2[] points = new Vector2[RING_PATHS.size() + 2];
        points[0] = new Vector2(RING_PATHS.get(0).x, RING_PATHS.get(0).y); //need to duplicate the first and last points
        for (int i = 0; i < RING_PATHS.size(); i++) {
            Risk.RingPathWrapper w = RING_PATHS.get(i);
            points[i + 1] = new Vector2(w.x, w.y);
            stage.addActor(new PathDotActor(w, w.x, w.y));
            if (w.selected) {
                frodo = new AnimatedSpriteActor(FRODO, w.x + 10, w.y + 10);
                sam = new AnimatedSpriteActor(SAM, w.x - 10, w.y - 10);
                stage.addActor(frodo);
                stage.addActor(sam);
            }
        }
        points[points.length - 1] = points[points.length - 2]; //need to duplicate the first and last points

        path = new CatmullRomSpline<>(points, false);

        stage.addActor(this);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        renderer.getRenderer().begin(batch.getProjectionMatrix(), GL20.GL_LINE_STRIP);
        Gdx.gl.glLineWidth(10);
        for (int i = 0; i < 100; ++i) {
            float t = i / 100f;
            Vector2 st = new Vector2();
            path.valueAt(st, t);
            renderer.getRenderer().color(new Color(0xd2b48c80));
            renderer.getRenderer().vertex(st.x, st.y, 0);
        }
        renderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private class PathDotActor extends Actor {

        private TextureRegion region1, region2;
        private Risk.RingPathWrapper wrapper;

        public PathDotActor(Risk.RingPathWrapper w, float x, float y) {
            this.region1 = Risk.skin.getRegion("circle-tan");
            this.region2 = Risk.skin.getRegion("circle-yellow");
            this.wrapper = w;
            this.setX(x - 6);
            this.setY(y - 6);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (wrapper.selected) {
                batch.draw(this.region2, getX(), getY());
            } else {
                batch.draw(this.region1, getX(), getY());
            }
        }

    }

    private class AnimatedSpriteActor extends Actor {

        private float stateTime;
        private final Animation<TextureRegion> anim;

        public AnimatedSpriteActor(Animation anim, float x, float y) {
            this.anim = anim;
            this.setX(x);
            this.setY(y);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            stateTime += Gdx.graphics.getDeltaTime();
            batch.draw(anim.getKeyFrame(stateTime, true), getX(), getY());
        }
    }

}
