package lotr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.payne.games.piemenu.AnimatedPieMenu;
import com.payne.games.piemenu.PieMenu;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lotr.Constants.ArmyType;
import lotr.Risk.RegionWrapper;
import static lotr.Risk.TMX_MAP;
import static lotr.Risk.GREEN_CIRCLE;
import static lotr.Risk.BLACK_CIRCLE;
import static lotr.Risk.LEADER_CIRCLE;
import static lotr.Risk.RED_CIRCLE;
import static lotr.Risk.YELLOW_CIRCLE;
import static lotr.util.RendererUtil.filledPolygon;

public class FortifyScreen implements Screen {

    protected float time = 0;
    private final HexagonalTiledMapRenderer renderer;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    private final Game game;
    private final Risk main;
    private final Army army;
    private final GameScreen gameScreen;
    private final TurnWidget turnWidget;
    private AnimatedPieMenu fortifyRadial;

    private final float unitScale = 0.35f;
    private final List<RegionWrapper> regions = new ArrayList<>();
    private RegionWrapper selectedTerritory, selectedFortifyTerritory;
    private final List<TerritoryCard> claimedTerritories;

    private final SpriteBatch hudbatch = new SpriteBatch();
    private final SpriteBatch batch = new SpriteBatch();

    private final Viewport mapViewport;
    private final OrthographicCamera camera;

    private final Viewport viewport = new ScreenViewport();
    private final Stage stage = new Stage(viewport);

    private static final int MAP_VIEWPORT_WIDTH = 736;
    private static final int MAP_VIEWPORT_HEIGHT = 968;

    private final TextButton exit;

    private final GlyphLayout layout = new GlyphLayout();
    private static final List<String> TEXTS = new ArrayList<>();

    static {
        TEXTS.add("You get ONE fortification with your battalions.  To fortify your position, take as many battalions as you'd like from one of your territories and move them to another connected territory.  You MUST leave at least one battalion behind - you cannot abandon a territory.");
    }

    public FortifyScreen(Risk main, Game game, Army army, GameScreen gameScreen, TurnWidget turnWidget) {

        this.game = game;
        this.main = main;
        this.army = army;
        this.gameScreen = gameScreen;
        this.turnWidget = turnWidget;

        this.camera = new OrthographicCamera(MAP_VIEWPORT_WIDTH, MAP_VIEWPORT_HEIGHT);
        this.mapViewport = new ScreenViewport(this.camera);
        this.camera.position.set(MAP_VIEWPORT_WIDTH / 2 - 200, MAP_VIEWPORT_HEIGHT / 2 - 15, 0);
        this.mapViewport.update(MAP_VIEWPORT_WIDTH * 2, MAP_VIEWPORT_HEIGHT, false);

        this.renderer = new HexagonalTiledMapRenderer(TMX_MAP, this.unitScale, this.batch);
        this.renderer.setView(this.camera);

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
            poly.setScale(unitScale, unitScale);
            poly.setPosition(poly.getX() * unitScale, poly.getY() * unitScale);

            w.vertices = poly.getTransformedVertices();
            w.name = name;
            w.territory = TerritoryCard.getTerritory(name);
            regions.add(w);
        }

        TiledMapTileLayer iconLayer = (TiledMapTileLayer) TMX_MAP.getLayers().get("icons");
        Risk.setPoints(iconLayer, regions, unitScale);

        this.claimedTerritories = army.claimedTerritories();

