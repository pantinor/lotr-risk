package lotr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.HexagonalTiledMapRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lotr.Constants.ArmyType;
import static lotr.Risk.FRODO;
import static lotr.Risk.GREEN_BATTALION;
import static lotr.Risk.GREEN_LEADER;
import static lotr.Risk.SCREEN_HEIGHT;
import static lotr.Risk.SCREEN_WIDTH;
import static lotr.Risk.BLACK_BATTALION;
import static lotr.Risk.BLACK_LEADER;
import static lotr.Risk.RED_BATTALION;
import static lotr.Risk.RED_LEADER;
import static lotr.Risk.RING_PATHS;
import static lotr.Risk.SAM;
import static lotr.Risk.TMX_MAP;
import static lotr.Risk.YELLOW_BATTALION;
import static lotr.Risk.YELLOW_LEADER;
import static lotr.Risk.RED_CIRCLE;
import static lotr.Risk.GREEN_CIRCLE;
import static lotr.Risk.BLACK_CIRCLE;
import static lotr.Risk.YELLOW_CIRCLE;
import static lotr.Risk.LEADER_CIRCLE;
import lotr.Risk.RegionWrapper;
import lotr.Risk.RingPathWrapper;

public class GameScreen implements Screen, InputProcessor {

    private final Stage stage, mapStage;
    private final Batch batch, mapBatch;

    private final HexagonalTiledMapRenderer renderer;
    private final OrthographicCamera camera;
    //public final OrthoCamController cameraController;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    private final Viewport mapViewport;
    private final Viewport viewport = new ScreenViewport();

    private final Game game;

    List<RegionWrapper> regions = new ArrayList<>();

    public GameScreen(Game game) {

        this.stage = new Stage(viewport);
        this.batch = new SpriteBatch();

        this.camera = new OrthographicCamera(SCREEN_WIDTH, SCREEN_HEIGHT);
        this.mapViewport = new ScreenViewport(this.camera);

        //this.cameraController = new OrthoCamController(this.camera);
        this.renderer = new HexagonalTiledMapRenderer(TMX_MAP, 1f);

        this.mapBatch = this.renderer.getBatch();
        this.mapStage = new Stage(this.mapViewport, this.mapBatch);

        MapLayer regionsLayer = TMX_MAP.getLayers().get("regions");
        Iterator<MapObject> iter = regionsLayer.getObjects().iterator();
        while (iter.hasNext()) {
            PolygonMapObject obj = (PolygonMapObject) iter.next();
            Polygon poly = obj.getPolygon();
            String name = obj.getName();

            RegionWrapper w = new RegionWrapper();
            w.polygon = poly;
            w.vertices = poly.getTransformedVertices();
            w.name = name;
            w.territory = TerritoryCard.getTerritory(name);

            regions.add(w);
        }

        TiledMapTileLayer iconLayer = (TiledMapTileLayer) TMX_MAP.getLayers().get("icons");
        Risk.setPoints(iconLayer, regions, 1f);

        this.game = game;

        Gdx.input.setInputProcessor(new InputAdapter() {

            Vector3 curr = new Vector3();
            Vector3 last = new Vector3(-1, -1, -1);
            Vector3 delta = new Vector3();

            @Override
            public boolean touchDragged(int x, int y, int pointer) {
                camera.unproject(curr.set(x, y, 0));
                if (!(last.x == -1 && last.y == -1 && last.z == -1)) {
                    camera.unproject(delta.set(last.x, last.y, 0));
                    delta.sub(curr);
                    camera.position.add(delta.x, delta.y, 0);
                }
                last.set(x, y, 0);
                return false;
            }

            @Override
            public boolean touchUp(int x, int y, int pointer, int button) {
                last.set(-1, -1, -1);

                Vector3 tmp = camera.unproject(new Vector3(x, y, 0));
                Vector2 v = new Vector2(tmp.x, tmp.y - 0);
                for (RegionWrapper w : regions) {
                    if (w.polygon.contains(v)) {
                        w.selected = true;
                    } else {
                        w.selected = false;
                    }
                }

                return false;
            }
        });

    }

    @Override
    public void show() {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        renderer.setView(camera);

        renderer.render();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);

