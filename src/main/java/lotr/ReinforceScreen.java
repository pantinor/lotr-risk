package lotr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import lotr.Constants.ArmyType;
import static lotr.Risk.GREEN_BATTALION;
import static lotr.Risk.GREEN_LEADER;
import static lotr.Risk.BLACK_BATTALION;
import static lotr.Risk.BLACK_LEADER;
import static lotr.Risk.RED_BATTALION;
import static lotr.Risk.RED_LEADER;
import lotr.Risk.RegionWrapper;
import static lotr.Risk.TMX_MAP;
import static lotr.Risk.YELLOW_BATTALION;
import static lotr.Risk.YELLOW_LEADER;
import static lotr.Risk.RED_CIRCLE;
import static lotr.Risk.GREEN_CIRCLE;
import static lotr.Risk.BLACK_CIRCLE;
import static lotr.Risk.YELLOW_CIRCLE;

public class ReinforceScreen implements Screen {

    protected float time = 0;
    private final HexagonalTiledMapRenderer renderer;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    Game game;
    Risk main;
    Army army;
    GameScreen gameScreen;

    float unitScale = 0.35f;
    List<RegionWrapper> regions = new ArrayList<>();

    private final SpriteBatch batch = new SpriteBatch();
    private final Viewport mapViewport;
    private final OrthographicCamera camera;

    private final Viewport viewport = new ScreenViewport();
    private final Stage stage = new Stage(viewport);

    private static final int MAP_VIEWPORT_WIDTH = 736;
    private static final int MAP_VIEWPORT_HEIGHT = 968;

    private final TextButton reinforce, exit;
    private final Table table = new Table();
    private final Label redLabel = new Label("-", Risk.skin);

    private int turnIndex = 0;
    private Random rand = new Random();

    public ReinforceScreen(Risk main, Game game, Army army, GameScreen gameScreen) {

        this.game = game;
        this.main = main;
        this.army = army;
        this.gameScreen = gameScreen;

        this.camera = new OrthographicCamera(MAP_VIEWPORT_WIDTH, MAP_VIEWPORT_HEIGHT);
        this.mapViewport = new ScreenViewport(this.camera);
        this.camera.position.set(MAP_VIEWPORT_WIDTH / 2 - 200, MAP_VIEWPORT_HEIGHT / 2 - 15, 0);
        this.mapViewport.update(MAP_VIEWPORT_WIDTH * 2, MAP_VIEWPORT_HEIGHT, false);

        this.renderer = new HexagonalTiledMapRenderer(TMX_MAP, this.unitScale);
        this.renderer.setView(this.camera);

        MapLayer regionsLayer = TMX_MAP.getLayers().get("regions");
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
            w.territory = TerritoryCard.getTerritory(name);
            regions.add(w);
        }

        TiledMapTileLayer iconLayer = (TiledMapTileLayer) TMX_MAP.getLayers().get("icons");
        Risk.setPoints(iconLayer, regions, unitScale);

        this.table.align(Align.left | Align.top).pad(5);
        this.table.columnDefaults(0).expandX().left().uniformX();

        ScrollPane sp = new ScrollPane(table, Risk.skin);
        sp.setBounds(300, 700, 300, 225);
        this.stage.addActor(sp);

        this.reinforce = new TextButton("REINFORCE", Risk.skin);

