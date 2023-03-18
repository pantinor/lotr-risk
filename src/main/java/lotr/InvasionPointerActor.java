package lotr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;

public class InvasionPointerActor extends Actor {

    private final ShapeRenderer renderer;
    Vector3 start, end;

    public InvasionPointerActor(ShapeRenderer renderer, Vector3 start, Vector3 end) {
        this.renderer = renderer;

        double l = Math.sqrt(Math.pow((start.x - end.x), 2) + Math.pow((start.y - end.y), 2));
        double d = l * 0.90;//start at a point 10% away along the line
        
        float newSX = (float) (end.x + (((start.x - end.x) / (l) * d)));
        float newSY = (float) (end.y + (((start.y - end.y) / (l) * d)));
        this.start = start;
        this.start.set(newSX, newSY, 0);

        this.end = end;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {

        renderer.begin(ShapeType.Line);
        Gdx.gl.glLineWidth(12);
        renderer.setColor(Color.BLACK);
        renderer.line(start.x, start.y, end.x, end.y);
        renderer.end();

        renderer.begin(ShapeType.Line);
        Gdx.gl.glLineWidth(6);
        renderer.setColor(Color.WHITE);
        renderer.line(start.x, start.y, end.x, end.y);
        renderer.end();
    }

}
