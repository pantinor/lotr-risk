package lotr;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lotr.ai.StrongBot;
import org.apache.commons.io.IOUtils;

public class Risk extends Game {

    public static Skin skin, ccskin;
    public static BitmapFont font, smallFont;
    public static boolean textToggle = true;

    public static final int SCREEN_WIDTH = 1800;
    public static final int SCREEN_HEIGHT = 1050;

    public static Risk mainGame;
    public static TiledMap TMX_MAP;
    public static Animation<TextureRegion> RED_BATTALION, BLACK_BATTALION, GREEN_BATTALION, YELLOW_BATTALION;
    public static Animation<TextureRegion> RED_LEADER, BLACK_LEADER, GREEN_LEADER, YELLOW_LEADER;
    public static Animation<TextureRegion> FRODO, SAM;
    public static Texture RED_CIRCLE, GREEN_CIRCLE, BLACK_CIRCLE, YELLOW_CIRCLE, LEADER_CIRCLE;

    public static TextureRegion[][] DICE_TEXTURES;

    private static final Model[] blackModels = new Model[6];
    private static final Model[] redModels = new Model[6];

    public static lotr.Game GAME;
    public static Stage STAGE;

    public static void main(String[] args) {

        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();

        cfg.title = "LOTR Risk";
        cfg.width = SCREEN_WIDTH;
        cfg.height = SCREEN_HEIGHT;

        new LwjglApplication(new Risk(), cfg);
    }

