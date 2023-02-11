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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import java.util.List;

public class Risk extends Game {

    public static Skin skin;
    public static BitmapFont font;
    public static BitmapFont fontSmall;

    public static final int SCREEN_WIDTH = 1800;
    public static final int SCREEN_HEIGHT = 1050;

    public static Risk mainGame;

    public static void main(String[] args) {

        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "LOTR Risk";
        cfg.width = SCREEN_WIDTH;
        cfg.height = SCREEN_HEIGHT;
        //cfg.fullscreen = true;
        //cfg.vSyncEnabled = true;
        new LwjglApplication(new Risk(), cfg);

    }

    @Override
    public void create() {

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.classpath("assets/fonts/gnuolane.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 18;
        font = generator.generateFont(parameter);

        parameter.size = 16;
        fontSmall = generator.generateFont(parameter);

        parameter.size = 16;
        parameter.color = Color.YELLOW;
        BitmapFont small = generator.generateFont(parameter);

        generator.dispose();

        skin = new Skin(Gdx.files.classpath("assets/skin/uiskin.json"));
        skin.remove("default-font", BitmapFont.class);
        skin.add("default-font", font, BitmapFont.class);
        skin.add("small-font", small, BitmapFont.class);
        Label.LabelStyle ls = new Label.LabelStyle();
        skin.add("small-font", ls, Label.LabelStyle.class);
        ls.font = small;

        lotr.Game game = new lotr.Game();

        ClaimTerritoryScreen startScreen = new ClaimTerritoryScreen(game);
        setScreen(startScreen);

        //GameScreen gameScreen = new GameScreen(this);
        //setScreen(gameScreen);
    }

    public static Texture fillRectangle(int width, int height, Color color, float alpha) {
        Pixmap pix = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pix.setColor(color.r, color.g, color.b, alpha);
        pix.fillRectangle(0, 0, width, height);
        Texture t = new Texture(pix);
        pix.dispose();
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
        Territory territory;
        Vector2 redPosition;
        Vector2 greyPosition;
        Vector2 greenPosition;
        Vector2 yellowPosition;
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
                if (layer.getCell(col, row) != null) {
                    float x = tileWidthUpperCorner * col + layerOffsetX;
                    float y = layerTileHeight50 + (layerTileHeight * row) + layerOffsetY;
                    Vector2 v = new Vector2(x, y);
                    for (RegionWrapper w : regions) {
                        if (w.polygon.contains(v)) {
                            if (w.redPosition == null) {
                                w.redPosition = new Vector2(v);
                            } else if (w.greyPosition == null) {
                                w.greyPosition = new Vector2(v);
                            } else if (w.greenPosition == null) {
                                w.greenPosition = new Vector2(v);
                            } else {
                                w.yellowPosition = new Vector2(v);
                            }
                        }
                    }
                }
            }
            for (int col = colB; col < col2; col += 2) {
                if (layer.getCell(col, row) != null) {
                    float x = tileWidthUpperCorner * col + layerOffsetX;
                    float y = layerTileHeight * row + layerOffsetY;
                    Vector2 v = new Vector2(x, y);
                    for (RegionWrapper w : regions) {
                        if (w.polygon.contains(v)) {
                            if (w.redPosition == null) {
                                w.redPosition = new Vector2(v);
                            } else if (w.greyPosition == null) {
                                w.greyPosition = new Vector2(v);
                            } else if (w.greenPosition == null) {
                                w.greenPosition = new Vector2(v);
                            } else {
                                w.yellowPosition = new Vector2(v);
                            }
                        }
                    }
                }
            }
        }
    }
}
