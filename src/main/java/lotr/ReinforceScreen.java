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
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lotr.Constants.ArmyType;
import lotr.Constants.BattalionType;
import lotr.Risk.RegionWrapper;
import static lotr.Risk.TMX_MAP;
import static lotr.Risk.GREEN_CIRCLE;
import static lotr.Risk.BLACK_CIRCLE;
import static lotr.Risk.LEADER_CIRCLE;
import static lotr.Risk.RED_CIRCLE;
import static lotr.Risk.YELLOW_CIRCLE;

public class ReinforceScreen implements Screen {

    protected float time = 0;
    private final HexagonalTiledMapRenderer renderer;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    private final Game game;
    private final Risk main;
    private final Army army;
    private final GameScreen gameScreen;

    private final float unitScale = 0.35f;
    private final List<RegionWrapper> regions = new ArrayList<>();

    private final SpriteBatch hudbatch = new SpriteBatch();
    private final SpriteBatch batch = new SpriteBatch();

    private final Viewport mapViewport;
    private final OrthographicCamera camera;

    private final Viewport viewport = new ScreenViewport();
    private final Stage stage = new Stage(viewport);

    private static final int MAP_VIEWPORT_WIDTH = 736;
    private static final int MAP_VIEWPORT_HEIGHT = 968;

    private final Table table = new Table();

    private final TextButton reinforceStrongholds, reinforceTerritories, reinforceRegions, reinforceCards, exit;

    private final List<TerritoryCard> claimedTerritories;
    private final List<TerritoryCard> reinforcedStrongholds = new ArrayList<>();

    private int strongholdReinforcements, territoryReinforcements, regionReinforcements, cardReinforcements;
    private int sumArchers = 0, sumRiders = 0, sumEagles = 0;

