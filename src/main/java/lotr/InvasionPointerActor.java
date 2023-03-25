package lotr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class InvasionPointerActor extends Actor {

    private final ShapeRenderer renderer;
    Vector3 start, end;
    float angle;

    public InvasionPointerActor(ShapeRenderer renderer, Vector3 start, Vector3 end) {
        this.renderer = renderer;

        double l = Math.sqrt(Math.pow((start.x - end.x), 2) + Math.pow((start.y - end.y), 2));
        double d1 = l * 0.90;//start at a point 10% away along the line

        float newSX = (float) (end.x + (((start.x - end.x) / (l) * d1)));
        float newSY = (float) (end.y + (((start.y - end.y) / (l) * d1)));
        this.start = start;
        this.start.set(newSX, newSY, 0);

        double d2 = l * 0.20;//end at a point 80% away along the line
        float newEX = (float) (end.x + (((start.x - end.x) / (l) * d2)));
        float newEY = (float) (end.y + (((start.y - end.y) / (l) * d2)));
        this.end = end;
        this.end.set(newEX, newEY, 0);

        this.angle = MathUtils.atan2(this.end.y - this.start.y, this.end.x - this.start.x);
        this.angle *= MathUtils.radiansToDegrees;
        this.angle -= 90;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {

        renderer.begin(ShapeType.Line);
        Gdx.gl.glLineWidth(12);
        renderer.setColor(Color.BLACK);
        renderer.line(start.x, start.y, end.x, end.y);
        renderer.end();

        renderer.begin(ShapeType.Filled);
        renderer.setColor(Color.BLACK);
        renderer.identity();
        renderer.translate(end.x, end.y, 0);
        renderer.rotate(0, 0, 1, angle);
        renderer.triangle(0, 16, 16, -16, -16, -16);
        renderer.rotate(0, 0, 1, -angle);
        renderer.translate(-end.x, -end.y, 0);
        renderer.end();

        renderer.begin(ShapeType.Line);
        Gdx.gl.glLineWidth(6);
        renderer.setColor(Color.WHITE);
        renderer.line(start.x, start.y, end.x, end.y);
        renderer.end();

        renderer.begin(ShapeType.Filled);
        renderer.setColor(Color.WHITE);
        renderer.identity();
        renderer.translate(end.x, end.y, 0);
        renderer.rotate(0, 0, 1, angle);
        renderer.triangle(0, 10, 10, -10, -10, -10);
        renderer.rotate(0, 0, 1, -angle);
        renderer.translate(-end.x, -end.y, 0);
        renderer.end();

    }

}
