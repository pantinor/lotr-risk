package lotr;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import static lotr.Risk.SCREEN_WIDTH;

public class TurnWidget extends Table {

    private final Game game;
    private final Risk main;
    private final GameScreen gameScreen;

    private ChangeListener draftListener;
    private ChangeListener attackListener;
    private ChangeListener fortifyListener;
    private ChangeListener cardsListener;
    private ChangeListener ringListener;
    private ChangeListener doneListener;

    private final TextButton nextButton;
    private ChangeListener currentListener;
    private final Label stepLabel;

    TextureRegionDrawable activeTexture = new TextureRegionDrawable(Risk.fillRectangle(5, 5, Color.YELLOW));
    TextureRegionDrawable inactiveTexture = new TextureRegionDrawable(Risk.fillRectangle(5, 5, Color.GRAY));

    Image[] progressBar = new Image[5];

    public static enum Step {
        DRAFT, ATTACK, FORTIFY, CARDS, RING;
    }

    public TurnWidget(Risk main, GameScreen gameScreen, Game game) {
        this.game = game;
        this.main = main;
        this.gameScreen = gameScreen;

        align(Align.left | Align.top).pad(5);
        columnDefaults(0).expandX().left().uniformX();
        columnDefaults(1).expandX().left().uniformX();
        columnDefaults(2).expandX().left().uniformX();
        columnDefaults(3).expandX().left().uniformX();

        draftListener = new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                ReinforceScreen rsc = new ReinforceScreen(main, game, game.current(), gameScreen, TurnWidget.this);
                main.setScreen(rsc);
            }
        };

        doneListener = new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                game.next();
            }
        };

        nextButton = new TextButton(">>", Risk.skin, "blue");
        nextButton.addListener(draftListener);
        currentListener = draftListener;

        stepLabel = new Label(Step.DRAFT.toString(), Risk.skin);

        progressBar[0] = new Image(activeTexture);
        progressBar[1] = new Image(inactiveTexture);
        progressBar[2] = new Image(inactiveTexture);
        progressBar[3] = new Image(inactiveTexture);
        progressBar[4] = new Image(inactiveTexture);

        add(progressBar[0]).expand().uniform().center().minWidth(30).pad(3);
        add(progressBar[1]).expand().uniform().center().minWidth(30).pad(3);
        add(progressBar[2]).expand().uniform().center().minWidth(30).pad(3);
        add(progressBar[3]).expand().uniform().center().minWidth(30).pad(3);
        add(progressBar[4]).expand().uniform().center().minWidth(30).pad(3);
        row();

        add(stepLabel).colspan(5).center();
        row();

        add(nextButton).colspan(5).center().width(60).minWidth(60);
        row();

        setBounds(SCREEN_WIDTH / 2 - 100, 0, 200, 80);

        setBackground(new TextureRegionDrawable(Risk.fillRectangle(1, 1, new Color(0, 0, .62f, .8f))));

    }

    public void setNextStep(Step step) {
        
        stepLabel.setText(step.toString());

        progressBar[0].setDrawable(inactiveTexture);
        progressBar[1].setDrawable(inactiveTexture);
        progressBar[2].setDrawable(inactiveTexture);
        progressBar[3].setDrawable(inactiveTexture);
        progressBar[4].setDrawable(inactiveTexture);

        if (currentListener != null) {
            nextButton.removeListener(currentListener);
        }

        if (step == Step.ATTACK) {
            progressBar[1].setDrawable(activeTexture);
        } else if (step == Step.FORTIFY) {
            currentListener = fortifyListener;
            nextButton.addListener(fortifyListener);
            progressBar[2].setDrawable(activeTexture);
        } else if (step == Step.CARDS) {
            currentListener = cardsListener;
            nextButton.addListener(cardsListener);
            progressBar[3].setDrawable(activeTexture);
        } else if (step == Step.RING) {
            currentListener = ringListener;
            nextButton.addListener(ringListener);
            progressBar[4].setDrawable(activeTexture);
        }
    }

    public void setAttackButtonListener(Army invader, Army defender, TerritoryCard from, TerritoryCard to, int attackingCount, int defendingCount) {

        if (currentListener != null) {
            nextButton.removeListener(currentListener);
        }

        attackListener = new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                AttackScreen as = new AttackScreen(main, gameScreen, game, invader, defender, from, to, attackingCount, defendingCount);
                main.setScreen(as);
            }
        };

        currentListener = attackListener;

        nextButton.addListener(attackListener);
    }

}