    public ReinforceScreen(Risk main, Game game, Army army, GameScreen gameScreen) {

        this.game = game;
        this.main = main;
        this.army = army;
        this.gameScreen = gameScreen;

        this.claimedTerritories = army.claimedTerritories();

        strongholdReinforcements = army.ownedStrongholds(claimedTerritories).size();
        territoryReinforcements = claimedTerritories.size() / 3 < 3 ? 3 : claimedTerritories.size() / 3;

        for (Region r : Region.values()) {
            if (claimedTerritories.containsAll(r.territories())) {
                regionReinforcements += r.reinforcements();
            }
        }

        for (TerritoryCard c : army.territoryCards) {
            if (c.battalionType() == BattalionType.ELVEN_ARCHER || c.battalionType() == null) {
                sumArchers++;
            }
            if (c.battalionType() == BattalionType.DARK_RIDER || c.battalionType() == null) {
                sumRiders++;
            }
            if (c.battalionType() == BattalionType.EAGLE || c.battalionType() == null) {
                sumEagles++;
            }
        }
        if (sumArchers >= 3) {
            cardReinforcements = 4;
        }
        if (sumRiders >= 3) {
            cardReinforcements = 6;
        }
        if (sumEagles >= 3) {
            cardReinforcements = 8;
        }
        if (sumArchers >= 1 && sumRiders >= 1 && sumEagles >= 1) {
            cardReinforcements = 10;
        }

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

        this.table.align(Align.left | Align.top).pad(5);
        table.columnDefaults(0).expandX().left().uniformX();
        table.columnDefaults(1).expandX().left().uniformX();

        ScrollPane sp = new ScrollPane(table, Risk.skin);
        sp.setBounds(300, 50, 300, 500);
        this.stage.addActor(sp);

        for (TerritoryCard c : army.territoryCards) {
            CheckBox cb = new CheckBox(c.toString().replace("_", " "), Risk.skin, "default");
            cb.setUserObject(c);
            this.table.add(cb);
            String bt = c.battalionType() == null ? "WILDCARD" : c.battalionType().toString().replace("_", " ");
            this.table.add(new Label(bt, Risk.skin));
            this.table.row();
        }

        this.reinforceStrongholds = new TextButton("REINFORCE STRONGHOLDS", Risk.skin);
        this.reinforceTerritories = new TextButton("REINFORCE TERRITORIES", Risk.skin);
        this.reinforceRegions = new TextButton("REINFORCE REGIONS", Risk.skin);
        this.reinforceCards = new TextButton("REINFORCE CARDS", Risk.skin);

        this.reinforceStrongholds.setBounds(400, 720, 220, 35);
        this.reinforceTerritories.setBounds(400, 680, 220, 35);
        this.reinforceRegions.setBounds(400, 640, 220, 35);
        this.reinforceCards.setBounds(400, 600, 220, 35);

        this.reinforceStrongholds.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                boolean foundSelected = false;
                for (RegionWrapper w : regions) {
                    if (w.selected) {
                        foundSelected = true;
                        if (strongholdReinforcements > 0 && claimedTerritories.contains(w.territory)
                                && Location.getStronghold(w.territory) != null && !reinforcedStrongholds.contains(w.territory)) {
                            army.addBattalion(w.territory);
                            reinforcedStrongholds.add(w.territory);
                            strongholdReinforcements--;
                            Sounds.play(Sound.TRIGGER);
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
        this.reinforceTerritories.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                boolean foundSelected = false;
                for (RegionWrapper w : regions) {
                    if (w.selected) {
                        foundSelected = true;
                        if (territoryReinforcements > 0 && claimedTerritories.contains(w.territory)) {
                            army.addBattalion(w.territory);
                            territoryReinforcements--;
                            Sounds.play(Sound.TRIGGER);
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
        this.reinforceRegions.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                boolean foundSelected = false;
                for (RegionWrapper w : regions) {
                    if (w.selected) {
                        foundSelected = true;
                        if (regionReinforcements > 0 && claimedTerritories.contains(w.territory)) {
                            army.addBattalion(w.territory);
                            regionReinforcements--;
                            Sounds.play(Sound.TRIGGER);
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
        this.reinforceCards.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                boolean foundSelected = false;
                for (RegionWrapper w : regions) {
                    if (w.selected) {
                        foundSelected = true;
                        if (false) {

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

        this.exit = new TextButton("EXIT", Risk.skin);
        this.exit.setVisible(false);
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                main.setScreen(ReinforceScreen.this.gameScreen);
            }
        });
        this.exit.setBounds(525, 600, 150, 40);

        this.stage.addActor(this.reinforceStrongholds);
        this.stage.addActor(this.reinforceTerritories);
        this.stage.addActor(this.reinforceRegions);
        this.stage.addActor(this.reinforceCards);

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
        Gdx.input.setInputProcessor(this.stage);
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

                ArmyType at = game.getOccupyingArmy(w.territory);

                if (at == ArmyType.RED) {
                    if (w.territory == game.red.leader1.territory || w.territory == game.red.leader2.territory) {
                        renderer.getBatch().draw(LEADER_CIRCLE, w.battalionPosition.x - 0, w.battalionPosition.y - 10);
                    }
                    renderer.getBatch().draw(RED_CIRCLE, w.textPosition.x - 8, w.textPosition.y - 17);
                }
                if (at == ArmyType.BLACK) {
                    if (w.territory == game.black.leader1.territory || w.territory == game.black.leader2.territory) {
                        renderer.getBatch().draw(LEADER_CIRCLE, w.battalionPosition.x - 0, w.battalionPosition.y - 10);
                    }
                    renderer.getBatch().draw(BLACK_CIRCLE, w.textPosition.x - 8, w.textPosition.y - 17);
                }
                if (at == ArmyType.GREEN) {
                    if (w.territory == game.green.leader1.territory || w.territory == game.green.leader2.territory) {
                        renderer.getBatch().draw(LEADER_CIRCLE, w.battalionPosition.x - 0, w.battalionPosition.y - 10);
                    }
                    renderer.getBatch().draw(GREEN_CIRCLE, w.textPosition.x - 8, w.textPosition.y - 17);
                }
                if (at == ArmyType.YELLOW) {
                    if (w.territory == game.yellow.leader1.territory || w.territory == game.yellow.leader2.territory) {
                        renderer.getBatch().draw(LEADER_CIRCLE, w.battalionPosition.x - 0, w.battalionPosition.y - 10);
                    }
                    renderer.getBatch().draw(YELLOW_CIRCLE, w.textPosition.x - 8, w.textPosition.y - 17);
                }

                int bc = game.battalionCount(w.territory);
                if (bc > 0) {
                    Risk.font.draw(renderer.getBatch(), bc + "", w.textPosition.x + 0, w.textPosition.y + 0);
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

        this.hudbatch.begin();

        int y = Risk.SCREEN_HEIGHT - 100;

        Risk.font.draw(hudbatch, army.armyType.toString(), 400, Risk.SCREEN_HEIGHT - 50);

        Risk.font.draw(hudbatch, "Stronghold Reinforcements " + strongholdReinforcements, 400, y);
        Risk.font.draw(hudbatch, "Territory Reinforcements " + territoryReinforcements, 400, y - 20);
        Risk.font.draw(hudbatch, "Region Reinforcements " + regionReinforcements, 400, y - 40);
        Risk.font.draw(hudbatch, "Card Reinforcements " + cardReinforcements, 400, y - 60);
        Risk.font.draw(hudbatch, "Cards with Eleven Archers " + sumArchers, 400, y - 100);
        Risk.font.draw(hudbatch, "Cards with Dark Riders " + sumRiders, 400, y - 120);
        Risk.font.draw(hudbatch, "Cards with Eagles " + sumEagles, 400, y - 140);

        this.hudbatch.end();

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
