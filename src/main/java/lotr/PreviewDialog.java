package lotr;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lotr.Constants.ArmyType;
import lotr.Risk.RegionWrapper;
import static lotr.Risk.TMX_MAP;
import static lotr.util.RendererUtil.filledPolygonWithOutline;

public class PreviewDialog extends Dialog {

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final InputProcessor input;

    private final float unitScale = 0.35f;
    private final List<RegionWrapper> regions = new ArrayList<>();

    private final OrthographicCamera camera = new OrthographicCamera();
    private final FrameBuffer fbo;
    private final TextureRegion staticMapTexture;

    public PreviewDialog(GameScreen screen, Game game) {
        super("", Risk.skin.get("dialog", Window.WindowStyle.class));
        this.input = Gdx.input.getInputProcessor();

        MapLayer regionsLayer = TMX_MAP.getLayers().get("regions");
        Iterator<MapObject> iter = regionsLayer.getObjects().iterator();
        while (iter.hasNext()) {
            PolygonMapObject obj = (PolygonMapObject) iter.next();

            Polygon poly = new Polygon(obj.getPolygon().getVertices());
            poly.setPosition(obj.getPolygon().getX(), obj.getPolygon().getY());
            poly.setOrigin(obj.getPolygon().getOriginX(), obj.getPolygon().getOriginY());

            String name = obj.getName();

            Risk.FortifyRegionWrapper frw = new Risk.FortifyRegionWrapper();

            frw.polygon = poly;
            poly.setScale(unitScale, unitScale);
            poly.setPosition(poly.getX() * unitScale, poly.getY() * unitScale);

            frw.vertices = poly.getTransformedVertices();
            frw.name = name;
            frw.territory = TerritoryCard.getTerritory(name);
            regions.add(frw);
        }

        TiledMapTileLayer layer = (TiledMapTileLayer) TMX_MAP.getLayers().get("icons");
        Risk.setPoints(layer, regions, unitScale);

        float w = layer.getWidth() * layer.getTileWidth() * unitScale;
        float h = layer.getHeight() * layer.getTileHeight() * unitScale;

        camera.setToOrtho(false, w, h);
        camera.zoom = 1f / unitScale;

        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, (int) w, (int) h, false);

        fbo.begin();

        shapeRenderer.setProjectionMatrix(camera.combined);
        for (RegionWrapper wr : regions) {
            ArmyType at = game.getOccupyingArmy(wr.territory).armyType;
            filledPolygonWithOutline(shapeRenderer, at.color(), wr.vertices);
        }

        SpriteBatch fboBatch = new SpriteBatch();
        fboBatch.setProjectionMatrix(camera.combined);
        fboBatch.begin();
        for (RegionWrapper wr : regions) {
            for (Army a : game.armies) {
                if (a != null) {
                    if (wr.territory == a.leader1.territory || wr.territory == a.leader2.territory) {
                        fboBatch.draw(Risk.LEADER_CIRCLE, wr.battalionPosition.x - 5, wr.battalionPosition.y - 15);
                    }
                }
            }
            fboBatch.draw(Risk.DISPLAY_CIRCLE, wr.battalionPosition.x, wr.battalionPosition.y - 10);
            Risk.defaultFont.draw(fboBatch, game.battalionCount(wr.territory) + "", wr.battalionPosition.x + 4, wr.battalionPosition.y + 10);
        }
        fboBatch.end();

        fbo.end();

        staticMapTexture = new TextureRegion(fbo.getColorBufferTexture());
        staticMapTexture.flip(false, true);

        Image mapImage = new Image(staticMapTexture);
        getContentTable().add(mapImage).center();

        addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                hide();
                return false;
            }
        });

    }

    @Override
    public Dialog show(Stage stage, Action action) {
        Gdx.input.setInputProcessor(stage);
        Dialog d = super.show(stage, action);
        return d;
    }

    @Override
    public void hide() {
        super.hide();
        Gdx.input.setInputProcessor(input);
    }

}