        this.reinforce.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                boolean foundSelected = false;
                for (RegionWrapper w : regions) {
                    if (w.selected) {
                        foundSelected = true;
                        if (!game.isClaimed(w.territory)) {

                        } else {
                            Sounds.play(Sound.NEGATIVE_EFFECT);
                        }
                        break;
                    }
                }
                if (!foundSelected) {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                }
            }
        });
        this.reinforce.setBounds(525, 600, 150, 40);

        this.exit = new TextButton("EXIT", Risk.skin);
        this.exit.setVisible(false);
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                main.setScreen(ReinforceScreen.this.gameScreen);
            }
        });
        this.exit.setBounds(525, 600, 150, 40);

        this.stage.addActor(this.reinforce);
        this.stage.addActor(this.exit);

        this.stage.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof InputEvent) {
                    InputEvent ev = (InputEvent) event;
                    if (ev.getType() == Type.touchDown && ev.getStageX() > 700) {
                        Vector3 tmp = camera.unproject(new Vector3(ev.getStageX(), ev.getStageY(), 0));
                        Vector2 v = new Vector2(tmp.x, MAP_VIEWPORT_HEIGHT - tmp.y - 32);
                        for (RegionWrapper w : regions) {
                            if (w.polygon.contains(v)) {
                                w.selected = true;
                            } else {
                                w.selected = false;
                            }
                        }
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
        time += delta;
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderer.render();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

        for (RegionWrapper w : regions) {

            if (w.territory != null) {
                renderer.getBatch().begin();

                Risk.fontSmall.draw(renderer.getBatch(), w.name, w.namePosition.x + 0, w.namePosition.y + 0);

                ArmyType at = game.getOccupyingArmy(w.territory);

                if (at == ArmyType.RED) {
                    renderer.getBatch().draw(RED_BATTALION.getKeyFrame(time, true), w.battalionPosition.x, w.battalionPosition.y);
                    if (w.territory == game.red.leader1.territory || w.territory == game.red.leader2.territory) {
                        renderer.getBatch().draw(RED_LEADER.getKeyFrame(time, true), w.battalionPosition.x - 48, w.battalionPosition.y - 0);
                    }
                    renderer.getBatch().draw(RED_CIRCLE, w.textPosition.x + 15, w.textPosition.y + 0);
                }
                if (at == ArmyType.BLACK) {
                    renderer.getBatch().draw(BLACK_BATTALION.getKeyFrame(time, true), w.battalionPosition.x, w.battalionPosition.y);
                    if (w.territory == game.black.leader1.territory || w.territory == game.black.leader2.territory) {
                        renderer.getBatch().draw(BLACK_LEADER.getKeyFrame(time, true), w.battalionPosition.x - 48, w.battalionPosition.y - 0);
                    }
                    renderer.getBatch().draw(BLACK_CIRCLE, w.textPosition.x + 15, w.textPosition.y + 0);
                }
                if (at == ArmyType.GREEN) {
                    renderer.getBatch().draw(GREEN_BATTALION.getKeyFrame(time, true), w.battalionPosition.x, w.battalionPosition.y);
                    if (w.territory == game.green.leader1.territory || w.territory == game.green.leader2.territory) {
                        renderer.getBatch().draw(GREEN_LEADER.getKeyFrame(time, true), w.battalionPosition.x - 48, w.battalionPosition.y - 0);
                    }
                    renderer.getBatch().draw(GREEN_CIRCLE, w.textPosition.x + 15, w.textPosition.y + 0);
                }
                if (at == ArmyType.YELLOW) {
                    renderer.getBatch().draw(YELLOW_BATTALION.getKeyFrame(time, true), w.battalionPosition.x, w.battalionPosition.y);
                    if (w.territory == game.yellow.leader1.territory || w.territory == game.yellow.leader2.territory) {
                        renderer.getBatch().draw(YELLOW_LEADER.getKeyFrame(time, true), w.battalionPosition.x - 48, w.battalionPosition.y - 0);
                    }
                    renderer.getBatch().draw(YELLOW_CIRCLE, w.textPosition.x + 15, w.textPosition.y + 0);
                }

                int bc = game.battalionCount(w.territory);
                if (bc > 0) {
                    Risk.fontSmall.draw(renderer.getBatch(), bc + "", w.textPosition.x + 24, w.textPosition.y + 17);
                }

                renderer.getBatch().end();
            }

        }

        for (RegionWrapper w : regions) {
            if (w.selected) {
                Gdx.gl.glLineWidth(6);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(Color.RED);
                shapeRenderer.polygon(w.vertices);
                shapeRenderer.end();
            }
        }

        this.batch.begin();
        this.gameScreen.hud().render(this.batch, this.game);
        this.batch.end();

        stage.act();
        stage.draw();

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