    @Override
    public void create() {

        skin = new Skin(Gdx.files.classpath("assets/skin/uiskin.json"));
        ccskin = new Skin(Gdx.files.classpath("assets/skin/clean-crispy/clean-crispy-ui.json"));

        font = skin.get("default-font", BitmapFont.class);

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.classpath("assets/fonts/gnuolane.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 14;
        smallFont = generator.generateFont(parameter);
        skin.add("small", smallFont, BitmapFont.class);

        TerritoryCard.init();

        TmxMapLoader loader = new TmxMapLoader(CLASSPTH_RSLVR);
        TMX_MAP = loader.load("assets/data/map.tmx");

        TiledMapTileSet tileset = TMX_MAP.getTileSets().getTileSet("uf_heroes");

        RED_BATTALION = getAnimation(tileset, 36);
        BLACK_BATTALION = getAnimation(tileset, 152);
        GREEN_BATTALION = getAnimation(tileset, 96);
        YELLOW_BATTALION = getAnimation(tileset, 24);

        RED_LEADER = getAnimation(tileset, 300);
        BLACK_LEADER = getAnimation(tileset, 64);
        GREEN_LEADER = getAnimation(tileset, 16);
        YELLOW_LEADER = getAnimation(tileset, 60);

        FRODO = getAnimation(tileset, 332);
        SAM = getAnimation(tileset, 320);

        RED_CIRCLE = fillCircle(Color.RED, 24);
        BLACK_CIRCLE = fillCircle(Color.GRAY, 24);
        GREEN_CIRCLE = fillCircle(Color.FOREST, 24);
        YELLOW_CIRCLE = fillCircle(Color.GOLDENROD, 24);
        LEADER_CIRCLE = fillCircle(Color.BLUE, 28);

        DICE_TEXTURES = TextureRegion.split(new Texture(Gdx.files.classpath("assets/data/dice-sheet.png")), 64, 64);

        for (int i = 0; i < 6; i++) {
            blackModels[i] = buildDiceModel(0, i);
        }
        for (int i = 0; i < 6; i++) {
            redModels[i] = buildDiceModel(1, i);
        }

        InputStream is = null;
        String json = null;
        try {
            is = new FileInputStream("savedGame.json");
            json = IOUtils.toString(is);
        } catch (Throwable e) {
        }

        if (is == null) {
            GAME = new lotr.Game();

            ClaimTerritoryScreen startScreen = new ClaimTerritoryScreen(this, GAME);
            setScreen(startScreen);

        } else {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.excludeFieldsWithoutExposeAnnotation().create();

            GAME = gson.fromJson(json, new TypeToken<lotr.Game>() {
            }.getType());

            GAME.setRed(GAME.red);
            GAME.setGreen(GAME.green);
            GAME.setBlack(GAME.black);
            GAME.setYellow(GAME.yellow);

            for (int i = 0; i < 4; i++) {
                if (GAME.armies[i] != null && GAME.armies[i].botType != null) {
                    switch (GAME.armies[i].botType) {
                        case STRONG:
                            GAME.armies[i].bot = new StrongBot(GAME, GAME.armies[i]);
                            break;
                        case RANDOM:
                            GAME.armies[i].bot = new StrongBot(GAME, GAME.armies[i]);
                            break;
                        case WEAK:
                            GAME.armies[i].bot = new StrongBot(GAME, GAME.armies[i]);
                            break;
                    }
                }
            }

            GameScreen gameScreen = new GameScreen(this, GAME);
            setScreen(gameScreen);

            for (int i = 0; i < 4; i++) {
                if (GAME.armies[i] != null && GAME.armies[i].botType != null) {
                    GAME.armies[i].bot.set(gameScreen.logs, gameScreen.ringPath, gameScreen.missionCardSlider);
                }
            }
            
            GAME.updateStandings();

        }

    }

    public static Texture fillRectangle(int width, int height, Color color) {
        Pixmap pix = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pix.setColor(color);
        pix.fillRectangle(0, 0, width, height);
        Texture t = new Texture(pix);
        pix.dispose();
        return t;
    }

    public static Texture fillCircle(Color color, int size) {
        Pixmap px = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        px.setColor(color);
        px.fillCircle(size / 2, size / 2, size / 2 - 2);
        Texture t = new Texture(px);
        px.dispose();
        return t;
    }

    public static final FileHandleResolver CLASSPTH_RSLVR = new FileHandleResolver() {
        @Override
        public FileHandle resolve(String fileName) {
            return Gdx.files.classpath(fileName);
        }
    };

    public static class RegionWrapper {

        float[] vertices;
        Polygon polygon;
        String name;
        TerritoryCard territory;
        Map<TerritoryCard, RegionWrapper> adjacents = new HashMap<>();
        Vector3 battalionPosition;
        Vector3 textPosition;
        Vector3 namePosition;
    }

    public static class FortifyRegionWrapper extends RegionWrapper {

        boolean isConnected;
    }

    public static void setPoints(TiledMapTileLayer layer, List<RegionWrapper> regions, float unitScale) {

        int layerWidth = layer.getWidth();
        int layerHeight = layer.getHeight();

        float layerTileWidth = layer.getTileWidth() * unitScale;
        float layerTileHeight = layer.getTileHeight() * unitScale;

        float layerOffsetX = layer.getRenderOffsetX() * unitScale;
        float layerOffsetY = -layer.getRenderOffsetY() * unitScale;

        float layerHexLength = 16f * unitScale;
        boolean staggerIndexEven = false;

        float tileWidthUpperCorner = (layerTileWidth + layerHexLength) / 2;
        float layerTileHeight50 = layerTileHeight * 0.50f;

        int row1 = 0;
        int row2 = layerHeight;

        int col1 = 0;
        int col2 = layerWidth;

        int colA = (staggerIndexEven == (col1 % 2 == 0)) ? col1 + 1 : col1;
        int colB = (staggerIndexEven == (col1 % 2 == 0)) ? col1 : col1 + 1;

        for (int row = row2 - 1; row >= row1; row--) {
            for (int col = colA; col < col2; col += 2) {
                TiledMapTileLayer.Cell iconCell = layer.getCell(col, row);
                if (iconCell != null) {
                    int iconId = iconCell.getTile().getId();
                    float x = tileWidthUpperCorner * col + layerOffsetX;
                    float y = layerTileHeight50 + (layerTileHeight * row) + layerOffsetY;
                    Vector2 v = new Vector2(x, y);
                    for (RegionWrapper w : regions) {
                        if (w.polygon.contains(v)) {
                            if (iconId == 7) {
                                w.textPosition = new Vector3(x, y, 0);
                            } else if (iconId == 44) {
                                w.namePosition = new Vector3(x, y, 0);
                            } else {
                                w.battalionPosition = new Vector3(x, y, 0);
                            }
                        }
                    }
                }
            }
            for (int col = colB; col < col2; col += 2) {
                TiledMapTileLayer.Cell iconCell = layer.getCell(col, row);
                if (iconCell != null) {
                    int iconId = iconCell.getTile().getId();
                    float x = tileWidthUpperCorner * col + layerOffsetX;
                    float y = layerTileHeight * row + layerOffsetY;
                    Vector2 v = new Vector2(x, y);
                    for (RegionWrapper w : regions) {
                        if (w.polygon.contains(v)) {
                            if (iconId == 7) {
                                w.textPosition = new Vector3(x, y, 0);
                            } else if (iconId == 44) {
                                w.namePosition = new Vector3(x, y, 0);
                            } else {
                                w.battalionPosition = new Vector3(x, y, 0);
                            }
                        }
                    }
                }
            }
        }
        for (RegionWrapper w : regions) {
            for (TerritoryCard ac : w.territory.adjacents()) {
                if (ac != null) {
                    for (RegionWrapper t : regions) {
                        if (t.territory == ac) {
                            w.adjacents.put(ac, t);
                            break;
                        }
                    }
                }
            }
        }
    }

    public static Animation getAnimation(TiledMapTileSet tileset, int id) {
        int firstgid = tileset.getProperties().get("firstgid", Integer.class);

        Array<TextureRegion> arr = new Array<>();
        arr.add(tileset.getTile(firstgid + id).getTextureRegion());
        arr.add(tileset.getTile(firstgid + id + 1).getTextureRegion());
        arr.add(tileset.getTile(firstgid + id + 2).getTextureRegion());
        arr.add(tileset.getTile(firstgid + id + 3).getTextureRegion());

        Animation<TextureRegion> anim = new Animation(.4f, arr);
        return anim;
    }

    public static Model getBlackModel(int val) {
        return blackModels[val];
    }

    public static Model getRedModel(int val) {
        return redModels[val];
    }

    private static Model buildDiceModel(int id, int val) {
        ModelBuilder modelBuilder = new ModelBuilder();

        Texture texture = Risk.DICE_TEXTURES[0][0].getTexture();
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;

        modelBuilder.begin();
        MeshPartBuilder mpb = modelBuilder.part("box", GL20.GL_TRIANGLES, attributes, new Material(TextureAttribute.createDiffuse(texture)));
        mpb.setUVRange(Risk.DICE_TEXTURES[id][val == 0 ? 3 : 0]);
        mpb.rect(-1f, -1f, -1f, -1f, 1f, -1f, 1f, 1f, -1, 1f, -1f, -1f, 0, 0, -1);
        mpb.setUVRange(Risk.DICE_TEXTURES[id][val == 1 ? 3 : 1]);
        mpb.rect(-1f, 1f, 1f, -1f, -1f, 1f, 1f, -1f, 1f, 1f, 1f, 1f, 0, 0, 1);
        mpb.setUVRange(Risk.DICE_TEXTURES[id][val == 2 ? 3 : 2]);
        mpb.rect(-1f, -1f, 1f, -1f, -1f, -1f, 1f, -1f, -1f, 1f, -1f, 1f, 0, -1, 0);
        mpb.setUVRange(Risk.DICE_TEXTURES[id][val]);
        mpb.rect(-1f, 1f, -1f, -1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, -1f, 0, 1, 0);
        mpb.setUVRange(Risk.DICE_TEXTURES[id][val == 4 ? 3 : 4]);
        mpb.rect(-1f, -1f, 1f, -1f, 1f, 1f, -1f, 1f, -1f, -1f, -1f, -1f, -1, 0, 0);
        mpb.setUVRange(Risk.DICE_TEXTURES[id][val == 5 ? 3 : 5]);
        mpb.rect(1f, -1f, -1f, 1f, 1f, -1f, 1f, 1f, 1f, 1f, -1f, 1f, 1, 0, 0);
        Model boxModel = modelBuilder.end();
        return boxModel;
    }

    public static <K, V extends Comparable<V>> Map<K, V> sortByDescendingValues(Map<K, V> map) {
        Map<K, V> sortedMapReverseOrder = map.entrySet().
                stream().
                sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        return sortedMapReverseOrder;
    }

}
