package lotr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
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
import static lotr.Risk.CLASSPTH_RSLVR;
import lotr.Risk.RegionWrapper;

public class ClaimTerritoryScreen implements Screen {

    private final HexagonalTiledMapRenderer renderer;
    private final TiledMap tmxMap;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    TextureRegion redIcon, greyIcon, greenIcon, yellowIcon;
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

        TmxMapLoader loader = new TmxMapLoader(CLASSPTH_RSLVR);
        this.tmxMap = loader.load("assets/data/map.tmx");

        this.renderer = new HexagonalTiledMapRenderer(this.tmxMap, this.unitScale, this.batch);
        this.renderer.setView(this.camera);

        TiledMapTileSet tileset = this.tmxMap.getTileSets().getTileSet("monsters");
        int firstgid = tileset.getProperties().get("firstgid", Integer.class);

        redIcon = tileset.getTile(firstgid + 82).getTextureRegion();
        greyIcon = tileset.getTile(firstgid + 237).getTextureRegion();
        greenIcon = tileset.getTile(firstgid + 245).getTextureRegion();
        yellowIcon = tileset.getTile(firstgid + 1143).getTextureRegion();

        MapLayer regionsLayer = this.tmxMap.getLayers().get("regions");
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

        TiledMapTileLayer iconLayer = (TiledMapTileLayer) this.tmxMap.getLayers().get("icons");
        Risk.setPoints(iconLayer, regions, unitScale);

        this.table.align(Align.left | Align.top).pad(1);
        this.table.columnDefaults(0).expandX().left().uniformX();
        this.table.columnDefaults(1).expandX().left().uniformX();

        ScrollPane sp = new ScrollPane(table, Risk.skin);
        sp.setBounds(100, 600, 300, 400);
        this.stage.addActor(sp);

