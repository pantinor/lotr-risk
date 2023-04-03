package lotr;

import lotr.util.Sound;
import lotr.util.Sounds;
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
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import lotr.Constants.ArmyType;
import static lotr.Risk.GREEN_BATTALION;
import static lotr.Risk.BLACK_BATTALION;
import static lotr.Risk.BLACK_CIRCLE;
import static lotr.Risk.GAME;
import static lotr.Risk.GREEN_CIRCLE;
import static lotr.Risk.RED_BATTALION;
import lotr.Risk.RegionWrapper;
import static lotr.Risk.TMX_MAP;
import static lotr.Risk.YELLOW_BATTALION;
import static lotr.Risk.LEADER_CIRCLE;
import static lotr.Risk.RED_CIRCLE;
import static lotr.Risk.YELLOW_CIRCLE;
import static lotr.util.RendererUtil.filledPolygon;

public class ClaimTerritoryScreen implements Screen {

    protected float time = 0;
    private final HexagonalTiledMapRenderer renderer;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    private final Game game;
    private final Risk main;

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

    private final TextButton claim, exit;
    private final Table table = new Table();
    private final Label redLabel = new Label("-", Risk.skin);
    private final Label greenLabel = new Label("-", Risk.skin);
    private final Label yellowLabel = new Label("-", Risk.skin);
    private final Label blackLabel = new Label("-", Risk.skin);

    private final Random rand = new Random();

