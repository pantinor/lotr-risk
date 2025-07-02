package lotr;

import lotr.util.Sound;
import lotr.util.Sounds;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import lotr.Constants.ArmyType;
import lotr.Constants.BattalionType;
import lotr.Risk.RegionWrapper;
import static lotr.Risk.TMX_MAP;
import static lotr.Risk.GREEN_CIRCLE;
import static lotr.Risk.BLACK_CIRCLE;
import static lotr.Risk.LEADER_CIRCLE;
import static lotr.Risk.RED_CIRCLE;
import static lotr.Risk.YELLOW_CIRCLE;
import static lotr.util.RendererUtil.filledPolygon;

public class ReinforceScreen implements Screen {

    protected float time = 0;
    private final HexagonalTiledMapRenderer renderer;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    private final Game game;
    private final Risk main;
    private final Army army;
    private final GameScreen gameScreen;
    private final TurnWidget turnWidget;

    private final float unitScale = 0.35f;
    private final List<RegionWrapper> regions = new ArrayList<>();
    private RegionWrapper selectedTerritory;

    private final SpriteBatch hudbatch = new SpriteBatch();
    private final SpriteBatch batch = new SpriteBatch();

    private final Viewport mapViewport;
    private final OrthographicCamera camera;

    private final Viewport viewport = new ScreenViewport();
    private final Stage stage = new Stage(viewport);

    private static final int MAP_VIEWPORT_WIDTH = 736;
    private static final int MAP_VIEWPORT_HEIGHT = 968;

    private final Table table = new Table();

    private final TextButton reinforce, exit;

    private final List<TerritoryCard> claimedTerritories;
    private final List<Location> strongholds;
    private final List<Region> ownedRegions = new ArrayList<>();

    private int strongholdReinforcements, territoryReinforcements, regionReinforcements, cardReinforcements;
    private int sumArchers = 0, sumRiders = 0, sumEagles = 0, wildcards = 0, wildcardsUsed = 0;

    private final GlyphLayout layout = new GlyphLayout();
    private static final List<String> TEXTS = new ArrayList<>();

    static {
        TEXTS.add("1. Reinforce Strongholds - Place 1 battalion into each territory with a stronghold you control.");
        TEXTS.add("2. Count your Territories - Divide the total number of territories by 3.  The number of reinforcecments you recive can never be fewer than 3.");
        TEXTS.add("3. Reinforcements from regions - If you control every teritory within the region, then you control the region.");
        TEXTS.add("4. Turn in any card sets - when you have a set of 3 cards that show the same picture or 1 of each picture, turn them in for reinforcements.  This is done automatically for you.");
    }