        this.claim = new TextButton("CLAIM", Risk.skin, "red-larger");
        this.claim.setVisible(false);
        this.claim.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {

                for (RegionWrapper w : regions) {
                    if (w.selected) {
                        if (w.territory.battalions.isEmpty()) {
                            addBattalion(w);
                        } else if (claim.getText().toString().equals("REINFORCE")) {
                            if (w.territory.battalions.get(0).army.armyType == game.armies[turnIndex].armyType) {
                                addBattalion(w);
                            }
                        }
                        break;
                    }
                }

            }
        });
        this.claim.setBounds(260, 500, 100, 40);

        this.exit = new TextButton("EXIT", Risk.skin, "red-larger");
        this.exit.setVisible(false);
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {

            }
        });
        this.exit.setBounds(150, 500, 100, 40);

        this.auto = new TextButton("AUTO REINFORCE", Risk.skin, "red-larger");
        this.auto.setVisible(false);
        this.auto.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                autoReinforce();
            }
        });
        this.auto.setBounds(370, 500, 200, 40);

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
        this.table.add(armyCell(ArmyType.RED, redLabel));
        this.table.add(armyCell(ArmyType.GREY, greyLabel));
        this.table.row();
        this.table.add(armyCell(ArmyType.GREEN, greenLabel));
        this.table.add(armyCell(ArmyType.YELLOW, yellowLabel));
        this.table.row();

        this.turnIndex = rand.nextInt(4);
        if (this.game.armies[turnIndex] == null) {
            this.turnIndex++;
        }
        if (this.turnIndex >= this.game.armies.length) {
            this.turnIndex = 0;
        }

        setActiveArmy();
    }

    private void setActiveArmy() {
        redLabel.setText(this.game.red != null ? this.game.red.battalions.size() + "" : "-");
        greenLabel.setText(this.game.green != null ? this.game.green.battalions.size() + "" : "-");
        greyLabel.setText(this.game.grey != null ? this.game.grey.battalions.size() + "" : "-");
        yellowLabel.setText(this.game.yellow != null ? this.game.yellow.battalions.size() + "" : "-");

        redLabel.setStyle(Risk.skin.get("default", Label.LabelStyle.class));
        greenLabel.setStyle(Risk.skin.get("default", Label.LabelStyle.class));
        greyLabel.setStyle(Risk.skin.get("default", Label.LabelStyle.class));
        yellowLabel.setStyle(Risk.skin.get("default", Label.LabelStyle.class));

        Army a = this.game.armies[turnIndex];

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
        if (!foundEmpty) {
            this.claim.setText("REINFORCE");
            this.auto.setVisible(true);
        }
    }

    private Table armyCell(ArmyType type, Label label) {
        Table t = new Table();
        t.columnDefaults(0).expandX().left().uniformX();
        t.align(Align.left | Align.top).pad(1);
        Label l = new Label(type + "", Risk.skin);

        if (type == ArmyType.RED) {
            l.setStyle(Risk.skin.get("default-red", Label.LabelStyle.class));
        }
        if (type == ArmyType.GREEN) {
            l.setStyle(Risk.skin.get("default-green", Label.LabelStyle.class));
        }
        if (type == ArmyType.GREY) {
            l.setStyle(Risk.skin.get("default", Label.LabelStyle.class));
        }
        if (type == ArmyType.YELLOW) {
            l.setStyle(Risk.skin.get("default-yellow", Label.LabelStyle.class));
        }

        t.add(l);
        t.row();
        t.add(label);
        return t;
    }

    private void addBattalion(RegionWrapper w) {

        if (!game.armies[turnIndex].battalions.isEmpty()) {
            Battalion b = game.armies[turnIndex].battalions.remove(0);
            w.territory.battalions.add(b);
        }

        claim.setVisible(false);

        turnIndex++;
        if (turnIndex >= game.armies.length) {
            turnIndex = 0;
        }
        if (game.armies[turnIndex] == null) {
            turnIndex++;
        }
        if (turnIndex >= game.armies.length) {
            turnIndex = 0;
        }
        setActiveArmy();

        boolean done = true;
        for (Army a : this.game.armies) {
            if (a != null) {
                if (!a.battalions.isEmpty()) {
                    done = false;
                }
            }
        }
        if (done) {
            this.exit.setVisible(true);
            this.claim.setVisible(false);
        }
    }

    private void autoReinforce() {
        while (true) {

            Army army = this.game.armies[turnIndex];

            boolean done = true;
            for (Army a : this.game.armies) {
                if (a != null && !a.battalions.isEmpty()) {
                    done = false;
                }
            }
            if (done) {
                break;
            }

            turnIndex++;
            if (turnIndex >= game.armies.length) {
                turnIndex = 0;
            }
            if (game.armies[turnIndex] == null) {
                turnIndex++;
            }
            if (turnIndex >= game.armies.length) {
                turnIndex = 0;
            }

            if (army.battalions.isEmpty()) {
                continue;
            }

            Battalion b = army.battalions.remove(0);
            List<Territory> terrs = TerritoryCard.getClaimedTerritories(army.armyType);
            Territory t = terrs.get(rand.nextInt(terrs.size()));
            t.battalions.add(b);

        }

        this.exit.setVisible(true);
        this.claim.setVisible(false);
        this.auto.setVisible(false);

        redLabel.setText(this.game.red != null ? this.game.red.battalions.size() + "" : "-");
        greenLabel.setText(this.game.green != null ? this.game.green.battalions.size() + "" : "-");
        greyLabel.setText(this.game.grey != null ? this.game.grey.battalions.size() + "" : "-");
        yellowLabel.setText(this.game.yellow != null ? this.game.yellow.battalions.size() + "" : "-");

        redLabel.setStyle(Risk.skin.get("default", Label.LabelStyle.class));
        greenLabel.setStyle(Risk.skin.get("default", Label.LabelStyle.class));
        greyLabel.setStyle(Risk.skin.get("default", Label.LabelStyle.class));
        yellowLabel.setStyle(Risk.skin.get("default", Label.LabelStyle.class));

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

        Gdx.gl.glLineWidth(3);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

        for (RegionWrapper w : regions) {

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(w.selected ? Color.RED : Color.YELLOW);
            shapeRenderer.polygon(w.vertices);
            shapeRenderer.end();

            if (w.territory != null && !w.territory.battalions.isEmpty()) {
                renderer.getBatch().begin();
                if (w.territory.battalions.get(0).army.armyType == ArmyType.RED) {
                    renderer.getBatch().draw(redIcon, w.redPosition.x, w.redPosition.y);
                    Risk.font.draw(renderer.getBatch(), w.territory.battalions.size() + "", w.redPosition.x + 8, w.redPosition.y + 8);
                }
                if (w.territory.battalions.get(0).army.armyType == ArmyType.GREY) {
                    renderer.getBatch().draw(greyIcon, w.greyPosition.x, w.greyPosition.y);
                    Risk.font.draw(renderer.getBatch(), w.territory.battalions.size() + "", w.greyPosition.x + 8, w.greyPosition.y + 8);
                }
                if (w.territory.battalions.get(0).army.armyType == ArmyType.GREEN) {
                    renderer.getBatch().draw(greenIcon, w.greenPosition.x, w.greenPosition.y);
                    Risk.font.draw(renderer.getBatch(), w.territory.battalions.size() + "", w.greenPosition.x + 8, w.greenPosition.y + 8);
                }
                if (w.territory.battalions.get(0).army.armyType == ArmyType.YELLOW) {
                    renderer.getBatch().draw(yellowIcon, w.yellowPosition.x, w.yellowPosition.y);
                    Risk.font.draw(renderer.getBatch(), w.territory.battalions.size() + "", w.yellowPosition.x + 8, w.yellowPosition.y + 8);
                }
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