        this.exit = new TextButton("FINISH", Risk.ccskin, "arcade");
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                main.setScreen(FortifyScreen.this.gameScreen);
            }
        });

        this.exit.setBounds(400, 560, 84, 84);

        this.stage.addActor(this.exit);

        PieMenu.PieMenuStyle style = new PieMenu.PieMenuStyle();
        style.backgroundColor = new Color(1, 1, 1, .3f);
        style.selectedColor = new Color(.7f, .3f, .5f, 1);
        style.sliceColor = new Color(0, .7f, 0, 1);
        style.alternateSliceColor = new Color(.7f, 0, 0, 1);

        fortifyRadial = new AnimatedPieMenu(Risk.skin.getRegion("white"), style, 75, .3f, 180, 320);
        fortifyRadial.setInfiniteSelectionRange(true);
        fortifyRadial.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                fortifyRadial.transitionToClosing(.4f);
                int index = fortifyRadial.getSelectedIndex();
                if (!fortifyRadial.isValidIndex(index)) {
                    return;
                }
                Actor child = fortifyRadial.getChild(index);
                Integer fortifyCount = (Integer) child.getUserObject();

                for (Battalion b : army.getBattalions()) {
                    if (b.territory == selectedTerritory.territory && fortifyCount > 0) {
                        b.territory = selectedFortifyTerritory.territory;
                        fortifyCount--;
                    }
                }

                if (game.hasLeader(army, selectedTerritory.territory)) {
                    game.moveLeader(army, selectedTerritory.territory, selectedFortifyTerritory.territory);
                    //TODO check mission card
                }

                //only one fortification allowed!
                fortifyRadial.remove();
            }
        });

        this.stage.addActor(fortifyRadial);

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, new InputAdapter() {
            @Override
            public boolean touchDown(int x, int y, int pointer, int button) {
                if (button == 0) {
                    selectedFortifyTerritory = null;
                    selectedTerritory = null;
                    Vector3 tmp = camera.unproject(new Vector3(x, y, 0));
                    Vector2 v = new Vector2(tmp.x, tmp.y - 0);
                    for (RegionWrapper w : regions) {
                        if (w.polygon.contains(v) && claimedTerritories.contains(w.territory)) {
                            selectedTerritory = w;
                            break;
                        }
                    }

                }

                if (button == 1 && selectedTerritory != null) {
                    selectedFortifyTerritory = null;
                    Vector3 tmp = camera.unproject(new Vector3(x, y, 0));
                    Vector2 v = new Vector2(tmp.x, tmp.y - 0);
                    for (RegionWrapper w : regions) {
                        if (w.polygon.contains(v) && claimedTerritories.contains(w.territory) && selectedTerritory.adjacents.containsKey(w.territory)) {
                            selectedFortifyTerritory = w;

                            int count = game.battalionCount(selectedTerritory.territory);
                            if (count > 1) {
                                fortifyRadial.resetSelection();
                                fortifyRadial.clearChildren();
                                for (int i = 1; i < count; i++) {
                                    Label l = new Label(Integer.toString(i), Risk.skin);
                                    l.setUserObject(i);
                                    fortifyRadial.addActor(l);
                                }
                                fortifyRadial.centerOnMouse();
                                fortifyRadial.animateOpening(.4f);
                            }

                            break;
                        }
                    }
                }

                return false;
            }

        }));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }

    Vector3 tmpb = new Vector3();
    Vector3 tmpt = new Vector3();

    @Override
    public void render(float delta) {
        time += delta;
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderer.render();

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

        if (selectedTerritory != null) {
            if (selectedFortifyTerritory == null) {
                for (TerritoryCard adj : selectedTerritory.territory.adjacents()) {
                    Army occupyingArmy = game.getOccupyingArmy(adj);
                    if (occupyingArmy == army) {
                        filledPolygon(shapeRenderer, new Color(0x7fff0080), selectedTerritory.adjacents.get(adj).vertices);
                    }
                }
            } else {
                filledPolygon(shapeRenderer, new Color(0xffff0080), selectedFortifyTerritory.vertices);
            }
            filledPolygon(shapeRenderer, new Color(0x00ff0080), selectedTerritory.vertices);
        }

        hudbatch.begin();
        for (RegionWrapper w : regions) {
            if (w.territory != null) {

                ArmyType at = game.getOccupyingArmy(w.territory).armyType;

                tmpb.set(w.battalionPosition);
                tmpt.set(w.textPosition);

                Vector3 bp = this.camera.project(tmpb);
                Vector3 tp = this.camera.project(tmpt);

                float bx = bp.x - 12;
                float by = bp.y - 12;
                float tx = tp.x - 12;
                float ty = tp.y - 12;

                if (at == ArmyType.RED) {
                    if (w.territory == game.red.leader1.territory || w.territory == game.red.leader2.territory) {
                        hudbatch.draw(LEADER_CIRCLE, bx, by);
                    }
                    hudbatch.draw(RED_CIRCLE, tx, ty);
                }
                if (at == ArmyType.BLACK) {
                    if (w.territory == game.black.leader1.territory || w.territory == game.black.leader2.territory) {
                        hudbatch.draw(LEADER_CIRCLE, bx, by);
                    }
                    hudbatch.draw(BLACK_CIRCLE, tx, ty);
                }
                if (at == ArmyType.GREEN) {
                    if (w.territory == game.green.leader1.territory || w.territory == game.green.leader2.territory) {
                        hudbatch.draw(LEADER_CIRCLE, bx, by);
                    }
                    hudbatch.draw(GREEN_CIRCLE, tx, ty);
                }
                if (at == ArmyType.YELLOW) {
                    if (w.territory == game.yellow.leader1.territory || w.territory == game.yellow.leader2.territory) {
                        hudbatch.draw(LEADER_CIRCLE, bx, by);
                    }
                    hudbatch.draw(YELLOW_CIRCLE, tx, ty);
                }

                int bc = game.battalionCount(w.territory);
                if (bc > 0) {
                    Risk.font.draw(hudbatch, bc + "", tp.x - 8, tp.y + 6);
                }

            }
        }

        int x = 15;
        int y = Risk.SCREEN_HEIGHT - 15;

        for (String text : TEXTS) {
            layout.setText(Risk.font, text, Color.WHITE, 320, Align.left, true);
            Risk.font.draw(hudbatch, layout, 15, y);
            y -= layout.height + 30;
        }

        hudbatch.end();

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
