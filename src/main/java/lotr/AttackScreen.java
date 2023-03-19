package lotr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
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
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
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
    private final int attackingCount, defendingCount;

    private Environment environment;
    private PointLight light;
    private ModelBatch modelBatch;
    private PerspectiveCamera camera;
    private CameraInputController inputController;
    private btCollisionConfiguration collisionConfiguration;
    private btCollisionDispatcher dispatcher;
    private btBroadphaseInterface broadphase;
    private btDynamicsWorld collisionWorld;
    private Array<ModelInstance> instances = new Array<>();
    private Array<btDefaultMotionState> motionStates = new Array<>();
    private Array<btRigidBody> bodies = new Array<>();
    private btRigidBody groundBody;
    private btDefaultMotionState groundMotionState;
    private ModelInstance ground;
    private static final Model groundModel;

    static {
        Bullet.init();

        ModelBuilder modelBuilder = new ModelBuilder();
        Texture txt = new Texture(Gdx.files.classpath("assets/data/risk-map.png"));
        groundModel = modelBuilder.createRect(20f, 0f, -20f, -20f, 0f, -20f, -20f, 0f, 20f, 20f, 0f, 20f, 0, 1, 0,
                new Material(TextureAttribute.createDiffuse(txt), ColorAttribute.createSpecular(1, 1, 1, 1), FloatAttribute.createShininess(8f)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
    }

    public AttackScreen(Risk main, GameScreen parent, Game game, Army invader, Army defender,
            TerritoryCard from, TerritoryCard to, int attackingCount, int defendingCount) {

        this.main = main;
        this.parent = parent;
        this.game = game;
        this.invader = invader;
        this.defender = defender;
        this.from = from;
        this.to = to;
        this.attackingCount = attackingCount;
        this.defendingCount = defendingCount;

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
        collisionWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, new btSequentialImpulseConstraintSolver(), collisionConfiguration);
        collisionWorld.setGravity(new Vector3(0, -60, 0));

        Vector3 tempVector = new Vector3();
        btCollisionShape groundShape = new btBoxShape(tempVector.set(20, 0, 20));
        btRigidBody.btRigidBodyConstructionInfo groundInfo = new btRigidBody.btRigidBodyConstructionInfo(0f, null, groundShape, Vector3.Zero);

        btCollisionShape btboxShape = new btBoxShape(new Vector3(1, 1, 1));
        btboxShape.calculateLocalInertia(1f, tempVector);
        btRigidBody.btRigidBodyConstructionInfo boxInfo = new btRigidBody.btRigidBodyConstructionInfo(1f, null, btboxShape, tempVector);

        ground = new ModelInstance(groundModel);
        groundMotionState = new btDefaultMotionState();
        groundMotionState.setWorldTransform(ground.transform);
        groundBody = new btRigidBody(groundInfo);
        groundBody.setMotionState(groundMotionState);
        collisionWorld.addRigidBody(groundBody);

        TextButton attack = new TextButton("ROLL", Risk.ccskin, "arcade");
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
                    addBox(Dice.getRedModel(r - 1), i + 2, 40, 0, boxInfo);
                }

                for (int i = 1; i <= defendingCount; i++) {
                    int r = DICE.roll();
                    rollsDefender.add(r);
                    addBox(Dice.getBlackModel(r - 1), i - 6, 40, 0, boxInfo);
                }

                Collections.sort(rollsInvader, Collections.reverseOrder());
                Collections.sort(rollsDefender, Collections.reverseOrder());

                Sounds.play(Sound.DICE);

                int highestAttacking = rollsInvader.get(0);
                if (hasLeader(invader, from)) {
                    highestAttacking++;
                }

                int highestDefending = rollsDefender.get(0);
                if (hasLeader(defender, to)) {
                    highestDefending++;
                }

                if (highestDefending >= highestAttacking) {
                    Sounds.play(Sound.EVADE);
                    animateText("Defender wins", 500, 500, "default-green");
                } else {
                    Sounds.play(Sound.PC_STRUCK);
                    animateText("Attacker wins", 500, 500, "default-red");
                }

            }
        });

        TextButton done = new TextButton("FINISH", Risk.ccskin, "arcade");
        done.setBounds(900 - 50, 50, 100, 100);
        done.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                main.setScreen(AttackScreen.this.parent);
            }
        });

        this.stage.addActor(done);
        this.stage.addActor(attack);
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

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
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

        this.stage.getBatch().begin();
        this.stage.getBatch().draw(this.frameLeft, 100, 200);
        this.stage.getBatch().draw(this.frameRight, 1400, 200);

        drawIcons(this.stage.getBatch(), invader, attackingCount, 90, 380, hasLeader(invader, from));
        drawIcons(this.stage.getBatch(), defender, defendingCount, 1420, 380, hasLeader(defender, to));

        int y = 400;

        String row1 = String.format("Battalions %d", game.battalionCount(from));
        Risk.font.draw(this.stage.getBatch(), row1, 170, y -= 20);

        y = 400;

        row1 = String.format("Battalions %d", game.battalionCount(to));
        Risk.font.draw(this.stage.getBatch(), row1, 1470, y -= 20);

        for (Location l : Location.values()) {
            if (!l.isSiteOfPower() && l.getTerritory() == to) {
                Risk.font.draw(this.stage.getBatch(), "Stronghold Defender Bonus", 1470, y -= 20);
                break;
            }
        }

        Risk.font.draw(this.stage.getBatch(), String.format("CAMREA %f %f %f", camera.position.x, camera.position.y, camera.position.z), 1470, y -= 20);

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

    private void clear() {
        for (btRigidBody body : bodies) {
            collisionWorld.removeRigidBody(body);
            body.dispose();
        }
        bodies.clear();
        for (btDefaultMotionState motionState : motionStates) {
            motionState.dispose();
        }
        motionStates.clear();
        instances.clear();
    }

    @Override
    public void dispose() {
        clear();
        collisionWorld.clearForces();
        collisionWorld.dispose();
        collisionWorld.release();
        broadphase.dispose();
        dispatcher.dispose();
        collisionConfiguration.dispose();
    }

    public void animateText(String text, int x, int y, String color) {
        Label label = new Label(text, Risk.skin, color);
        label.setPosition(x, y);
        this.stage.addActor(label);
        label.addAction(sequence(moveTo(x + 200, y + 200, 3), fadeOut(1), removeActor(label)));
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
