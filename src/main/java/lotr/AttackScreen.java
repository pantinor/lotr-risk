package lotr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.DelayedRemovalArray;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.payne.games.piemenu.AnimatedPieMenu;
import com.payne.games.piemenu.PieMenu;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static lotr.Risk.BLACK_BATTALION;
import static lotr.Risk.BLACK_LEADER;
import static lotr.Risk.GREEN_BATTALION;
import static lotr.Risk.GREEN_LEADER;
import static lotr.Risk.RED_BATTALION;
import static lotr.Risk.RED_LEADER;
import static lotr.Risk.YELLOW_BATTALION;
import static lotr.Risk.YELLOW_LEADER;
import lotr.util.Dice;
import lotr.util.Sound;
import lotr.util.Sounds;

public class AttackScreen implements Screen {

    private float time = 0;
    public static final Dice DICE = new Dice();

    private final Stage stage = new Stage();
    private final Texture frameLeft;
    private final Texture frameRight;

    private final Risk main;
    private final GameScreen parent;
    private final Game game;
    private final Army invader;
    private final Army defender;
    private final TerritoryCard from;
    private final TerritoryCard to;

    private int attackingCount, defendingCount;

    private TextButton attack;

    private Environment environment;
    private PointLight light;
    private ModelBatch modelBatch;
    private PerspectiveCamera camera;
    private CameraInputController inputController;

    private btCollisionConfiguration collisionConfiguration;
    private btCollisionDispatcher dispatcher;
    private btBroadphaseInterface broadphase;
    private btDynamicsWorld collisionWorld;
    private btRigidBody groundBody;
    private btDefaultMotionState groundMotionState;
    private Array<btDefaultMotionState> motionStates = new Array<>();
    private Array<btRigidBody> bodies = new Array<>();
    private Array<BulletBase> allBulletReferences = new Array<>();

    private Array<ModelInstance> instances = new Array<>();
    private ModelInstance ground;
    private final Model groundModel;
    private static final float DICE_DROP_HEIGHT = 10;

    private AnimatedPieMenu reinforceRadial;
    private DelayedRemovalArray<ExplosionTriangle> explosionTriangles = new DelayedRemovalArray<>();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final Vector2 invaderPosition = new Vector2(100, 200), defenderPosition = new Vector2(1400, 200);

    static {
        Bullet.init();
    }

