package lotr.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.utils.ShortArray;

public class RendererUtil {

    private static final EarClippingTriangulator EAR = new EarClippingTriangulator();

    public static void filledPolygon(ShapeRenderer sr, Color color, float[] vertices) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glLineWidth(1);
        sr.setAutoShapeType(true);
        sr.begin(ShapeType.Line);
        sr.set(ShapeType.Filled);
        sr.setColor(color);

        ShortArray arrRes = EAR.computeTriangles(vertices);

        for (int i = 0; i < arrRes.size - 2; i = i + 3) {
            float x1 = vertices[arrRes.get(i) * 2];
            float y1 = vertices[(arrRes.get(i) * 2) + 1];

            float x2 = vertices[(arrRes.get(i + 1)) * 2];
            float y2 = vertices[(arrRes.get(i + 1) * 2) + 1];

            float x3 = vertices[arrRes.get(i + 2) * 2];
            float y3 = vertices[(arrRes.get(i + 2) * 2) + 1];

            sr.triangle(x1, y1, x2, y2, x3, y3);
        }

        sr.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

}
