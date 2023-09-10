package lotr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.renderers.HexagonalTiledMapRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.payne.games.piemenu.PieMenu;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lotr.Constants.ArmyType;
import lotr.Game.Step;
import static lotr.Risk.GREEN_BATTALION;
import static lotr.Risk.GREEN_LEADER;
import static lotr.Risk.BLACK_BATTALION;
import static lotr.Risk.BLACK_LEADER;
import static lotr.Risk.RED_BATTALION;
import static lotr.Risk.RED_LEADER;
import static lotr.Risk.YELLOW_BATTALION;
import static lotr.Risk.YELLOW_LEADER;
import static lotr.Risk.RED_CIRCLE;
import static lotr.Risk.GREEN_CIRCLE;
import static lotr.Risk.BLACK_CIRCLE;
import static lotr.Risk.YELLOW_CIRCLE;
import lotr.Risk.RegionWrapper;
import static lotr.Risk.TMX_MAP;
import lotr.util.LocationActor;
import static lotr.util.RendererUtil.filledPolygon;

public class GameScreen implements Screen {

    private float time = 0;

    private final Stage mapStage, widgetStage;
    private final Batch batch = new SpriteBatch();
    private final InputMultiplexer input;

    private final HexagonalTiledMapRenderer renderer;
    private final OrthographicCamera camera;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final Viewport viewport;

    private final Game game;
    private final Risk main;

    private final Hud hud = new Hud();
    public final TurnWidget turnWidget;

    private final List<RegionWrapper> regions = new ArrayList<>();
    public RegionWrapper selectedAttackingTerritory, selectedDefendingTerritory;

    public RingPath ringPath;
    private ShippingRoutes shippingRoutes;
    public AdventureCardWidget cardSlider;
    public LogScrollPane logs;

