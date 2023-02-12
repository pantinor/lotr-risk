package lotr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import static lotr.Risk.GREY_BATTALION;
import static lotr.Risk.GREY_LEADER;
import static lotr.Risk.LEADER_CIRCLE;
import static lotr.Risk.RED_BATTALION;
import static lotr.Risk.RED_LEADER;
import static lotr.Risk.RING_PATHS;
import lotr.Risk.RegionWrapper;
import lotr.Risk.RingPathWrapper;
import static lotr.Risk.SAM;
import static lotr.Risk.TEXT_CIRCLE;
import static lotr.Risk.TMX_MAP;
import static lotr.Risk.YELLOW_BATTALION;
import static lotr.Risk.YELLOW_LEADER;

public class GameScreen implements Screen, InputProcessor {

    private final Stage stage, mapStage;
    private final Batch batch, mapBatch;

    private final HexagonalTiledMapRenderer renderer;
    private final OrthographicCamera camera;
    public final OrthoCamController cameraController;
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

        this.cameraController = new OrthoCamController(this.camera);

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

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this.cameraController);
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
            Gdx.gl.glLineWidth(w.selected ? 8 : 5);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(w.selected ? Color.RED : Color.LIGHT_GRAY);
            shapeRenderer.polygon(w.vertices);
            shapeRenderer.end();

            if (w.territory != null && !w.territory.battalions.isEmpty()) {
                renderer.getBatch().begin();

                if (w.territory.battalions.get(0).army.armyType == ArmyType.RED) {
                    renderer.getBatch().draw(RED_BATTALION, w.redPosition.x, w.redPosition.y);
                    if (w.territory.leader != null) {
                        renderer.getBatch().draw(RED_LEADER, w.redPosition.x - 10, w.redPosition.y - 10);
                    }
                }
                if (w.territory.battalions.get(0).army.armyType == ArmyType.GREY) {
                    renderer.getBatch().draw(GREY_BATTALION, w.greyPosition.x, w.greyPosition.y);
                    if (w.territory.leader != null) {
                        renderer.getBatch().draw(GREY_LEADER, w.greyPosition.x - 10, w.greyPosition.y - 10);
                    }
                }
                if (w.territory.battalions.get(0).army.armyType == ArmyType.GREEN) {
                    renderer.getBatch().draw(GREEN_BATTALION, w.greenPosition.x, w.greenPosition.y);
                    if (w.territory.leader != null) {
                        renderer.getBatch().draw(GREEN_LEADER, w.greenPosition.x - 10, w.greenPosition.y - 10);
                    }
                }
                if (w.territory.battalions.get(0).army.armyType == ArmyType.YELLOW) {
                    renderer.getBatch().draw(YELLOW_BATTALION, w.yellowPosition.x, w.yellowPosition.y);
                    if (w.territory.leader != null) {
                        renderer.getBatch().draw(YELLOW_LEADER, w.yellowPosition.x - 10, w.yellowPosition.y - 10);
                    }
                }

                renderer.getBatch().draw(w.territory.leader != null ? LEADER_CIRCLE : TEXT_CIRCLE, w.textPosition.x - 6, w.textPosition.y - 10);
                Risk.fontSmall.draw(renderer.getBatch(), w.territory.battalions.size() + "", w.textPosition.x + 3, w.textPosition.y + 8);

                renderer.getBatch().end();
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