    public ReinforceScreen(Risk main, Game game, Army army, GameScreen gameScreen, TurnWidget turnWidget) {

        this.game = game;
        this.main = main;
        this.army = army;
        this.gameScreen = gameScreen;
        this.turnWidget = turnWidget;

        this.claimedTerritories = army.claimedTerritories();

        strongholds = army.ownedStrongholds(claimedTerritories);
        strongholdReinforcements = strongholds.size();
        territoryReinforcements = claimedTerritories.size() / 3 < 3 ? 3 : claimedTerritories.size() / 3;

        for (Region r : Region.values()) {
            if (claimedTerritories.containsAll(r.territories())) {
                regionReinforcements += r.reinforcements();
                ownedRegions.add(r);
            }
        }

        // Count battalion types
        for (TerritoryCard c : army.territoryCards) {
            BattalionType type = c.battalionType();
            if (type == BattalionType.ELVEN_ARCHER) {
                sumArchers++;
            } else if (type == BattalionType.DARK_RIDER) {
                sumRiders++;
            } else if (type == BattalionType.EAGLE) {
                sumEagles++;
            } else if (type == null) {
                wildcards++;
            }
        }

        cardReinforcements = 0;

        /**
         * Calculates the number of reinforcement battalions awarded based on
         * the player's current set of Territory cards. Reinforcements are
         * granted by turning in a valid set of cards, with priority given to
         * the most valuable combination.
         * <p>
         * The player may redeem:
         * <ul>
         * <li><b>10 battalions</b> for a mixed set: one Elven Archer, one Dark
         * Rider, and one Eagle.</li>
         * <li><b>8 battalions</b> for a set of three Eagles.</li>
         * <li><b>6 battalions</b> for a set of three Dark Riders.</li>
         * <li><b>4 battalions</b> for a set of three Elven Archers.</li>
         * </ul>
         * Wildcards can substitute for any missing card type in a set.
         * <p>
         * The logic first attempts to complete a mixed set using wildcards if
         * needed. If a mixed set is not possible, it checks for matching sets
         * of 3 of the same type (including wildcard substitutions), in
         * descending order of value.
         */
        int neededMixed = Math.max(0, 1 - sumArchers) + Math.max(0, 1 - sumRiders) + Math.max(0, 1 - sumEagles);
        if (wildcards >= neededMixed) {
            cardReinforcements = 10;
            wildcardsUsed = neededMixed;
        } else if (sumEagles + wildcards >= 3) {
            int needed = Math.max(0, 3 - sumEagles);
            cardReinforcements = 8;
            wildcardsUsed = Math.min(needed, wildcards);
        } else if (sumRiders + wildcards >= 3) {
            int needed = Math.max(0, 3 - sumRiders);
            cardReinforcements = 6;
            wildcardsUsed = Math.min(needed, wildcards);
        } else if (sumArchers + wildcards >= 3) {
            int needed = Math.max(0, 3 - sumArchers);
            cardReinforcements = 4;
            wildcardsUsed = Math.min(needed, wildcards);
        }

        gameScreen.logs.log(String.format("%s reinforcements: %d (stronghold) + %d (territory) + %d (region) + %d (cards)",
                army.armyType, strongholdReinforcements, territoryReinforcements, regionReinforcements, cardReinforcements), army.armyType.color());

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

        table.align(Align.left | Align.top).pad(5);
        table.columnDefaults(0).expandX().left().uniformX();
        table.columnDefaults(1).expandX().left().uniformX();

        ScrollPane sp = new ScrollPane(table, Risk.skin);
        sp.setBounds(400, 50, 280, 500);
        this.stage.addActor(sp);

        for (TerritoryCard c : army.territoryCards) {
            String bt = c.battalionType() == null ? "ANY" : c.battalionType().toString().replace("_", " ");
            this.table.add(new Label(c.title() + " - " + bt, Risk.skin));
            this.table.row();
        }

        if (cardReinforcements > 0) {
            game.turnInTerritoryCards(army, sumArchers, sumRiders, sumEagles, wildcardsUsed);
        }

        this.reinforce = new TextButton("REINFORCE", Risk.skin, "blue");

        this.reinforce.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {

                if (selectedTerritory == null) {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    return;
                }

                TerritoryCard target = selectedTerritory.territory;

                if (!claimedTerritories.contains(target)) {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    return;
                }

                if (strongholdReinforcements > 0) {
                    for (Location l : strongholds) {
                        army.addBattalion(l.getTerritory());
                        strongholdReinforcements--;
                    }
                } else if (territoryReinforcements > 0) {
                    army.addBattalion(target);
                    Sounds.play(Sound.TRIGGER);
                    territoryReinforcements--;
                } else if (regionReinforcements > 0) {
                    army.addBattalion(target);
                    Sounds.play(Sound.TRIGGER);
                    regionReinforcements--;
                } else if (cardReinforcements > 0) {
                    army.addBattalion(target);
                    Sounds.play(Sound.TRIGGER);
                    cardReinforcements--;
                } else {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                }
            }
        });

        this.exit = new TextButton("DONE", Risk.skin, "blue");
        this.exit.setVisible(false);
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                ReinforceScreen.this.gameScreen.lookAt(selectedTerritory.territory);
                main.setScreen(ReinforceScreen.this.gameScreen);
            }
        });

        this.reinforce.setBounds(400, 600, 220, 35);
        this.exit.setBounds(400, 560, 220, 35);

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
                                selectedTerritory = w;
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
                    Risk.defaultFont.draw(hudbatch, bc + "", tp.x - 8, tp.y + 6);
                }

            }
        }

        int x = 15;
        int y = Gdx.graphics.getHeight() - 15;

        for (String text : TEXTS) {
            layout.setText(Risk.font, text, Color.WHITE, 320, Align.left, true);
            Risk.font.draw(hudbatch, layout, 15, y);
            y -= layout.height + 30;
        }

        Risk.font.draw(hudbatch, army.armyType.toString(), x, y -= 20);

        Risk.font.draw(hudbatch, "Stronghold Reinforcements " + strongholdReinforcements, x, y -= 30);
        Risk.font.draw(hudbatch, "Territory Reinforcements " + territoryReinforcements, x, y -= 20);
        Risk.font.draw(hudbatch, "Region Reinforcements " + regionReinforcements, x, y -= 20);
        Risk.font.draw(hudbatch, "Card Reinforcements " + cardReinforcements, x, y -= 20);

        Risk.font.draw(hudbatch, "Strongholds", x, y -= 40);
        for (Location s : strongholds) {
            Risk.font.draw(hudbatch, "    " + s.title(), x, y -= 20);
        }
        Risk.font.draw(hudbatch, "Regions", x, y -= 40);
        for (Region r : ownedRegions) {
            Risk.font.draw(hudbatch, "    " + r, x, y -= 20);
        }

        Risk.font.draw(hudbatch, "Cards with Eleven Archers " + sumArchers, x, y -= 40);
        Risk.font.draw(hudbatch, "Cards with Dark Riders " + sumRiders, x, y -= 20);
        Risk.font.draw(hudbatch, "Cards with Eagles " + sumEagles, x, y -= 20);

        this.hudbatch.end();

        stage.act();
        stage.draw();

        if (strongholdReinforcements == 0 && territoryReinforcements == 0 && regionReinforcements == 0 && cardReinforcements == 0) {
            this.reinforce.setVisible(false);
            this.exit.setVisible(true);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && this.reinforce.isVisible()) {
            this.reinforce.fire(new ChangeListener.ChangeEvent());
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
