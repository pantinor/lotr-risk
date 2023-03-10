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
import static lotr.Risk.GREEN_CIRCLE;
import static lotr.Risk.RED_BATTALION;
import lotr.Risk.RegionWrapper;
import static lotr.Risk.TMX_MAP;
import static lotr.Risk.YELLOW_BATTALION;
import static lotr.Risk.LEADER_CIRCLE;
import static lotr.Risk.RED_CIRCLE;
import static lotr.Risk.YELLOW_CIRCLE;

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

    private final TextButton claim, exit, auto;
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
                        addBattalion(selectedTerritory, false);
                    } else if (claim.getText().toString().equals("REINFORCE")) {

                        if (game.getOccupyingArmy(selectedTerritory.territory).armyType == game.current().armyType) {
                            addBattalion(selectedTerritory, false);
                        } else {
                            Sounds.play(Sound.NEGATIVE_EFFECT);
                        }

                    } else if (claim.getText().toString().equals("PLACE LEADERS")) {

                        if (game.getOccupyingArmy(selectedTerritory.territory).armyType == game.current().armyType) {
                            if (game.current().leader1.territory == null) {
                                game.current().leader1.territory = selectedTerritory.territory;
                                Sounds.play(Sound.TRIGGER);
                                addBattalion(selectedTerritory, false); //just to advance the next player
                            } else if (game.current().leader2.territory == null && game.current().leader1.territory != selectedTerritory.territory) {
                                game.current().leader2.territory = selectedTerritory.territory;
                                Sounds.play(Sound.TRIGGER);
                                addBattalion(selectedTerritory, false); //just to advance the next player
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

        this.auto = new TextButton("AUTO REINFORCE", Risk.skin);
        this.auto.setVisible(false);
        this.auto.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                autoReinforce();
            }
        });
        this.auto.setBounds(525, 475, 150, 40);

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
                    Gson gson = builder.excludeFieldsWithoutExposeAnnotation().create();
                    String json = gson.toJson(game);
                    FileOutputStream fos = new FileOutputStream("savedGame.json");
                    fos.write(json.getBytes("UTF-8"));
                    fos.close();
                } catch (Throwable t) {
                    t.printStackTrace();
                }

                GameScreen gameScreen = new GameScreen(main, game);
                main.setScreen(gameScreen);

            }
        });
        this.exit.setBounds(525, 600, 150, 40);

        this.stage.addActor(this.claim);
        this.stage.addActor(this.exit);
        this.stage.addActor(this.auto);

        this.stage.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof InputEvent) {
                    InputEvent ev = (InputEvent) event;
                    if (ev.getType() == Type.touchDown && ev.getStageX() > 700) {
                        Vector3 tmp = camera.unproject(new Vector3(ev.getStageX(), ev.getStageY(), 0));
                        //System.out.printf("screen  %f %f    unprojected  %f %f    translated  %f %f\n", ev.getStageX(), ev.getStageY(), tmp.x, tmp.y, tmp.x, MAP_VIEWPORT_HEIGHT - tmp.y - 32);
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

    public void initArmies() {
        armyCell(this.table, ArmyType.RED, redLabel);
        armyCell(this.table, ArmyType.GREEN, greenLabel);
        armyCell(this.table, ArmyType.BLACK, blackLabel);
        if (this.game.yellow != null) {
            armyCell(this.table, ArmyType.YELLOW, yellowLabel);
            this.game.turnIndex = rand.nextInt(4);
        } else {
            this.game.turnIndex = rand.nextInt(3);
        }
        setActiveArmy();
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

        int rlc = this.game.red.leader1.territory != null ? (this.game.red.leader2.territory != null ? 2 : 1) : 0;
        redLabel.setText(this.game.red != null ? "Battalions: " + this.game.red.battalions.size() + " Leaders: " + rlc : "-");
        redLabel.setStyle(Risk.skin.get("default", Label.LabelStyle.class));

        int blc = this.game.black.leader1.territory != null ? (this.game.black.leader2.territory != null ? 2 : 1) : 0;
        blackLabel.setText(this.game.black != null ? "Battalions: " + this.game.black.battalions.size() + " Leaders: " + blc : "-");
        blackLabel.setStyle(Risk.skin.get("default", Label.LabelStyle.class));

        int glc = this.game.green.leader1.territory != null ? (this.game.green.leader2.territory != null ? 2 : 1) : 0;
        greenLabel.setText(this.game.green != null ? "Battalions: " + this.game.green.battalions.size() + " Leaders: " + glc : "-");
        greenLabel.setStyle(Risk.skin.get("default", Label.LabelStyle.class));

        if (this.game.yellow != null) {
            int ylc = this.game.yellow.leader1.territory != null ? (this.game.yellow.leader2.territory != null ? 2 : 1) : 0;
            yellowLabel.setText(this.game.yellow != null ? "Battalions: " + this.game.yellow.battalions.size() + " Leaders: " + ylc : "-");
            yellowLabel.setStyle(Risk.skin.get("default", Label.LabelStyle.class));
        }

        Army a = this.game.current();

        if (a.armyType == ArmyType.RED) {
            redLabel.setStyle(Risk.skin.get("default-yellow", Label.LabelStyle.class));
        }
        if (a.armyType == ArmyType.GREEN) {
            greenLabel.setStyle(Risk.skin.get("default-yellow", Label.LabelStyle.class));
        }
        if (a.armyType == ArmyType.BLACK) {
            blackLabel.setStyle(Risk.skin.get("default-yellow", Label.LabelStyle.class));
        }
        if (a.armyType == ArmyType.YELLOW) {
            yellowLabel.setStyle(Risk.skin.get("default-yellow", Label.LabelStyle.class));
        }

        claim.setVisible(true);

        boolean foundEmpty = false;
        selectedTerritory = null;
        for (RegionWrapper w : regions) {
            if (game.getOccupyingArmy(w.territory) == null) {
                foundEmpty = true;
            }
        }
        if (!foundEmpty && this.claim.getText().toString().equals("CLAIM")) {
            this.claim.setText("REINFORCE");
            this.auto.setVisible(true);
        }
    }

    private void addBattalion(RegionWrapper w, boolean isAuto) {

        boolean assigned = game.current().assignTerritory(w.territory);

        if (assigned && !isAuto) {
            Sounds.play(Sound.TRIGGER);
        }

        claim.setVisible(false);

        this.game.next();

        setActiveArmy();

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

    private void autoReinforce() {

        while (true) {

            Army army = this.game.current();

            boolean done = true;
            for (Army a : this.game.armies) {
                if (a != null) {
                    for (Battalion b : a.battalions) {
                        if (b.territory == null) {
                            done = false;
                        }
                    }
                }
            }

            if (done) {
                break;
            }

            List<TerritoryCard> terrs = army.claimedTerritories();
            TerritoryCard t = terrs.get(rand.nextInt(terrs.size()));
            RegionWrapper found = null;
            for (RegionWrapper w : regions) {
                if (w.territory == t) {
                    found = w;
                    break;
                }
            }

            if (found != null) {
                addBattalion(found, true);
            }
        }

        Sounds.play(Sound.DIVINE_INTERVENTION);
        this.auto.setVisible(false);
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

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

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

        if (selectedTerritory != null) {
            Gdx.gl.glLineWidth(6);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.polygon(selectedTerritory.vertices);
            shapeRenderer.end();
        }

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
