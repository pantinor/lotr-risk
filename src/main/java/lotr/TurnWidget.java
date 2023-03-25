package lotr;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
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

    private final ChangeListener draftListener;
    private final ChangeListener endCombatlistener;
    private final ChangeListener fortifyListener;
    private final ChangeListener cardsListener;
    private final ChangeListener ringListener;
    private final ChangeListener doneListener;

    private final TextButton nextButton;
    private final TextButton combatButton;

    private ChangeListener currentListener;
    private final Label stepLabel;

    private final TextureRegionDrawable activeTexture = new TextureRegionDrawable(Risk.fillRectangle(5, 5, Color.YELLOW));
    private final TextureRegionDrawable inactiveTexture = new TextureRegionDrawable(Risk.fillRectangle(5, 5, Color.GRAY));

    private final Image[] progressBar = new Image[6];
    
    public Step currentStep;
    public Army invader, defender;
    public TerritoryCard from, to;
    public int attackingCount, defendingCount;

    public static enum Step {
        DRAFT, COMBAT, FORTIFY, CARDS, RING, DONE;
    }

    public TurnWidget(Risk main, GameScreen gameScreen, Game game) {
        this.game = game;
        this.main = main;
        this.gameScreen = gameScreen;

        align(Align.left | Align.top).pad(3);
        columnDefaults(0).expandX().left().uniformX();
        columnDefaults(1).expandX().left().uniformX();
        columnDefaults(2).expandX().left().uniformX();
        columnDefaults(3).expandX().left().uniformX();
        columnDefaults(4).expandX().left().uniformX();
        columnDefaults(5).expandX().left().uniformX();

        draftListener = new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                setNextStep(TurnWidget.Step.COMBAT);
                ReinforceScreen rsc = new ReinforceScreen(main, game, game.current(), gameScreen, TurnWidget.this);
                main.setScreen(rsc);
            }
        };

        endCombatlistener = new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                setNextStep(TurnWidget.Step.FORTIFY);
                FortifyScreen rsc = new FortifyScreen(main, game, game.current(), gameScreen, TurnWidget.this);
                main.setScreen(rsc);
            }
        };

        fortifyListener = new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                setNextStep(TurnWidget.Step.CARDS);
            }
        };

        cardsListener = new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                setNextStep(TurnWidget.Step.RING);
            }
        };

        ringListener = new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                setNextStep(TurnWidget.Step.DONE);
            }
        };

        doneListener = new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                game.next();
                setNextStep(TurnWidget.Step.DRAFT);
            }
        };

        nextButton = new TextButton(">>", Risk.ccskin, "arcade");

        combatButton = new TextButton("", Risk.ccskin, "arcade-sword");
        combatButton.setVisible(false);
        combatButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (invader != null) {
                    AttackScreen as = new AttackScreen(main, gameScreen, game, invader, defender, from, to, attackingCount, defendingCount);
                    main.setScreen(as);
                }
            }
        });

        stepLabel = new Label(Step.DRAFT.toString(), Risk.skin);

        progressBar[0] = new Image(inactiveTexture);
        progressBar[1] = new Image(inactiveTexture);
        progressBar[2] = new Image(inactiveTexture);
        progressBar[3] = new Image(inactiveTexture);
        progressBar[4] = new Image(inactiveTexture);
        progressBar[5] = new Image(inactiveTexture);

        add(progressBar[0]).expand().uniform().center().minWidth(30).pad(3);
        add(progressBar[1]).expand().uniform().center().minWidth(30).pad(3);
        add(progressBar[2]).expand().uniform().center().minWidth(30).pad(3);
        add(progressBar[3]).expand().uniform().center().minWidth(30).pad(3);
        add(progressBar[4]).expand().uniform().center().minWidth(30).pad(3);
        add(progressBar[5]).expand().uniform().center().minWidth(30).pad(3);
        row();

        add(stepLabel).colspan(6).center();
        row();

        add(combatButton).colspan(3).center();
        add(nextButton).colspan(3).center();
        row();

        setBounds(SCREEN_WIDTH / 2 - 115, 0, 230, 140);

        setBackground(new TextureRegionDrawable(Risk.fillRectangle(1, 1, new Color(0, 0, .62f, .8f))));

        setNextStep(Step.DRAFT);
    }

    public void setNextStep(Step step) {
        
        currentStep = step;

        stepLabel.setText(step.toString());

        progressBar[0].setDrawable(inactiveTexture);
        progressBar[1].setDrawable(inactiveTexture);
        progressBar[2].setDrawable(inactiveTexture);
        progressBar[3].setDrawable(inactiveTexture);
        progressBar[4].setDrawable(inactiveTexture);
        progressBar[5].setDrawable(inactiveTexture);

        if (currentListener != null) {
            nextButton.removeListener(currentListener);
        }

        if (step == Step.DRAFT) {
            currentListener = draftListener;
            nextButton.addListener(draftListener);
            progressBar[0].setDrawable(activeTexture);
        } else if (step == Step.COMBAT) {
            currentListener = endCombatlistener;
            nextButton.addListener(endCombatlistener);
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
        } else if (step == Step.DONE) {
            currentListener = doneListener;
            nextButton.addListener(doneListener);
            progressBar[5].setDrawable(activeTexture);
        }
    }

    public void setCombat(Army invader, Army defender, TerritoryCard from, TerritoryCard to, int attackingCount, int defendingCount) {
        this.invader = invader;
        this.defender = defender;
        this.from = from;
        this.to = to;
        this.attackingCount = attackingCount;
        this.defendingCount = defendingCount;
        this.combatButton.setVisible(true);
    }

    public void clearCombat() {
        this.combatButton.setVisible(false);
        this.invader = null;
        this.defender = null;
        this.from = null;
        this.to = null;
        this.attackingCount = 0;
        this.defendingCount = 0;
    }
}
