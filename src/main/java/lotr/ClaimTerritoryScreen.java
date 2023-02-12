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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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
import static lotr.Risk.GREY_BATTALION;
import static lotr.Risk.GREY_LEADER;
import static lotr.Risk.LEADER_CIRCLE;
import static lotr.Risk.RED_BATTALION;
import static lotr.Risk.RED_LEADER;
import lotr.Risk.RegionWrapper;
import static lotr.Risk.TEXT_CIRCLE;
import static lotr.Risk.TMX_MAP;
import static lotr.Risk.YELLOW_BATTALION;
import static lotr.Risk.YELLOW_LEADER;

public class ClaimTerritoryScreen implements Screen {

    private final HexagonalTiledMapRenderer renderer;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

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

    private final TextButton claim, exit, auto;
    private final Table table = new Table();
    private final Label redLabel = new Label("-", Risk.skin);
    private final Label greenLabel = new Label("-", Risk.skin);
    private final Label yellowLabel = new Label("-", Risk.skin);
    private final Label greyLabel = new Label("-", Risk.skin);

    private int turnIndex = 0;
    private Random rand = new Random();

    public ClaimTerritoryScreen(Game game) {

        this.game = game;

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
        sp.setBounds(350, 700, 300, 200);
        this.stage.addActor(sp);

        this.claim = new TextButton("CLAIM", Risk.skin);
        this.claim.setVisible(false);
        this.claim.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                boolean foundSelected = false;
                for (RegionWrapper w : regions) {
                    if (w.selected) {
                        foundSelected = true;
                        if (w.territory.battalions.isEmpty()) {

                            addBattalion(w, false);

                        } else if (claim.getText().toString().equals("REINFORCE")) {

                            if (w.territory.battalions.get(0).army.armyType == game.armies.get(turnIndex).armyType) {
                                addBattalion(w, false);
                            } else {
                                Sounds.play(Sound.NEGATIVE_EFFECT);
                            }

                        } else if (claim.getText().toString().equals("PLACE LEADERS")) {

                            if (w.territory.battalions.get(0).army.armyType == game.armies.get(turnIndex).armyType && w.territory.leader == null) {
                                int size = TerritoryCard.getClaimedTerritoriesWithLeaders(game.armies.get(turnIndex).armyType).size();
                                if (size == 0) {
                                    w.territory.leader = game.armies.get(turnIndex).leader1;
                                } else if (size == 1) {
                                    w.territory.leader = game.armies.get(turnIndex).leader2;
                                } else {
                                    Sounds.play(Sound.BLOCKED);
                                }
                                addBattalion(w, false); //just to advance the next player
                            } else {
                                Sounds.play(Sound.NEGATIVE_EFFECT);
                            }

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
                                //System.out.printf("%s\n", w.name);
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

    public void initArmies() {
        armyCell(this.table, ArmyType.RED, redLabel);
        armyCell(this.table, ArmyType.GREY, greyLabel);
        armyCell(this.table, ArmyType.GREEN, greenLabel);
        armyCell(this.table, ArmyType.YELLOW, yellowLabel);

        this.turnIndex = rand.nextInt(this.game.armies.size());

        setActiveArmy();
    }

    private void armyCell(Table t, ArmyType type, Label label) {
        Image image = null;
        if (type == ArmyType.RED) {
            image = new Image(RED_BATTALION);
        }
        if (type == ArmyType.GREEN) {
            image = new Image(GREEN_BATTALION);
        }
        if (type == ArmyType.GREY) {
            image = new Image(GREY_BATTALION);
        }
        if (type == ArmyType.YELLOW) {
            image = new Image(YELLOW_BATTALION);
        }
        t.add(image).left().pad(5);
        t.add(label).left().pad(5).expandX();
        t.row();
    }

    private void setActiveArmy() {

        int rlc = TerritoryCard.getClaimedTerritoriesWithLeaders(ArmyType.RED).size();
        int glc = TerritoryCard.getClaimedTerritoriesWithLeaders(ArmyType.GREY).size();
        int grlc = TerritoryCard.getClaimedTerritoriesWithLeaders(ArmyType.GREEN).size();
        int ylc = TerritoryCard.getClaimedTerritoriesWithLeaders(ArmyType.YELLOW).size();

        redLabel.setText(this.game.red != null ? "Battalions: " + this.game.red.battalions.size() + " Leaders: " + rlc : "-");
        greenLabel.setText(this.game.green != null ? "Battalions: " + this.game.green.battalions.size() + " Leaders: " + grlc : "-");
        greyLabel.setText(this.game.grey != null ? "Battalions: " + this.game.grey.battalions.size() + " Leaders: " + glc : "-");
        yellowLabel.setText(this.game.yellow != null ? "Battalions: " + this.game.yellow.battalions.size() + " Leaders: " + ylc : "-");

        redLabel.setStyle(Risk.skin.get("default", Label.LabelStyle.class));
        greenLabel.setStyle(Risk.skin.get("default", Label.LabelStyle.class));
        greyLabel.setStyle(Risk.skin.get("default", Label.LabelStyle.class));
        yellowLabel.setStyle(Risk.skin.get("default", Label.LabelStyle.class));

        Army a = this.game.armies.get(turnIndex);

        if (a.armyType == ArmyType.RED) {
            redLabel.setStyle(Risk.skin.get("default-blue", Label.LabelStyle.class));
        }
        if (a.armyType == ArmyType.GREEN) {
            greenLabel.setStyle(Risk.skin.get("default-blue", Label.LabelStyle.class));
        }
        if (a.armyType == ArmyType.GREY) {
            greyLabel.setStyle(Risk.skin.get("default-blue", Label.LabelStyle.class));
        }
        if (a.armyType == ArmyType.YELLOW) {
            yellowLabel.setStyle(Risk.skin.get("default-blue", Label.LabelStyle.class));
        }

        claim.setVisible(true);

        boolean foundEmpty = false;
        for (RegionWrapper w : regions) {
            w.selected = false;
            if (w.territory.battalions.isEmpty()) {
                foundEmpty = true;
            }
        }
        if (!foundEmpty && this.claim.getText().toString().equals("CLAIM")) {
            this.claim.setText("REINFORCE");
            this.auto.setVisible(true);
        }
    }

    private void addBattalion(RegionWrapper w, boolean isAuto) {

        if (!game.armies.get(turnIndex).battalions.isEmpty()) {
            Battalion b = game.armies.get(turnIndex).battalions.remove(0);
            w.territory.battalions.add(b);
            if (!isAuto) {
                Sounds.play(Sound.TRIGGER);
            }
        }

        claim.setVisible(false);

        turnIndex++;
        if (turnIndex >= game.armies.size()) {
            turnIndex = 0;
        }

        setActiveArmy();

        boolean reinforcedone = true;
        boolean leaderPlacementDone = true;
        for (Army a : this.game.armies) {
            if (a != null) {
                if (!a.battalions.isEmpty()) {
                    reinforcedone = false;
                }
                if (TerritoryCard.getClaimedTerritoriesWithLeaders(a.armyType).size() != 2) {
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

            Army army = this.game.armies.get(turnIndex);

            boolean done = true;
            for (Army a : this.game.armies) {
                if (a != null && !a.battalions.isEmpty()) {
                    done = false;
                }
            }

            if (done) {
                break;
            }

            List<Territory> terrs = TerritoryCard.getClaimedTerritories(army.armyType);
            Territory t = terrs.get(rand.nextInt(terrs.size()));

            RegionWrapper found = null;
            for (RegionWrapper w : regions) {
                if (w.territory == t) {
                    found = w;
                    break;
                }
            }

            addBattalion(found, true);
        }

        Sounds.play(Sound.DIVINE_INTERVENTION);
        this.auto.setVisible(false);
    }

    @Override
    public void show() {
        new PlayerSelectionDialog(this.game, this).show(stage);
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderer.render();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

        for (RegionWrapper w : regions) {

            Gdx.gl.glLineWidth(w.selected ? 6 : 3);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(w.selected ? Color.RED : Color.LIGHT_GRAY);
            shapeRenderer.polygon(w.vertices);
            shapeRenderer.end();

            if (w.territory != null && !w.territory.battalions.isEmpty()) {
                renderer.getBatch().begin();

                if (w.territory.battalions.get(0).army.armyType == ArmyType.RED) {
                    renderer.getBatch().draw(RED_BATTALION, w.redPosition.x, w.redPosition.y);
                    if (w.territory.leader != null) {
                        renderer.getBatch().draw(RED_LEADER, w.redPosition.x - 10, w.redPosition.y - 10);
                    }
                }
                if (w.territory.battalions.get(0).army.armyType == ArmyType.GREY) {
                    renderer.getBatch().draw(GREY_BATTALION, w.greyPosition.x, w.greyPosition.y);
                    if (w.territory.leader != null) {
                        renderer.getBatch().draw(GREY_LEADER, w.greyPosition.x - 10, w.greyPosition.y - 10);
                    }
                }
                if (w.territory.battalions.get(0).army.armyType == ArmyType.GREEN) {
                    renderer.getBatch().draw(GREEN_BATTALION, w.greenPosition.x, w.greenPosition.y);
                    if (w.territory.leader != null) {
                        renderer.getBatch().draw(GREEN_LEADER, w.greenPosition.x - 10, w.greenPosition.y - 10);
                    }
                }
                if (w.territory.battalions.get(0).army.armyType == ArmyType.YELLOW) {
                    renderer.getBatch().draw(YELLOW_BATTALION, w.yellowPosition.x, w.yellowPosition.y);
                    if (w.territory.leader != null) {
                        renderer.getBatch().draw(YELLOW_LEADER, w.yellowPosition.x - 10, w.yellowPosition.y - 10);
                    }
                }

                renderer.getBatch().draw(w.territory.leader != null ? LEADER_CIRCLE : TEXT_CIRCLE, w.textPosition.x - 6, w.textPosition.y - 10);
                Risk.fontSmall.draw(renderer.getBatch(), w.territory.battalions.size() + "", w.textPosition.x + 3, w.textPosition.y + 8);

                renderer.getBatch().end();
            }

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