    public ClaimTerritoryScreen(Risk main, Game game) {

        this.game = game;
        this.main = main;

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
        this.table.columnDefaults(0).expandX().left().uniformX();

        ScrollPane sp = new ScrollPane(table, Risk.skin);
        sp.setBounds(300, 700, 300, 225);
        this.stage.addActor(sp);

        this.claim = new TextButton("CLAIM", Risk.skin);
        this.claim.setVisible(false);
        this.claim.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {

                if (selectedTerritory != null) {

                    if (game.isClaimed(selectedTerritory.territory) == null) {
                        addBattalion(selectedTerritory.territory);
                    } else if (claim.getText().toString().equals("REINFORCE")) {

                        if (game.getOccupyingArmy(selectedTerritory.territory).armyType == game.current().armyType) {
                            addBattalion(selectedTerritory.territory);
                        } else {
                            Sounds.play(Sound.NEGATIVE_EFFECT);
                        }

                    } else if (claim.getText().toString().equals("PLACE LEADERS")) {

                        if (game.getOccupyingArmy(selectedTerritory.territory).armyType == game.current().armyType) {
                            if (game.current().leader1.territory == null) {
                                game.current().leader1.territory = selectedTerritory.territory;
                                Sounds.play(Sound.TRIGGER);
                                addBattalion(selectedTerritory.territory); //just to advance the next player
                            } else if (game.current().leader2.territory == null && game.current().leader1.territory != selectedTerritory.territory) {
                                game.current().leader2.territory = selectedTerritory.territory;
                                Sounds.play(Sound.TRIGGER);
                                addBattalion(selectedTerritory.territory); //just to advance the next player
                            } else {
                                Sounds.play(Sound.NEGATIVE_EFFECT);
                            }
                        } else {
                            Sounds.play(Sound.NEGATIVE_EFFECT);
                        }

                    } else {
                        Sounds.play(Sound.NEGATIVE_EFFECT);
                    }
                } else {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                }
            }
        });
        this.claim.setBounds(525, 600, 150, 40);

        this.exit = new TextButton("EXIT", Risk.skin);
        this.exit.setVisible(false);
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {

                List<AdventureCard> adventureCards = AdventureCard.shuffledCardsWithoutEvents();

                //deal 1 territory card and 4 adventure cards to each player
                for (Army a : game.armies) {
                    if (a != null) {
                        TerritoryCard c = game.territoryCards.remove(0);
                        a.addTerritoryCard(c);
                        a.addAdventureCard(adventureCards.remove(0));
                        a.addAdventureCard(adventureCards.remove(0));
                        a.addAdventureCard(adventureCards.remove(0));
                        a.addAdventureCard(adventureCards.remove(0));
                    }
                }

                adventureCards = AdventureCard.shuffledCards();
                for (Army a : game.armies) {
                    if (a != null) {
                        for (AdventureCard c : a.adventureCards) {
                            adventureCards.remove(c);
                        }
                    }
                }

                game.adventureCards.addAll(adventureCards);

                try {
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
                    String json = gson.toJson(game);
                    FileOutputStream fos = new FileOutputStream("savedGame.json");
                    fos.write(json.getBytes("UTF-8"));
                    fos.close();
                } catch (Throwable t) {
                    t.printStackTrace();
                }

                GameScreen gameScreen = new GameScreen(main, game);
                main.setScreen(gameScreen);

                for (int i = 0; i < 4; i++) {
                    if (GAME.armies[i].botType != null) {
                        GAME.armies[i].bot.set(gameScreen.logs, gameScreen.ringPath, gameScreen.missionCardSlider);
                    }
                }

            }
        });
        this.exit.setBounds(525, 600, 150, 40);

        this.stage.addActor(this.claim);
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

    public void init() {

        if (this.game.red != null) {
            armyCell(this.table, ArmyType.RED, redLabel);
        }
        if (this.game.green != null) {
            armyCell(this.table, ArmyType.GREEN, greenLabel);
        }
        if (this.game.black != null) {
            armyCell(this.table, ArmyType.BLACK, blackLabel);
        }
        if (this.game.yellow != null) {
            armyCell(this.table, ArmyType.YELLOW, yellowLabel);
        }

        //roll see who goes first
        this.game.turnIndex = rand.nextInt(4);
        this.game.nextPlayer();

        setActiveArmy();

        if (game.current().isBot()) {
            TerritoryCard t = game.findRandomEmptyTerritory(game.current().getClassType());
            addBattalion(t);
        }
    }

    private void armyCell(Table t, ArmyType type, Label label) {
        if (type == ArmyType.RED) {
            t.add(new Image(RED_CIRCLE)).left().pad(2);
            t.add(new Image(RED_BATTALION.getKeyFrame(0))).left().pad(2);
        }
        if (type == ArmyType.BLACK) {
            t.add(new Image(BLACK_CIRCLE)).left().pad(2);
            t.add(new Image(BLACK_BATTALION.getKeyFrame(0))).left().pad(2);
        }
        if (type == ArmyType.GREEN) {
            t.add(new Image(GREEN_CIRCLE)).left().pad(2);
            t.add(new Image(GREEN_BATTALION.getKeyFrame(0))).left().pad(2);
        }
        if (type == ArmyType.YELLOW) {
            t.add(new Image(YELLOW_CIRCLE)).left().pad(2);
            t.add(new Image(YELLOW_BATTALION.getKeyFrame(0))).left().pad(2);
        }
        t.add(label).left().pad(3).expandX();
        t.row();
    }

    private void setActiveArmy() {

        if (this.game.red != null) {
            int bc = 0;
            for (Battalion b : this.game.red.battalions) {
                if (b.territory == null) {
                    bc++;
                }
            }
            redLabel.setText(this.game.red != null ? "Battalions: " + bc : "-");
            redLabel.setStyle(Risk.skin.get("default", Label.LabelStyle.class));
        }

        if (this.game.black != null) {
            int bc = 0;
            for (Battalion b : this.game.black.battalions) {
                if (b.territory == null) {
                    bc++;
                }
            }
            blackLabel.setText(this.game.black != null ? "Battalions: " + bc : "-");
            blackLabel.setStyle(Risk.skin.get("default", Label.LabelStyle.class));
        }
        if (this.game.green != null) {
            int bc = 0;
            for (Battalion b : this.game.green.battalions) {
                if (b.territory == null) {
                    bc++;
                }
            }
            greenLabel.setText(this.game.green != null ? "Battalions: " + bc : "-");
            greenLabel.setStyle(Risk.skin.get("default", Label.LabelStyle.class));
        }

        if (this.game.yellow != null) {
            int bc = 0;
            for (Battalion b : this.game.yellow.battalions) {
                if (b.territory == null) {
                    bc++;
                }
            }
            yellowLabel.setText(this.game.yellow != null ? "Battalions: " + bc : "-");
            yellowLabel.setStyle(Risk.skin.get("default", Label.LabelStyle.class));
        }

        Army a = this.game.current();

        if (a.armyType == ArmyType.RED) {
            redLabel.setStyle(Risk.skin.get("yellow", Label.LabelStyle.class));
        }
        if (a.armyType == ArmyType.GREEN) {
            greenLabel.setStyle(Risk.skin.get("yellow", Label.LabelStyle.class));
        }
        if (a.armyType == ArmyType.BLACK) {
            blackLabel.setStyle(Risk.skin.get("yellow", Label.LabelStyle.class));
        }
        if (a.armyType == ArmyType.YELLOW) {
            yellowLabel.setStyle(Risk.skin.get("yellow", Label.LabelStyle.class));
        }

        if (!a.isBot()) {
            claim.setVisible(true);
        }

        boolean foundEmpty = false;
        selectedTerritory = null;
        for (RegionWrapper w : regions) {
            if (game.getOccupyingArmy(w.territory) == null) {
                foundEmpty = true;
            }
        }
        if (!foundEmpty && this.claim.getText().toString().equals("CLAIM")) {
            this.claim.setText("REINFORCE");
        }
    }

    private void addBattalion(TerritoryCard territory) {

        game.current().assignTerritory(territory);

        claim.setVisible(false);

        this.game.nextPlayer();

        setActiveArmy();

        if (game.current().isBot()) {
            this.stage.addAction(botClaim());
        }

        boolean reinforcedone = true;
        boolean leaderPlacementDone = true;
        for (Army a : this.game.armies) {
            if (a != null) {
                for (Battalion b : a.battalions) {
                    if (b.territory == null) {
                        reinforcedone = false;
                    }
                }
                if (a.leader1.territory == null || a.leader2.territory == null) {
                    leaderPlacementDone = false;
                }
            }
        }
        if (reinforcedone) {
            if (leaderPlacementDone) {
                this.exit.setVisible(true);
                this.claim.setVisible(false);
            } else {
                this.claim.setText("PLACE LEADERS");
            }
        }
    }

    public SequenceAction botClaim() {

        SequenceAction s = Actions.sequence();

        if (!claim.getText().toString().equals("REINFORCE")) {
            s.addAction(Actions.delay(1));
        }

        RunnableAction r1 = new RunnableAction();
        r1.setRunnable(() -> {
            TerritoryCard t = game.findRandomEmptyTerritory(game.current().getClassType());
            if (t != null) {
                addBattalion(t);
            } else {
                List<TerritoryCard> terrs = game.current().claimedTerritories();
                List<Location> sh = game.current().ownedStrongholds(terrs);
                if (sh.isEmpty()) {
                    t = terrs.get(rand.nextInt(terrs.size()));
                } else {
                    t = sh.get(rand.nextInt(sh.size())).getTerritory();
                }
                if (game.current().leader1.territory == null) {
                    game.current().leader1.territory = t;
                }
                if (game.current().leader2.territory == null) {
                    terrs.remove(game.current().leader1.territory);
                    game.current().leader2.territory = terrs.get(rand.nextInt(terrs.size()));
                }
                addBattalion(t);
            }
        });
        s.addAction(r1);

        return s;
    }

    @Override
    public void show() {
        new NewGameDialog(this.game, this).show(stage);
    }

    @Override
    public void resize(int width, int height) {

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

                ArmyType at = game.getOccupyingArmy(w.territory) != null ? game.getOccupyingArmy(w.territory).armyType : null;

                tmpb.set(w.battalionPosition);
                tmpt.set(w.textPosition);

                Vector3 bp = this.camera.project(tmpb);
                Vector3 tp = this.camera.project(tmpt);

                float bx = bp.x - 12;
                float by = bp.y - 12;
                float tx = tp.x - 12;
                float ty = tp.y - 12;

                Risk.smallFont.draw(hudbatch, w.territory.title(), tp.x - 20, tp.y + 16);

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
