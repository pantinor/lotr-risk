package lotr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lotr.util.Dice;
import lotr.util.Sound;
import lotr.util.Sounds;

public class AttackScreen implements Screen {

    public static final Dice DICE = new Dice(1, 6);

    private final Stage stage = new Stage();

    private final Risk main;
    private final GameScreen parent;
    private final Game game;
    private final Army invader;
    private final Army defender;
    private final TerritoryCard from;
    private final TerritoryCard to;

    private final int attackingCount, defendingCount;

    public AttackScreen(Risk main, GameScreen parent, Game game, Army invader, Army defender, TerritoryCard from, TerritoryCard to, int attackingCount, int defendingCount) {
        this.main = main;
        this.parent = parent;
        this.game = game;
        this.invader = invader;
        this.defender = defender;
        this.from = from;
        this.to = to;
        this.attackingCount = attackingCount;
        this.defendingCount = defendingCount;

        TextButton attack = new TextButton("ATTACK", Risk.skin);
        attack.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {

                List<Integer> rollsInvader = new ArrayList<>();
                List<Integer> rollsDefender = new ArrayList<>();

                for (int i = 1; i <= attackingCount; i++) {
                    int r = DICE.roll();
                    rollsInvader.add(r);
                    animateDice(1, r, i * 130, 100);
                }

                for (int i = 1; i <= defendingCount; i++) {
                    int r = DICE.roll();
                    rollsDefender.add(r);
                    animateDice(0, r, 400 + i * 130, 100);
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

        attack.setBounds(50, 50, 100, 20);

        this.stage.addActor(attack);
    }

    public void animateText(String text, int x, int y, String color) {
        Label label = new Label(text, Risk.skin, color);
        label.setPosition(x, y);
        this.stage.addActor(label);
        label.addAction(sequence(moveTo(x + 200, y + 200, 3), fadeOut(1), removeActor(label)));
    }

    public void animateDice(int id, int roll, int x, int y) {
        Image im = new Image(Risk.DICE_TEXTURES[id][roll - 1]);
        im.setPosition(x, y);
        this.stage.addActor(im);
        im.addAction(sequence(moveTo(x + 200, y + 200, 3), fadeOut(1), removeActor(im)));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, false);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        this.stage.getBatch().begin();

        int y = 500;

        Risk.font.setColor(invader.armyType.color());
        String row1 = String.format("Battalions %d", game.battalionCount(from));
        String row2 = String.format("Leaders bonus %s", hasLeader(invader, from));
        Risk.font.draw(this.stage.getBatch(), row1, 50, y -= 20);
        Risk.font.draw(this.stage.getBatch(), row2, 50, y -= 20);

        Risk.font.setColor(defender.armyType.color());
        row1 = String.format("Battalions %d", game.battalionCount(to));
        row2 = String.format("Leaders bonus %s", hasLeader(defender, to));
        Risk.font.draw(this.stage.getBatch(), row1, 50, y -= 20);
        Risk.font.draw(this.stage.getBatch(), row2, 50, y -= 20);
        for (Location l : Location.values()) {
            if (!l.isSiteOfPower() && l.getTerritory() == to) {
                Risk.font.draw(this.stage.getBatch(), "Stronghold Defender Bonus", 50, y -= 20);
                break;
            }
        }

        this.stage.getBatch().end();

        this.stage.act();
        this.stage.draw();

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