    public AttackScreen(Risk main, GameScreen parent, Game game, Army invader, Army defender,
            TerritoryCard from, TerritoryCard to, int acount, int dcount) {

        this.main = main;
        this.parent = parent;
        this.game = game;
        this.invader = invader;
        this.defender = defender;
        this.from = from;
        this.to = to;
        this.attackingCount = acount;
        this.defendingCount = dcount;

        frameLeft = Risk.fillCircle(invader.armyType.color(), 300);
        frameRight = Risk.fillCircle(defender.armyType.color(), 300);

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.25f, 0.25f, 0.25f, 1f));
        light = new PointLight();
        environment.add(light);
        environment.add(new PointLight().set(1f, 0.8f, 0.6f, -20, 4, 20, 20));

        modelBatch = new ModelBatch();

        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0, 15, -10);
        camera.lookAt(0, 0, 0);
        camera.update();

        inputController = new CameraInputController(camera);
        inputController.rotateLeftKey = inputController.rotateRightKey = inputController.forwardKey = inputController.backwardKey = 0;
        inputController.translateUnits = 30f;

        collisionConfiguration = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfiguration);
        broadphase = new btDbvtBroadphase();
        btSequentialImpulseConstraintSolver solver = new btSequentialImpulseConstraintSolver();
        collisionWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
        collisionWorld.setGravity(new Vector3(0, -60, 0));

        Vector3 tempVector = new Vector3();
        btCollisionShape groundShape = new btBoxShape(tempVector.set(20, 0, 20));
        btRigidBody.btRigidBodyConstructionInfo groundInfo = new btRigidBody.btRigidBodyConstructionInfo(0f, null, groundShape, Vector3.Zero);

        btCollisionShape btboxShape = new btBoxShape(new Vector3(1, 1, 1));
        btboxShape.calculateLocalInertia(1f, tempVector);
        btRigidBody.btRigidBodyConstructionInfo boxInfo = new btRigidBody.btRigidBodyConstructionInfo(1f, null, btboxShape, tempVector);

        ModelBuilder modelBuilder = new ModelBuilder();
        Texture txt = new Texture(Gdx.files.classpath("assets/data/risk-map.png"));
        groundModel = modelBuilder.createRect(20f, 0f, -20f, -20f, 0f, -20f, -20f, 0f, 20f, 20f, 0f, 20f, 0, 1, 0,
                new Material(TextureAttribute.createDiffuse(txt), ColorAttribute.createSpecular(1, 1, 1, 1), FloatAttribute.createShininess(8f)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        ground = new ModelInstance(groundModel);

        groundMotionState = new btDefaultMotionState();
        groundMotionState.setWorldTransform(ground.transform);
        groundBody = new btRigidBody(groundInfo);
        groundBody.setMotionState(groundMotionState);
        collisionWorld.addRigidBody(groundBody);

        //for clean ups of native memory
        allBulletReferences.add(collisionWorld);
        allBulletReferences.add(groundShape);
        allBulletReferences.add(groundInfo);
        allBulletReferences.add(groundMotionState);
        allBulletReferences.add(groundBody);
        allBulletReferences.add(btboxShape);
        allBulletReferences.add(boxInfo);
        allBulletReferences.add(solver);
        allBulletReferences.add(broadphase);
        allBulletReferences.add(dispatcher);
        allBulletReferences.add(collisionConfiguration);

        attack = new TextButton("ROLL", Risk.ccskin, "arcade");
        attack.setBounds(900 - 50, 200, 100, 100);
        attack.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {

                clear();

                List<Integer> rollsInvader = new ArrayList<>();
                List<Integer> rollsDefender = new ArrayList<>();

                for (int i = 1; i <= attackingCount; i++) {
                    int r = DICE.roll();
                    rollsInvader.add(r);
                    addBox(Dice.getRedModel(r - 1), i * 3 + 0, DICE_DROP_HEIGHT, 0, boxInfo);
                }

                for (int i = 1; i <= defendingCount; i++) {
                    int r = DICE.roll();
                    rollsDefender.add(r);
                    addBox(Dice.getBlackModel(r - 1), i * 3 - 10, DICE_DROP_HEIGHT, 0, boxInfo);
                }

                Collections.sort(rollsInvader, Collections.reverseOrder());
                Collections.sort(rollsDefender, Collections.reverseOrder());

                int highestAttacking = rollsInvader.get(0);
                if (hasLeader(invader, from)) {
                    highestAttacking++;
                }

                int highestDefending = rollsDefender.get(0);
                if (hasLeader(defender, to)) {
                    highestDefending++;
                }

                if (isDefendingStrongHold()) {
                    highestDefending++;
                }

                if (highestDefending >= highestAttacking) {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    invader.removeBattalion(from);
                    attackingCount--;
                    if (attackingCount == 0) {
                        attack.setVisible(false);
                    }
                    addExplosion(invaderPosition);
                } else {
                    Sounds.play(Sound.POSITIVE_EFFECT);
                    defender.removeBattalion(to);
                    defendingCount--;
                    if (defendingCount == 0) {
                        attack.setVisible(false);
                    }
                    addExplosion(defenderPosition);
                }

            }
        });

        TextButton done = new TextButton("FINISH", Risk.ccskin, "arcade");
        done.setBounds(900 - 50, 50, 100, 100);
        done.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                int totalDefendingBattalionCount = game.battalionCount(to);
                if (defendingCount == 0 && totalDefendingBattalionCount == 0) {

                    if (hasLeader(invader, from)) {
                        moveLeader(invader, from, to);
                        //TODO check mission card
                    }

                    if (defendingCount == 0 && hasLeader(defender, to)) {
                        removeLeader(defender, to);
                    }

                    int acount = game.battalionCount(from);

                    if (acount > 1) {
                        reinforceRadial.resetSelection();
                        reinforceRadial.clearChildren();
                        for (int i = 1; i < acount; i++) {
                            Label l = new Label(Integer.toString(i), Risk.skin);
                            l.setUserObject(i);
                            reinforceRadial.addActor(l);
                        }
                        reinforceRadial.centerOnMouse();
                        reinforceRadial.animateOpening(.4f);
                    }

                } else {
                    AttackScreen.this.parent.attackingCount = null;
                    AttackScreen.this.parent.selectedDefendingTerritory = null;
                    main.setScreen(AttackScreen.this.parent);
                    AttackScreen.this.parent.turnWidget.clearCombat();
                    dispose();
                }
            }
        });

        this.stage.addActor(done);
        this.stage.addActor(attack);

        PieMenu.PieMenuStyle style = new PieMenu.PieMenuStyle();
        style.backgroundColor = new Color(1, 1, 1, .3f);
        style.selectedColor = new Color(.7f, .3f, .5f, 1);
        style.sliceColor = new Color(0, .7f, 0, 1);
        style.alternateSliceColor = new Color(.7f, 0, 0, 1);

        reinforceRadial = new AnimatedPieMenu(Risk.skin.getRegion("white"), style, 75, .3f, 180, 320);
        reinforceRadial.setInfiniteSelectionRange(true);
        reinforceRadial.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                reinforceRadial.transitionToClosing(.4f);
                int index = reinforceRadial.getSelectedIndex();
                if (!reinforceRadial.isValidIndex(index)) {
                    return;
                }
                Actor child = reinforceRadial.getChild(index);
                Integer reinforceCount = (Integer) child.getUserObject();

                for (Battalion b : invader.getBattalions()) {
                    if (b.territory == from && reinforceCount > 0) {
                        b.territory = to;
                        reinforceCount--;
                    }
                }

                AttackScreen.this.parent.attackingCount = null;
                AttackScreen.this.parent.selectedDefendingTerritory = null;
                main.setScreen(AttackScreen.this.parent);
                AttackScreen.this.parent.turnWidget.clearCombat();
                dispose();

            }
        });

        this.stage.addActor(reinforceRadial);
    }

    private void addBox(Model boxModel, float x, float y, float z, btRigidBody.btRigidBodyConstructionInfo boxInfo) {
        ModelInstance box = new ModelInstance(boxModel);
        instances.add(box);

        box.transform.trn(x, y, z);

        btDefaultMotionState ms = new btDefaultMotionState();
        ms.setWorldTransform(box.transform);
        motionStates.add(ms);

        btRigidBody b = new btRigidBody(boxInfo);
        b.setMotionState(ms);
        bodies.add(b);

        collisionWorld.addRigidBody(b);
    }

    private void addExplosion(Vector2 pos) {
        int n = MathUtils.random(20, 30);
        for (int k = 0; k < n; k++) {
            ExplosionTriangle explosionTriangle = new ExplosionTriangle(shapeRenderer, pos, k * 360 / n);
            explosionTriangles.add(explosionTriangle);
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, inputController));
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, false);
    }

    @Override
    public void render(float delta) {
        time += delta;
        Gdx.gl.glClearColor(0, 0, .62f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        camera.update();

        collisionWorld.stepSimulation(delta, 5);
        groundMotionState.getWorldTransform(ground.transform);
        for (int i = 0; i < motionStates.size; i++) {
            motionStates.get(i).getWorldTransform(instances.get(i).transform);
        }

        float lightSize = 100 + 20 * MathUtils.random();
        light.set(1f, 0.8f, 0.6f, 7, 10, 0, lightSize);

        modelBatch.begin(camera);
        modelBatch.render(ground, environment);
        modelBatch.render(instances, environment);
        modelBatch.end();

        for (ExplosionTriangle tri : explosionTriangles) {
            tri.render(delta);
        }

        explosionTriangles.begin();
        for (int i = 0; i < explosionTriangles.size; i++) {
            if (explosionTriangles.get(i).getTime() > 8f) {
                explosionTriangles.removeIndex(i);
            }
        }
        explosionTriangles.end();

        this.stage.getBatch().begin();
        this.stage.getBatch().draw(this.frameLeft, invaderPosition.x, invaderPosition.y);
        this.stage.getBatch().draw(this.frameRight, defenderPosition.x, defenderPosition.y);

        drawIcons(this.stage.getBatch(), invader, attackingCount, 90, 380, hasLeader(invader, from));
        drawIcons(this.stage.getBatch(), defender, defendingCount, 1420, 380, hasLeader(defender, to));

        int y = 400;

        Risk.font.draw(this.stage.getBatch(), String.format("%s", from.title()), 170, y -= 20);
        Risk.font.draw(this.stage.getBatch(), String.format("Battalions %d", game.battalionCount(from)), 170, y -= 20);

        y = 400;

        Risk.font.draw(this.stage.getBatch(), String.format("%s", to.title()), 1470, y -= 20);
        Risk.font.draw(this.stage.getBatch(), String.format("Battalions %d", game.battalionCount(to)), 1470, y -= 20);
        if (isDefendingStrongHold()) {
            Risk.font.draw(this.stage.getBatch(), "Stronghold Defender Bonus", 1470, y -= 20);
        }

        //Risk.font.draw(this.stage.getBatch(), String.format("CAMREA %f %f %f", camera.position.x, camera.position.y, camera.position.z), 1470, y -= 20);
        this.stage.getBatch().end();

        this.stage.act();
        this.stage.draw();

    }

    private void drawIcons(Batch batch, Army a, int count, float x, float y, boolean hasLeader) {
        TextureRegion tr = null, ltr = null;
        switch (a.armyType) {
            case RED:
                tr = RED_BATTALION.getKeyFrame(time, true);
                ltr = RED_LEADER.getKeyFrame(time, true);
                break;
            case GREEN:
                tr = GREEN_BATTALION.getKeyFrame(time, true);
                ltr = GREEN_LEADER.getKeyFrame(time, true);
                break;
            case BLACK:
                tr = BLACK_BATTALION.getKeyFrame(time, true);
                ltr = BLACK_LEADER.getKeyFrame(time, true);
                break;
            case YELLOW:
                tr = YELLOW_BATTALION.getKeyFrame(time, true);
                ltr = YELLOW_LEADER.getKeyFrame(time, true);
                break;
        }

        for (int i = 0; i < count; i++) {
            batch.draw(tr, x += 56, y, 96, 96);
        }

        if (hasLeader) {
            batch.draw(ltr, x += 56, y, 96, 96);
        }

    }

    private boolean hasLeader(Army a, TerritoryCard tc) {

        if (a.leader1 != null && a.leader1.territory == tc) {
            return true;
        }

        if (a.leader2 != null && a.leader2.territory == tc) {
            return true;
        }
        return false;
    }

    private void removeLeader(Army a, TerritoryCard tc) {

        if (a.leader1 != null && a.leader1.territory == tc) {
            a.leader1.territory = null;
        }

        if (a.leader2 != null && a.leader2.territory == tc) {
            a.leader2.territory = null;
        }
    }

    private void moveLeader(Army a, TerritoryCard from, TerritoryCard to) {

        if (a.leader1 != null && a.leader1.territory == from) {
            a.leader1.territory = to;
        }

        if (a.leader2 != null && a.leader2.territory == from) {
            a.leader2.territory = to;
        }
    }

    private boolean isDefendingStrongHold() {
        for (Location l : Location.values()) {
            if (!l.isSiteOfPower() && l.getTerritory() == to) {
                return true;
            }
        }
        return false;
    }

    private void clear() {
        for (btDefaultMotionState motionState : motionStates) {
            motionState.dispose();
        }
        motionStates.clear();
        for (btRigidBody body : bodies) {
            collisionWorld.removeRigidBody(body);
            body.dispose();
        }
        bodies.clear();
        instances.clear();
    }

    @Override
    public void dispose() {
        clear();
        for (BulletBase b : allBulletReferences) {
            b.dispose();
        }
        allBulletReferences.clear();
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

}
