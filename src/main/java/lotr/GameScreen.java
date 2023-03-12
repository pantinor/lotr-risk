package lotr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.payne.games.piemenu.AnimatedPieMenu;
import com.payne.games.piemenu.PieMenu;
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
import lotr.Risk.RegionWrapper;
import lotr.Risk.RingPathWrapper;
import lotr.TurnWidget.Step;

public class GameScreen implements Screen, InputProcessor {

    protected float time = 0;

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
    private final TurnWidget turnWidget;

    private final List<RegionWrapper> regions = new ArrayList<>();
    private RegionWrapper selectedAttackingTerritory, selectedDefendingTerritory;
    private Integer attackingCount, defendingCount;
    private AnimatedPieMenu invasionRadial;

    public GameScreen(Risk main, Game game) {

        this.camera = new OrthographicCamera(SCREEN_WIDTH, SCREEN_HEIGHT);
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
            w.name = name.toUpperCase();
            w.territory = TerritoryCard.getTerritory(name);

            regions.add(w);
        }

        TiledMapTileLayer iconLayer = (TiledMapTileLayer) TMX_MAP.getLayers().get("icons");
        Risk.setPoints(iconLayer, regions, 1f);

        this.game = game;
        this.main = main;

        this.widgetStage = new Stage();
        this.turnWidget = new TurnWidget(main, this, game);
        this.widgetStage.addActor(turnWidget);

        PieMenu.PieMenuStyle style = new PieMenu.PieMenuStyle();
        style.backgroundColor = new Color(1, 1, 1, .3f);
        style.selectedColor = new Color(.7f, .3f, .5f, 1);
        style.sliceColor = new Color(0, .7f, 0, 1);
        style.alternateSliceColor = new Color(.7f, 0, 0, 1);

        invasionRadial = new AnimatedPieMenu(Risk.skin.getRegion("white"), style, 75, .3f, 180, 320);
        invasionRadial.setInfiniteSelectionRange(true);
        invasionRadial.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                invasionRadial.transitionToClosing(.4f);
                int index = invasionRadial.getSelectedIndex();
                if (!invasionRadial.isValidIndex(index)) {
                    return;
                }
                Actor child = invasionRadial.getChild(index);
                Object tmp = child.getUserObject();
                if (tmp != null) {
                    attackingCount = (Integer) tmp;
                } else {
                    attackingCount = null;
                }
            }
        });

        this.widgetStage.addActor(invasionRadial);

        this.input = new InputMultiplexer(widgetStage, new InputAdapter() {

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

                Vector3 tmp = camera.unproject(new Vector3(x, y, 0));
                Vector2 v = new Vector2(tmp.x, tmp.y - 0);
                for (RegionWrapper w : regions) {
                    if (w.polygon.contains(v)) {
                        if (attackingCount != null) {
                            Army occupyingArmy = game.getOccupyingArmy(w.territory);
                            if (selectedAttackingTerritory.adjacents.containsKey(w.territory) && occupyingArmy != game.current()) {
                                selectedDefendingTerritory = w;
                                int dc = game.battalionCount(w.territory);
                                if (dc > 2) {
                                    dc = 2;
                                }
                                turnWidget.setAttackButtonListener(game.current(), occupyingArmy, selectedAttackingTerritory.territory, w.territory, attackingCount, dc);
                                break;
                            } else {
                                attackingCount = null;
                                selectedDefendingTerritory = null;
                            }
                        } else {
                            selectedAttackingTerritory = w;
                            break;
                        }
                    }
                }

                return false;
            }

            @Override
            public boolean touchUp(int x, int y, int pointer, int button) {
                if (button == 1 && selectedAttackingTerritory != null && selectedDefendingTerritory == null) {
                    int count = game.battalionCount(selectedAttackingTerritory.territory) - 1;
                    if (count > 0 && game.isClaimed(selectedAttackingTerritory.territory) == game.current()) {
                        invasionRadial.resetSelection();
                        invasionRadial.clearChildren();
                        for (int i = 0; i < count; i++) {
                            Label l = new Label(Integer.toString(i + 1), Risk.skin);
                            l.setUserObject(i + 1);
                            invasionRadial.addActor(l);
                        }
                        invasionRadial.centerOnMouse();
                        invasionRadial.animateOpening(.4f);
                    }
                }
                return false;
            }

        });
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(input);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }

    @Override
    public void render(float delta) {
        time += delta;
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        renderer.setView(camera);

        renderer.render();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);

        for (RegionWrapper w : regions) {

            if (w.territory != null) {
                renderer.getBatch().begin();

                Risk.font.draw(renderer.getBatch(), w.name, w.namePosition.x + 0, w.namePosition.y + 0);

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
                    Risk.font.draw(renderer.getBatch(), bc + "", w.textPosition.x + 20, w.textPosition.y + 19);
                }

                renderer.getBatch().end();
            }

        }

        if (selectedAttackingTerritory != null) {
            Gdx.gl.glLineWidth(8);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            if (selectedDefendingTerritory == null) {
                shapeRenderer.setColor(Color.YELLOW);
                for (TerritoryCard adj : selectedAttackingTerritory.territory.adjacents()) {
                    shapeRenderer.polygon(selectedAttackingTerritory.adjacents.get(adj).vertices);
                }
            } else {
                shapeRenderer.setColor(Color.GREEN);
                shapeRenderer.polygon(selectedDefendingTerritory.vertices);
                
                //shapeRenderer.setColor(Color.WHITE);
                //shapeRenderer.curve(0.0f, 0.25f, 0.2f, 0.3f, 0.3f, 0.6f, 0.1f, 0.5f, 30);
            }
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.polygon(selectedAttackingTerritory.vertices);
            shapeRenderer.end();
        }

        for (RingPathWrapper rw : RING_PATHS) {
            if (rw.selected) {
                renderer.getBatch().begin();
                renderer.getBatch().draw(FRODO.getKeyFrame(time, true), rw.x + 10, rw.y + 10);
                renderer.getBatch().draw(SAM.getKeyFrame(time, true), rw.x - 10, rw.y - 10);
                renderer.getBatch().end();
            }
        }

        this.batch.begin();

        this.hud.render(this.batch, this.game, delta);
        this.batch.end();

        this.widgetStage.act();
        this.widgetStage.draw();
    }

    @Override
    public boolean keyUp(int keycode) {
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

    public Hud hud() {
        return this.hud;
    }

}
