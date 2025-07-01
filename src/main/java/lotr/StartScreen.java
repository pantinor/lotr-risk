package lotr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.FileInputStream;
import java.io.InputStream;
import static lotr.Risk.GAME;
import static lotr.Risk.defaultFont;
import static lotr.ai.BaseBot.Type.HEURISTIC;
import static lotr.ai.BaseBot.Type.RANDOM;
import static lotr.ai.BaseBot.Type.STRONG;
import static lotr.ai.BaseBot.Type.WEAK;
import lotr.ai.HeuristicBot;
import lotr.ai.RandomBot;
import lotr.ai.StrongBot;
import lotr.ai.WeakBot;
import org.apache.commons.io.IOUtils;

public class StartScreen implements Screen {

    private final Batch batch;
    private final TextButton play;
    private final BitmapFont font;
    private final Stage stage;
    private final Texture bg;

    public StartScreen(Risk main) {
        this.bg = new Texture(Gdx.files.classpath("assets/data/start-screen.jpg"));

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.classpath("assets/fonts/aniron.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 72;
        font = generator.generateFont(parameter);

        batch = new SpriteBatch();

        play = new TextButton("Play", Risk.skin, "blue");
        play.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                InputStream is = null;
                String json = null;
                try {
                    is = new FileInputStream("savedGame.json");
                    json = IOUtils.toString(is);
                } catch (Throwable e) {
                }

                if (is == null) {
                    GAME = new lotr.Game();

                    ClaimTerritoryScreen claimScreen = new ClaimTerritoryScreen(main, GAME);
                    main.setScreen(claimScreen);

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
                                    GAME.armies[i].bot = new RandomBot(GAME, GAME.armies[i]);
                                    break;
                                case WEAK:
                                    GAME.armies[i].bot = new WeakBot(GAME, GAME.armies[i]);
                                    break;
                                case HEURISTIC:
                                    GAME.armies[i].bot = new HeuristicBot(GAME, GAME.armies[i], 85);
                                    break;
                            }
                        }
                    }

                    GameScreen gameScreen = new GameScreen(main, GAME);

                    for (int i = 0; i < 4; i++) {
                        if (GAME.armies[i] != null && GAME.armies[i].botType != null) {
                            GAME.armies[i].bot.set(gameScreen, gameScreen.ringPath, gameScreen.cardSlider);
                        }
                    }

                    GAME.updateStandings();

                    main.setScreen(gameScreen);

                }
            }
        });
        play.setBounds(300, 170, 220, 40);

        stage = new Stage();
        stage.addActor(play);

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        int bgWidth = bg.getWidth();
        int bgHeight = bg.getHeight();

        float bgX = (screenWidth - bgWidth) / 2f;
        float bgY = (screenHeight - bgHeight) / 2f;

        batch.draw(this.bg, bgX, bgY);

        font.setColor(Color.WHITE);

        int x = 300;

        font.draw(batch, "Lord of the Rings", x + 3, Risk.SCREEN_HEIGHT - 100 - 3);
        font.draw(batch, "Game of RISK", x + 20 + 3, Risk.SCREEN_HEIGHT - 270 - 3);

        font.setColor(Color.RED);
        font.setColor(Color.RED);

        font.draw(batch, "Lord of the Rings", x, Risk.SCREEN_HEIGHT - 100);
        font.draw(batch, "Game of RISK", x + 20, Risk.SCREEN_HEIGHT - 270);

        defaultFont.draw(batch, "LIBGDX Conversion by Paul Antinori", 300, 128);

        batch.end();

        stage.act();
        stage.draw();

    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }

}
