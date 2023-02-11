package lotr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lotr.Constants.ArmyType;
import static lotr.Risk.CLASSPTH_RSLVR;
import lotr.Risk.RegionWrapper;

public class ClaimTerritoryScreen implements Screen {

    private final HexagonalTiledMapRenderer renderer;
    private final TiledMap tmxMap;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    TextureRegion redIcon, greyIcon, greenIcon, yellowIcon;
    Game game;
    float unitScale = 0.35f;
    List<RegionWrapper> regions = new ArrayList<>();

    private final SpriteBatch batch = new SpriteBatch();
    private final Viewport mapViewport;
    private final OrthographicCamera camera;

    private final Viewport viewport = new ScreenViewport();
    private final Stage stage = new Stage(viewport);

    private static final int MAP_VIEWPORT_WIDTH = 736;
    private static final int MAP_VIEWPORT_HEIGHT = 968;

    public ClaimTerritoryScreen(Game game) {

        this.game = game;
        
        this.camera = new OrthographicCamera(MAP_VIEWPORT_WIDTH, MAP_VIEWPORT_HEIGHT);
        this.mapViewport = new ScreenViewport(this.camera);
        this.camera.position.set(MAP_VIEWPORT_WIDTH / 2 - 200, MAP_VIEWPORT_HEIGHT / 2 - 15 , 0);
        this.mapViewport.update(MAP_VIEWPORT_WIDTH * 2, MAP_VIEWPORT_HEIGHT, false);

        TmxMapLoader loader = new TmxMapLoader(CLASSPTH_RSLVR);
        this.tmxMap = loader.load("assets/data/map.tmx");

        this.renderer = new HexagonalTiledMapRenderer(this.tmxMap, this.unitScale, this.batch);
        this.renderer.setView(this.camera);

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
            poly.setScale(unitScale, unitScale);
            poly.setPosition(poly.getX() * unitScale, poly.getY() * unitScale);
            w.vertices = poly.getTransformedVertices();
            w.name = name;

            regions.add(w);
        }

        TiledMapTileLayer iconLayer = (TiledMapTileLayer) this.tmxMap.getLayers().get("icons");
        Risk.setPoints(iconLayer, regions, unitScale);

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

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderer.render();

        Gdx.gl.glLineWidth(3);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

        for (RegionWrapper w : regions) {

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(255, 255, 0, .50f);//yellow
            shapeRenderer.polygon(w.vertices);
            shapeRenderer.end();

            if (w.territory != null && !w.territory.battalions.isEmpty()) {
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
            }

        }

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

}
