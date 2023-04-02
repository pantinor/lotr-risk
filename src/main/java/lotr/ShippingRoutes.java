package lotr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Vector2;
import java.util.Iterator;
import static lotr.Risk.TMX_MAP;

public class ShippingRoutes {

    private final ShapeRenderer shapeRenderer;
    private final CatmullRomSpline<Vector2> path1;
    private final CatmullRomSpline<Vector2> path2;
    private final CatmullRomSpline<Vector2> path3;
    private final CatmullRomSpline<Vector2> path4;

    public ShippingRoutes(ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;

        Vector2[] points1 = new Vector2[8 + 2];
        Vector2[] points2 = new Vector2[9 + 2];
        Vector2[] points3 = new Vector2[7 + 2];
        Vector2[] points4 = new Vector2[7 + 2];

        int idx = 1;
        Iterator<MapObject> pathIter = TMX_MAP.getLayers().get("ship-route-1").getObjects().iterator();
        while (pathIter.hasNext()) {
            RectangleMapObject obj = (RectangleMapObject) pathIter.next();
            float x = obj.getProperties().get("x", Float.class);
            float y = obj.getProperties().get("y", Float.class);
            points1[idx] = new Vector2(x, y);
            idx++;
        }
        points1[0] = new Vector2(points1[1]); //need to duplicate the first and last points
        points1[points1.length - 1] = points1[points1.length - 2]; //need to duplicate the first and last points

        idx = 1;
        pathIter = TMX_MAP.getLayers().get("ship-route-2").getObjects().iterator();
        while (pathIter.hasNext()) {
            RectangleMapObject obj = (RectangleMapObject) pathIter.next();
            float x = obj.getProperties().get("x", Float.class);
            float y = obj.getProperties().get("y", Float.class);
            points2[idx] = new Vector2(x, y);
            idx++;
        }
        points2[0] = new Vector2(points2[1]); //need to duplicate the first and last points
        points2[points2.length - 1] = points2[points2.length - 2]; //need to duplicate the first and last points

        idx = 1;
        pathIter = TMX_MAP.getLayers().get("ship-route-3").getObjects().iterator();
        while (pathIter.hasNext()) {
            RectangleMapObject obj = (RectangleMapObject) pathIter.next();
            float x = obj.getProperties().get("x", Float.class);
            float y = obj.getProperties().get("y", Float.class);
            points3[idx] = new Vector2(x, y);
            idx++;
        }
        points3[0] = new Vector2(points3[1]); //need to duplicate the first and last points
        points3[points3.length - 1] = points3[points3.length - 2]; //need to duplicate the first and last points

        idx = 1;
        pathIter = TMX_MAP.getLayers().get("ship-route-4").getObjects().iterator();
        while (pathIter.hasNext()) {
            RectangleMapObject obj = (RectangleMapObject) pathIter.next();
            float x = obj.getProperties().get("x", Float.class);
            float y = obj.getProperties().get("y", Float.class);
            points4[idx] = new Vector2(x, y);
            idx++;
        }
        points4[0] = new Vector2(points4[1]); //need to duplicate the first and last points
        points4[points4.length - 1] = points4[points4.length - 2]; //need to duplicate the first and last points

        path1 = new CatmullRomSpline<>(points1, false);
        path2 = new CatmullRomSpline<>(points2, false);
        path3 = new CatmullRomSpline<>(points3, false);
        path4 = new CatmullRomSpline<>(points4, false);

    }

    public void render() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glLineWidth(8);

        shapeRenderer.getRenderer().begin(shapeRenderer.getProjectionMatrix(), GL20.GL_LINE_STRIP);
        for (int i = 0; i < 100; ++i) {
            float t = i / 100f;
            Vector2 st = new Vector2();
            path1.valueAt(st, t);
            shapeRenderer.getRenderer().color(Color.NAVY);
            shapeRenderer.getRenderer().vertex(st.x, st.y, 0);
        }
        shapeRenderer.end();

        shapeRenderer.getRenderer().begin(shapeRenderer.getProjectionMatrix(), GL20.GL_LINE_STRIP);
        for (int i = 0; i < 100; ++i) {
            float t = i / 100f;
            Vector2 st = new Vector2();
            path2.valueAt(st, t);
            shapeRenderer.getRenderer().color(Color.NAVY);
            shapeRenderer.getRenderer().vertex(st.x, st.y, 0);
        }
        shapeRenderer.end();

        shapeRenderer.getRenderer().begin(shapeRenderer.getProjectionMatrix(), GL20.GL_LINE_STRIP);
        for (int i = 0; i < 100; ++i) {
            float t = i / 100f;
            Vector2 st = new Vector2();
            path3.valueAt(st, t);
            shapeRenderer.getRenderer().color(Color.NAVY);
            shapeRenderer.getRenderer().vertex(st.x, st.y, 0);
        }
        shapeRenderer.end();

        shapeRenderer.getRenderer().begin(shapeRenderer.getProjectionMatrix(), GL20.GL_LINE_STRIP);
        for (int i = 0; i < 100; ++i) {
            float t = i / 100f;
            Vector2 st = new Vector2();
            path4.valueAt(st, t);
            shapeRenderer.getRenderer().color(Color.NAVY);
            shapeRenderer.getRenderer().vertex(st.x, st.y, 0);
        }
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

}