    public GameScreen(Risk main, Game game) {

        this.game = game;
        this.main = main;

        this.camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.viewport = new ScreenViewport(this.camera);
        this.renderer = new HexagonalTiledMapRenderer(TMX_MAP, 1f);
        this.mapStage = new Stage(this.viewport, this.renderer.getBatch());

        MapLayer regionsLayer = TMX_MAP.getLayers().get("regions");
        Iterator<MapObject> iter = regionsLayer.getObjects().iterator();
        while (iter.hasNext()) {
            PolygonMapObject obj = (PolygonMapObject) iter.next();

            Polygon poly = new Polygon(obj.getPolygon().getVertices());
            poly.setPosition(obj.getPolygon().getX(), obj.getPolygon().getY());
            poly.setOrigin(obj.getPolygon().getOriginX(), obj.getPolygon().getOriginY());

            String name = obj.getName();

            RegionWrapper w = new RegionWrapper();
            w.polygon = poly;
            w.vertices = poly.getTransformedVertices();
            w.territory = TerritoryCard.getTerritory(name);
            w.name = w.territory.capitalized();

            regions.add(w);
        }

        TiledMapTileLayer iconLayer = (TiledMapTileLayer) TMX_MAP.getLayers().get("icons");
        Risk.setPoints(iconLayer, regions, 1f);

        MapLayer locLayer = TMX_MAP.getLayers().get("locations");
        Iterator<MapObject> locIter = locLayer.getObjects().iterator();
        while (locIter.hasNext()) {
            TiledMapTileMapObject obj = (TiledMapTileMapObject) locIter.next();
            Location l = Location.valueOf(obj.getName());
            TextureRegion tr = obj.getTile().getTextureRegion();
            float x = obj.getProperties().get("x", Float.class);
            float y = obj.getProperties().get("y", Float.class);
            LocationActor la = new LocationActor(tr, l, x, y);
            mapStage.addActor(la);
        }

        this.widgetStage = Risk.STAGE = new Stage(new ScreenViewport());

        this.turnWidget = new TurnWidget(main, this, game);
        this.widgetStage.addActor(turnWidget);

        PieMenu.PieMenuStyle style = new PieMenu.PieMenuStyle();
        style.backgroundColor = new Color(1, 1, 1, .3f);
        style.selectedColor = new Color(.7f, .3f, .5f, 1);
        style.sliceColor = new Color(0, .7f, 0, 1);
        style.alternateSliceColor = new Color(.7f, 0, 0, 1);

        logs = new LogScrollPane();
        cardSlider = new AdventureCardWidget(widgetStage, game, logs);
        
        widgetStage.addActor(cardSlider);
        widgetStage.addActor(logs);

        ringPath = new RingPath(mapStage, shapeRenderer, TMX_MAP.getLayers().get("ring-path"), logs);
        shippingRoutes = new ShippingRoutes(shapeRenderer);

        input = new InputMultiplexer(widgetStage, new InputAdapter() {

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
            public boolean touchDown(int x, int y, int pointer, int button) {
                last.set(-1, -1, -1);

                turnWidget.clearCombat();

                Vector3 tmp = camera.unproject(new Vector3(x, y, 0));
                Vector2 v = new Vector2(tmp.x, tmp.y - 0);

                if (button == 0 && game.currentStep == Step.COMBAT) {
                    selectedDefendingTerritory = null;
                    selectedAttackingTerritory = null;
                    for (RegionWrapper w : regions) {
                        if (w.polygon.contains(v)) {
                            Army occupyingArmy = game.getOccupyingArmy(w.territory);
                            if (occupyingArmy == game.current()) {
                                selectedAttackingTerritory = w;

                                //draw some pointer arrows to potential targets to attack
                                int count = game.battalionCount(w.territory);
                                if (count > 1 && game.isClaimed(w.territory) == game.current()) {
                                    for (RegionWrapper adj : w.adjacents.values()) {
                                        if (game.isClaimed(adj.territory) != game.current()) {
                                            Vector3 start = new Vector3(w.textPosition);
                                            Vector3 end = new Vector3(adj.textPosition);
                                            InvasionPointerActor arrow = new InvasionPointerActor(shapeRenderer, start, end);
                                            mapStage.addActor(arrow);
                                            arrow.addAction(sequence(delay(3, removeActor(arrow))));
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                }

                if (button == 1 && selectedAttackingTerritory != null && game.currentStep == Step.COMBAT) {
                    selectedDefendingTerritory = null;
                    for (RegionWrapper w : regions) {
                        if (w.polygon.contains(v)) {
                            Army occupyingArmy = game.getOccupyingArmy(w.territory);
                            if (selectedAttackingTerritory.adjacents.containsKey(w.territory) && occupyingArmy != game.current()) {
                                selectedDefendingTerritory = w;
                                GameScreen.this.turnWidget.setCombat(game.current(), occupyingArmy, selectedAttackingTerritory.territory, w.territory);
                                break;
                            }
                        }
                    }
                }

                return false;
            }

        });

        if (game.current().isBot()) {
            widgetStage.addAction(game.current().bot.run());
        }
    }

    public void lookAt(TerritoryCard t) {
        for (RegionWrapper w : regions) {
            if (w.territory == t) {
                camera.position.set(w.battalionPosition.x, w.battalionPosition.y, 0);
            }
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(input);

        turnWidget.clearCombat();
        selectedAttackingTerritory = null;
        selectedDefendingTerritory = null;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);

        widgetStage.getViewport().setWorldWidth(width);
        widgetStage.getViewport().setWorldHeight(height);
        widgetStage.getViewport().update(width, height, true);
    }

    @Override
    public void render(float delta) {
        time += delta;
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        renderer.setView(camera);

        renderer.render();

        shapeRenderer.setProjectionMatrix(camera.combined);

        ringPath.render();
        shippingRoutes.render();

        if (selectedAttackingTerritory != null) {
            if (selectedDefendingTerritory == null) {
                for (TerritoryCard adj : selectedAttackingTerritory.territory.adjacents()) {
                    Army occupyingArmy = game.getOccupyingArmy(adj);
                    if (occupyingArmy != game.current()) {
                        filledPolygon(shapeRenderer, new Color(0x7f7f7f80), selectedAttackingTerritory.adjacents.get(adj).vertices);
                    }
                }
            } else {
                filledPolygon(shapeRenderer, new Color(0xff000080), selectedDefendingTerritory.vertices);
            }
            filledPolygon(shapeRenderer, new Color(0x00ff0080), selectedAttackingTerritory.vertices);
        }

        this.mapStage.act();
        this.mapStage.draw();

        for (RegionWrapper w : regions) {

            if (w.territory != null) {
                renderer.getBatch().begin();

                if (Risk.textToggle) {
                    Risk.blackFont.draw(renderer.getBatch(), w.name, w.namePosition.x + 1, w.namePosition.y - 1);
                    Risk.font.draw(renderer.getBatch(), w.name, w.namePosition.x + 0, w.namePosition.y + 0);
                }

                ArmyType at = game.getOccupyingArmy(w.territory).armyType;

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
                    Risk.defaultFont.draw(renderer.getBatch(), bc + "", w.textPosition.x + 20, w.textPosition.y + 19);
                }

                renderer.getBatch().end();
            }

        }

        this.widgetStage.getBatch().begin();
        this.hud.render(this.widgetStage.getBatch(), this.game, delta);
        this.widgetStage.getBatch().end();

        this.widgetStage.act();
        this.widgetStage.draw();

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

    public void setCards(boolean show) {
        if (show) {
            cardSlider.set();
            cardSlider.show();
        } else {
            cardSlider.hide();
        }
    }

    public void toggleLog(boolean show) {
        if (show) {
            logs.show();
        } else {
            logs.hide();
        }
    }

}
