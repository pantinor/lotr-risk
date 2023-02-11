package lotr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.HexagonalTiledMapRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lotr.Constants.ArmyType;
import static lotr.Risk.CLASSPTH_RSLVR;
import lotr.Risk.RegionWrapper;
import static lotr.Risk.SCREEN_HEIGHT;
import static lotr.Risk.SCREEN_WIDTH;

public class GameScreen implements Screen, InputProcessor {

    private final Stage stage, mapStage;
    private final Batch batch, mapBatch;
    private final Risk main;

    private final HexagonalTiledMapRenderer renderer;
    private final OrthographicCamera camera;
    private final TiledMap tmxMap;
    public final OrthoCamController cameraController;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    private final Viewport mapViewport;
    private final Viewport viewport = new ScreenViewport();

    TextureRegion redIcon, greyIcon, greenIcon, yellowIcon;

    Game game;

    List<RegionWrapper> regions = new ArrayList<>();

    public GameScreen(Risk main, Game game) {

        this.main = main;

        this.stage = new Stage(viewport);
        this.batch = new SpriteBatch();

        TmxMapLoader loader = new TmxMapLoader(CLASSPTH_RSLVR);
        this.tmxMap = loader.load("assets/data/map.tmx");

        this.camera = new OrthographicCamera(SCREEN_WIDTH, SCREEN_HEIGHT);
        this.mapViewport = new ScreenViewport(this.camera);

        this.cameraController = new OrthoCamController(this.camera);

        this.renderer = new HexagonalTiledMapRenderer(this.tmxMap, 1f);

        this.mapBatch = this.renderer.getBatch();
        this.mapStage = new Stage(this.mapViewport, this.mapBatch);

        TiledMapTileSet tileset = this.tmxMap.getTileSets().getTileSet("monsters");
        int firstgid = tileset.getProperties().get("firstgid", Integer.class);

        redIcon = tileset.getTile(firstgid + 82).getTextureRegion();
        greyIcon = tileset.getTile(firstgid + 237).getTextureRegion();
        greenIcon = tileset.getTile(firstgid + 245).getTextureRegion();
        yellowIcon = tileset.getTile(firstgid + 1143).getTextureRegion();

        MapLayer regionsLayer = this.tmxMap.getLayers().get("regions");
        Iterator<MapObject> iter = regionsLayer.getObjects().iterator();
        while (iter.hasNext()) {
            PolygonMapObject obj = (PolygonMapObject) iter.next();
            Polygon poly = obj.getPolygon();
            String name = obj.getName();

            RegionWrapper w = new RegionWrapper();
            w.polygon = poly;
            w.vertices = poly.getTransformedVertices();
            w.name = name;

            regions.add(w);
        }

        TiledMapTileLayer iconLayer = (TiledMapTileLayer) this.tmxMap.getLayers().get("icons");
        Risk.setPoints(iconLayer, regions, 1f);

        TerritoryCard.init();

        this.game = game;

        for (RegionWrapper w : regions) {
            w.territory = TerritoryCard.getTerritory(w.name);
        }

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

        Gdx.gl.glLineWidth(5);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);

        for (RegionWrapper w : regions) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(255, 255, 0, .50f);//yellow
            shapeRenderer.polygon(w.vertices);
            shapeRenderer.end();

            if (w.territory != null && !w.territory.battalions.isEmpty()) {
                renderer.getBatch().begin();
                if (w.territory.battalions.get(0).army.armyType == ArmyType.RED) {
                    renderer.getBatch().draw(redIcon, w.redPosition.x, w.redPosition.y);
                    Risk.font.draw(renderer.getBatch(), w.territory.battalions.size() + "", w.redPosition.x, w.redPosition.y);
                }
                if (w.territory.battalions.get(0).army.armyType == ArmyType.GREY) {
                    renderer.getBatch().draw(greyIcon, w.greyPosition.x, w.greyPosition.y);
                    Risk.font.draw(renderer.getBatch(), w.territory.battalions.size() + "", w.greyPosition.x, w.greyPosition.y);
                }
                if (w.territory.battalions.get(0).army.armyType == ArmyType.GREEN) {
                    renderer.getBatch().draw(greenIcon, w.greenPosition.x, w.greenPosition.y);
                    Risk.font.draw(renderer.getBatch(), w.territory.battalions.size() + "", w.greenPosition.x, w.greenPosition.y);
                }
                if (w.territory.battalions.get(0).army.armyType == ArmyType.YELLOW) {
                    renderer.getBatch().draw(yellowIcon, w.yellowPosition.x, w.yellowPosition.y);
                    Risk.font.draw(renderer.getBatch(), w.territory.battalions.size() + "", w.yellowPosition.x, w.yellowPosition.y);
                }
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
