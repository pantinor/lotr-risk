package lotr;

import lotr.util.Sound;
import lotr.util.Sounds;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.HexagonalTiledMapRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
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

    public static BitmapFont font;
    private final HexagonalTiledMapRenderer renderer;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    private final Game game;
    private final Risk main;

    private final float unitScale = 0.35f;
    private final List<RegionWrapper> regions = new ArrayList<>();
    private RegionWrapper selectedTerritory;

    private final Batch hudbatch = new SpriteBatch();
    private final Batch batch = new SpriteBatch();
    private final Viewport mapViewport;
    private final OrthographicCamera camera;

    private final Stage stage = new Stage();

    private static final int MAP_VIEWPORT_WIDTH = 736;
    private static final int MAP_VIEWPORT_HEIGHT = 968;

    private final TextButton claim, exit;
    private final Table table = new Table();
    private final Label redLabel = new Label("-", Risk.skin);
    private final Label greenLabel = new Label("-", Risk.skin);
    private final Label yellowLabel = new Label("-", Risk.skin);
    private final Label blackLabel = new Label("-", Risk.skin);

    private final Random rand = new Random();

    private final GlyphLayout layout = new GlyphLayout();
    private static final List<String> TEXTS = new ArrayList<>();

    static {
        TEXTS.add("Select a terririty on the map and click on CLAIM during your turn.");
        TEXTS.add("When all territories are claimed, Reinforce your territories in similar way during your turn.");
        TEXTS.add("The Game will begin when all battalions of each player are deployed.");
    }

    public ClaimTerritoryScreen(Risk main, Game game) {

        this.game = game;
        this.main = main;

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.classpath("assets/fonts/aniron.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 10;
        font = generator.generateFont(parameter);

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
                    if (GAME.armies[i] != null && GAME.armies[i].botType != null) {
                        GAME.armies[i].bot.set(gameScreen, gameScreen.ringPath, gameScreen.cardSlider);
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

        RunnableAction closure = new RunnableAction();
        closure.setRunnable(() -> {
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
        });
        this.stage.addAction(closure);

    }

    private Action botClaim() {
        RunnableAction action = new RunnableAction();
        action.setRunnable(() -> {
            Army current = game.current();
            TerritoryCard t = game.findRandomEmptyTerritory(current.getClassType());

            if (t != null) {
                addBattalion(t);
                return;
            }

            List<TerritoryCard> claimed = current.claimedTerritories();

            List<Location> ownedStrongholds = current.ownedStrongholds(claimed);

            List<TerritoryCard> adjacentToEnemyStronghold = new ArrayList<>();
            for (TerritoryCard tr : claimed) {
                for (TerritoryCard adj : tr.adjacents()) {
                    Army owner = game.isClaimed(adj);
                    if (owner != null && owner != current && game.isStronghold(adj)) {
                        adjacentToEnemyStronghold.add(tr);
                        break;
                    }
                }
            }

            boolean flip = rand.nextBoolean();
            if (flip && !adjacentToEnemyStronghold.isEmpty()) {
                t = adjacentToEnemyStronghold.get(rand.nextInt(adjacentToEnemyStronghold.size()));
            } else if (!ownedStrongholds.isEmpty()) {
                Location loc = ownedStrongholds.get(rand.nextInt(ownedStrongholds.size()));
                t = loc.getTerritory();
            } else {
                t = claimed.get(rand.nextInt(claimed.size()));
            }

            addBattalion(t);

            if (current.leader1.territory == null) {
                current.leader1.territory = t;
            }
            if (current.leader2.territory == null) {
                claimed.remove(current.leader1.territory);
                current.leader2.territory = claimed.get(rand.nextInt(claimed.size()));
            }
        });

        return action;
    }

    @Override
    public void show() {
        new NewGameDialog(this.game, this).show(stage);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    Vector3 tmpt = new Vector3();

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderer.render();

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        if (selectedTerritory != null) {
            filledPolygon(shapeRenderer, new Color(0x00ff0080), selectedTerritory.vertices);
        }

        hudbatch.begin();

        for (RegionWrapper w : regions) {
            if (w.territory == null) {
                continue;
            }

            TerritoryCard territory = w.territory;
            Army occupyingArmy = game.getOccupyingArmy(territory);
            ArmyType armyType = occupyingArmy != null ? occupyingArmy.armyType : null;

            tmpt.set(w.textPosition);

            this.camera.project(tmpt);

            float tx = tmpt.x - 12;
            float ty = tmpt.y - 12;

            // Draw territory name
            // Draw army indicator and leader icon
            Texture armyCircle = null;
            boolean isLeader = false;
            Color color = Color.WHITE;

            if (armyType == ArmyType.RED) {
                armyCircle = RED_CIRCLE;
                color = Color.RED;
                isLeader = (territory == game.red.leader1.territory || territory == game.red.leader2.territory);
            } else if (armyType == ArmyType.BLACK) {
                armyCircle = BLACK_CIRCLE;
                color = Color.DARK_GRAY;
                isLeader = (territory == game.black.leader1.territory || territory == game.black.leader2.territory);
            } else if (armyType == ArmyType.GREEN) {
                armyCircle = GREEN_CIRCLE;
                color = Color.GREEN;
                isLeader = (territory == game.green.leader1.territory || territory == game.green.leader2.territory);
            } else if (armyType == ArmyType.YELLOW) {
                armyCircle = YELLOW_CIRCLE;
                color = Color.YELLOW;
                isLeader = (territory == game.yellow.leader1.territory || territory == game.yellow.leader2.territory);
            }

            font.setColor(armyType == ArmyType.BLACK ? Color.BLUE : Color.BLACK);
            font.draw(hudbatch, territory.title(), tx - 25 + 1, ty + 30 - 1);
            font.setColor(color);
            font.draw(hudbatch, territory.title(), tx - 25, ty + 30);

            if (armyCircle != null) {
                if (isLeader) {
                    hudbatch.draw(LEADER_CIRCLE, tx - 5, ty - 5);
                }
                hudbatch.draw(armyCircle, tx, ty);
            }

            int battalionCount = game.battalionCount(territory);
            if (battalionCount > 0) {
                Risk.defaultFont.draw(hudbatch, Integer.toString(battalionCount), tx + 3, ty + 18);
            }
        }

        int x = 15;
        int y = Gdx.graphics.getHeight() - 350;

        for (String text : TEXTS) {
            layout.setText(Risk.font, text, Color.WHITE, 320, Align.left, true);
            Risk.font.draw(hudbatch, layout, 15, y);
            y -= layout.height + 30;
        }

        hudbatch.end();

        stage.act();
        stage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && this.claim.isVisible()) {
            this.claim.fire(new ChangeListener.ChangeEvent());
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
