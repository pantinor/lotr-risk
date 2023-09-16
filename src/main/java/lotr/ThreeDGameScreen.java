package lotr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ShortArray;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static lotr.Risk.TMX_MAP;

public class ThreeDGameScreen implements Screen {

    private final float unitScale = 0.25f;

    private AssetManager assets;
    private Environment environment;

    private PerspectiveCamera camera;
    private CameraInputController inputController;
    private ModelBatch modelBatch;

    private List<ModelInstance> modelInstances = new ArrayList<>();
    private List<ModelInstance> floor = new ArrayList<>();

    private final Stage stage = new Stage();

    public ThreeDGameScreen() {

        assets = new AssetManager();
        assets.load("src/main/resources/assets/data/map-rendered.png", Texture.class);

        assets.update(2000);

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.25f, 0.25f, 0.25f, 1f));

        modelBatch = new ModelBatch();

        createAxes();

        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 0.1f;
        camera.far = 1000f;
        camera.position.set(-120, 280, 280);
        camera.lookAt(2688 / 8, 0, 2048 / 8);
        camera.update();

        inputController = new CameraInputController(camera);
        inputController.rotateLeftKey = inputController.rotateRightKey = inputController.forwardKey = inputController.backwardKey = 0;
        inputController.translateUnits = 100f;

        ModelBuilder builder = new ModelBuilder();

        Model sf = builder.createBox(2688 * unitScale, 1, 2048 * unitScale, new Material(TextureAttribute.createDiffuse(assets.get("src/main/resources/assets/data/map-rendered.png", Texture.class))), VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.Normal);
        floor.add(new ModelInstance(sf, new Vector3(2688 * (unitScale / 2), -.5f, 2048 * (unitScale / 2))));

        for (int x = 0; x < 640; x += 40) {
            for (int y = 0; y < 640; y += 40) {
                PointLight l = new PointLight();
                l.set(1f, 1f, 1f, x, 10f, y, 100);
                environment.add(l);
            }
        }

        MapLayer regionsLayer = TMX_MAP.getLayers().get("regions");
        Iterator<MapObject> iter = regionsLayer.getObjects().iterator();
        while (iter.hasNext()) {
            PolygonMapObject obj = (PolygonMapObject) iter.next();

            Polygon poly = new Polygon(obj.getPolygon().getVertices());
            poly.setScale(unitScale, unitScale);
            poly.setPosition(obj.getPolygon().getX() * unitScale, obj.getPolygon().getY() * unitScale);
            poly.setOrigin(obj.getPolygon().getOriginX() * unitScale, obj.getPolygon().getOriginY() * unitScale);

            String name = obj.getName();

            Risk.RegionWrapper w = new Risk.RegionWrapper();
            w.polygon = poly;
            w.vertices = poly.getTransformedVertices();
            w.territory = TerritoryCard.getTerritory(name);
            w.name = w.territory.capitalized();

            ModelInstance m = createPolygon(w.territory.region().color(), obj.getPolygon().getOriginX(), 2f, obj.getPolygon().getOriginY(), poly.getTransformedVertices());

            modelInstances.add(m);

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

        camera.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(camera);

        modelBatch.render(axesInstance);

        for (ModelInstance i : floor) {
            modelBatch.render(i, environment);
        }

        for (ModelInstance i : modelInstances) {
            modelBatch.render(i, environment);
        }

        modelBatch.end();

        stage.getBatch().begin();

        Risk.font.draw(stage.getBatch(), String.format("current camera coords: %d, %d, %d", (int) camera.position.x, (int) camera.position.y, (int) camera.position.z), 10, 800);

        stage.getBatch().end();
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

    final float GRID_MIN = -20;
    final float GRID_MAX = 600;
    final float GRID_STEP = 10;
    public Model axesModel;
    public ModelInstance axesInstance;

    private void createAxes() {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        // grid
        MeshPartBuilder builder = modelBuilder.part("grid", GL30.GL_LINES, VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorUnpacked, new Material());
        builder.setColor(Color.LIGHT_GRAY);
        for (float t = GRID_MIN; t <= GRID_MAX; t += GRID_STEP) {
            builder.line(t, 0, GRID_MIN, t, 0, GRID_MAX);
            builder.line(GRID_MIN, 0, t, GRID_MAX, 0, t);
        }
        // axes
        builder = modelBuilder.part("axes", GL30.GL_LINES, VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorUnpacked, new Material());
        builder.setColor(Color.RED);
        builder.line(0, 0, 0, 500, 0, 0);//x
        builder.setColor(Color.GREEN);
        builder.line(0, 0, 0, 0, 500, 0);//y
        builder.setColor(Color.BLUE);
        builder.line(0, 0, 0, 0, 0, 500);//z
        axesModel = modelBuilder.end();
        axesInstance = new ModelInstance(axesModel);
    }

    private static final EarClippingTriangulator EAR = new EarClippingTriangulator();

    private ModelInstance createPolygon(Color color, float x, float y, float z, float... vertices) {

        ModelBuilder mb = new ModelBuilder();
        mb.begin();

        MeshPartBuilder part = mb.part("polygon", GL20.GL_LINES, Usage.Position | Usage.ColorUnpacked, new Material());

        part.setColor(color);

        ShortArray arrRes = EAR.computeTriangles(vertices);

        for (int i = 0; i < arrRes.size - 2; i = i + 3) {
            float x1 = vertices[arrRes.get(i) * 2];
            float y1 = vertices[(arrRes.get(i) * 2) + 1];

            float x2 = vertices[(arrRes.get(i + 1)) * 2];
            float y2 = vertices[(arrRes.get(i + 1) * 2) + 1];

            float x3 = vertices[arrRes.get(i + 2) * 2];
            float y3 = vertices[(arrRes.get(i + 2) * 2) + 1];

            Vector3 p1 = new Vector3(y1, 0, x1);
            Vector3 p2 = new Vector3(y2, 0, x2);
            Vector3 p3 = new Vector3(y3, 0, x3);

            part.triangle(p1, p2, p3);
        }

        Model model = mb.end();

        ModelInstance modelInstance = new ModelInstance(model);
        modelInstance.transform.setTranslation(x, y, z);

        return modelInstance;
    }

}