        for (RegionWrapper w : regions) {

            Gdx.gl.glLineWidth(5);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.LIGHT_GRAY);
            shapeRenderer.polygon(w.vertices);
            shapeRenderer.end();

            if (w.territory != null) {
                renderer.getBatch().begin();

                ArmyType at = game.getOccupyingArmy(w.territory);

                if (at == ArmyType.RED) {
                    renderer.getBatch().draw(RED_BATTALION, w.battalionPosition.x, w.battalionPosition.y);
                    if (w.territory == game.red.leader1.territory || w.territory == game.red.leader2.territory) {
                        renderer.getBatch().draw(RED_LEADER, w.battalionPosition.x - 10, w.battalionPosition.y - 10);
                        renderer.getBatch().draw(LEADER_CIRCLE, w.textPosition.x - 6, w.textPosition.y - 10);
                    }
                    renderer.getBatch().draw(RED_CIRCLE, w.textPosition.x - 6, w.textPosition.y - 10);
                }
                if (at == ArmyType.BLACK) {
                    renderer.getBatch().draw(BLACK_BATTALION, w.battalionPosition.x, w.battalionPosition.y);
                    if (w.territory == game.black.leader1.territory || w.territory == game.black.leader2.territory) {
                        renderer.getBatch().draw(BLACK_LEADER, w.battalionPosition.x - 10, w.battalionPosition.y - 10);
                        renderer.getBatch().draw(LEADER_CIRCLE, w.textPosition.x - 6, w.textPosition.y - 10);
                    }
                    renderer.getBatch().draw(BLACK_CIRCLE, w.textPosition.x - 6, w.textPosition.y - 10);
                }
                if (at == ArmyType.GREEN) {
                    renderer.getBatch().draw(GREEN_BATTALION, w.battalionPosition.x, w.battalionPosition.y);
                    if (w.territory == game.green.leader1.territory || w.territory == game.green.leader2.territory) {
                        renderer.getBatch().draw(GREEN_LEADER, w.battalionPosition.x - 10, w.battalionPosition.y - 10);
                        renderer.getBatch().draw(LEADER_CIRCLE, w.textPosition.x - 6, w.textPosition.y - 10);
                    }
                    renderer.getBatch().draw(GREEN_CIRCLE, w.textPosition.x - 6, w.textPosition.y - 10);
                }
                if (at == ArmyType.YELLOW) {
                    renderer.getBatch().draw(YELLOW_BATTALION, w.battalionPosition.x, w.battalionPosition.y);
                    if (w.territory == game.yellow.leader1.territory || w.territory == game.yellow.leader2.territory) {
                        renderer.getBatch().draw(YELLOW_LEADER, w.battalionPosition.x - 10, w.battalionPosition.y - 10);
                        renderer.getBatch().draw(LEADER_CIRCLE, w.textPosition.x - 6, w.textPosition.y - 10);
                    }
                    renderer.getBatch().draw(YELLOW_CIRCLE, w.textPosition.x - 6, w.textPosition.y - 10);
                }

                int bc = game.battalionCount(w.territory);
                if (bc > 0) {
                    Risk.fontSmall.draw(renderer.getBatch(), bc + "", w.textPosition.x + 3, w.textPosition.y + 8);
                }

                renderer.getBatch().end();
            }

        }

        for (RegionWrapper w : regions) {
            if (w.selected) {
                Gdx.gl.glLineWidth(8);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(Color.RED);
                shapeRenderer.polygon(w.vertices);
                shapeRenderer.end();
            }
        }

        for (RingPathWrapper rw : RING_PATHS) {
            if (rw.selected) {
                renderer.getBatch().begin();
                renderer.getBatch().draw(FRODO, rw.x + 10, rw.y + 10);
                renderer.getBatch().draw(SAM, rw.x - 10, rw.y - 10);
                renderer.getBatch().end();
            }
        }

        stage.act();
        stage.draw();

    }

    @Override
    public boolean keyUp(int keycode) {

        if (keycode == Input.Keys.TAB) {

        }

        return false;
    }

    @Override
    public boolean keyTyped(char c) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        return false;
    }

    @Override
    public boolean touchUp(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchDragged(int i, int i1, int i2) {
        return false;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {
        return false;
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean keyDown(int i) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

}
