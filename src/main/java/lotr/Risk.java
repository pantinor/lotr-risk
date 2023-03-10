package lotr;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import lotr.util.Dice;
import lotr.util.Sound;
import lotr.util.Sounds;
import org.apache.commons.io.IOUtils;

public class Risk extends Game {

    public static Skin skin;
    public static BitmapFont font;

    public static final int SCREEN_WIDTH = 1800;
    public static final int SCREEN_HEIGHT = 1050;

    public static Risk mainGame;
    public static TiledMap TMX_MAP;
    public static Animation<TextureRegion> RED_BATTALION, BLACK_BATTALION, GREEN_BATTALION, YELLOW_BATTALION;
    public static Animation<TextureRegion> RED_LEADER, BLACK_LEADER, GREEN_LEADER, YELLOW_LEADER;
    public static Animation<TextureRegion> FRODO, SAM;
    public static Texture RED_CIRCLE, GREEN_CIRCLE, BLACK_CIRCLE, YELLOW_CIRCLE, LEADER_CIRCLE;

    public static TextureRegion[][] DICE_TEXTURES;
    public static final Dice DICE = new Dice(1, 6);
    public static TextureRegion rolledDiceImageLeft;
    public static TextureRegion rolledDiceImageRight;

    public static List<RingPathWrapper> RING_PATHS = new ArrayList<>();

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

        font = skin.get("default-font", BitmapFont.class);

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

        MapLayer pathLayer = TMX_MAP.getLayers().get("ring-path");
        Iterator<MapObject> pathIter = pathLayer.getObjects().iterator();
        while (pathIter.hasNext()) {
            RectangleMapObject obj = (RectangleMapObject) pathIter.next();
            RingPathWrapper w = new RingPathWrapper();
            w.name = obj.getName();
            w.id = obj.getProperties().get("id", Integer.class);
            w.x = obj.getProperties().get("x", Float.class);
            w.y = obj.getProperties().get("y", Float.class);
            RING_PATHS.add(w);
        }
        Collections.sort(RING_PATHS);
        RING_PATHS.get(0).selected = true;

        RED_CIRCLE = fillCircle(Color.RED, 24);
        BLACK_CIRCLE = fillCircle(Color.GRAY, 24);
        GREEN_CIRCLE = fillCircle(Color.FOREST, 24);
        YELLOW_CIRCLE = fillCircle(Color.GOLDENROD, 24);
        LEADER_CIRCLE = fillCircle(Color.BLUE, 28);

        DICE_TEXTURES = TextureRegion.split(new Texture(Gdx.files.classpath("assets/data/DiceSheet.png")), 56, 56);

        lotr.Game game = null;

        InputStream is = null;
        String json = null;
        try {
            is = new FileInputStream("savedGame.json");
            json = IOUtils.toString(is);
        } catch (Throwable e) {
        }

        if (is == null) {
            game = new lotr.Game();

            ClaimTerritoryScreen startScreen = new ClaimTerritoryScreen(this, game);
            setScreen(startScreen);

        } else {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.excludeFieldsWithoutExposeAnnotation().create();

            game = gson.fromJson(json, new TypeToken<lotr.Game>() {
            }.getType());

            game.setBlack(game.black);
            game.setGreen(game.green);
            game.setYellow(game.yellow);
            game.setRed(game.red);

            GameScreen gameScreen = new GameScreen(this, game);
            setScreen(gameScreen);
        }

    }

    public static Vector2 rollDice() {

        int roll1 = DICE.roll();
        int roll2 = DICE.roll();

        Sounds.play(Sound.DICE);

        rolledDiceImageLeft = DICE_TEXTURES[0][roll1 - 1];
        rolledDiceImageRight = DICE_TEXTURES[0][roll2 - 1];

        return new Vector2(roll1, roll2);
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
        Vector2 battalionPosition;
        Vector2 textPosition;
        Vector2 namePosition;
    }

    public static class RingPathWrapper implements Comparable {

        int id;
        String name;
        float x;
        float y;
        boolean selected;

        @Override
        public int compareTo(Object obj) {
            final RingPathWrapper other = (RingPathWrapper) obj;
            if (this.id > other.id) {
                return 1;
            } else if (this.id < other.id) {
                return -1;
            } else {
                return 0;
            }
        }

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
                                w.textPosition = new Vector2(v);
                            } else if (iconId == 44) {
                                w.namePosition = new Vector2(v);
                            } else {
                                w.battalionPosition = new Vector2(v);
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
                                w.textPosition = new Vector2(v);
                            } else if (iconId == 44) {
                                w.namePosition = new Vector2(v);
                            } else {
                                w.battalionPosition = new Vector2(v);
                            }
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
}
