package lotr;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import lotr.Game.GameStepListener;
import lotr.Game.Step;
import static lotr.Risk.GAME;
import static lotr.Risk.SCREEN_WIDTH;
import static lotr.Risk.STAGE;
import lotr.util.Sound;
import lotr.util.Sounds;

public class TurnWidget extends Table implements GameStepListener {

    private final Game game;
    private final Risk main;
    private final GameScreen gameScreen;

    private final ChangeListener draftListener;
    private final ChangeListener endCombatlistener;
    private final ChangeListener fortifyListener;
    private final ChangeListener tcardListener;
    private final ChangeListener acardListener;
    private final ChangeListener replaceListener;
    private final ChangeListener ringListener;

    private final TextButton nextButton;
    private final TextButton combatButton;

    private ChangeListener currentListener;
    private final Label stepLabel;

    private final TextureRegionDrawable activeTexture = new TextureRegionDrawable(Risk.fillRectangle(5, 5, Color.YELLOW));
    private final TextureRegionDrawable inactiveTexture = new TextureRegionDrawable(Risk.fillRectangle(5, 5, Color.GRAY));

    private final Image[] progressBar = new Image[7];

    public Army invader, defender;
    public TerritoryCard from, to;
    boolean conqueredTerritory;

    public TurnWidget(Risk main, GameScreen gameScreen, Game game) {
        this.game = game;
        this.main = main;
        this.gameScreen = gameScreen;

        setBounds(SCREEN_WIDTH / 2 - 150, 0, 300, 160);

        setBackground(new TextureRegionDrawable(Risk.fillRectangle(1, 1, new Color(0, 0, .62f, .8f))));

        align(Align.left | Align.top).pad(3);
        columnDefaults(0).expandX().left().uniformX();
        columnDefaults(1).expandX().left().uniformX();
        columnDefaults(2).expandX().left().uniformX();
        columnDefaults(3).expandX().left().uniformX();
        columnDefaults(4).expandX().left().uniformX();
        columnDefaults(5).expandX().left().uniformX();
        columnDefaults(6).expandX().left().uniformX();

        game.registerListener(this);

        draftListener = new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                conqueredTerritory = false;
                ReinforceScreen rsc = new ReinforceScreen(main, game, game.current(), gameScreen, TurnWidget.this);
                main.setScreen(rsc);
                game.nextStep();//attack
            }
        };

        endCombatlistener = new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                FortifyScreen rsc = new FortifyScreen(main, game, game.current(), gameScreen, TurnWidget.this);
                main.setScreen(rsc);
                game.nextStep();//fortify
            }
        };

        fortifyListener = new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                game.nextStep();//tcard
            }
        };

        tcardListener = new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (conqueredTerritory) {
                    if (!game.territoryCards.isEmpty()) {
                        TerritoryCard newCard = game.territoryCards.remove(0);
                        game.current().territoryCards.add(newCard);
                        Sounds.play(Sound.POSITIVE_EFFECT);
                    }
                }
                game.nextStep();//acard
            }
        };

        acardListener = new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                game.nextStep();//replace
            }
        };

        replaceListener = new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                game.nextStep();//ring
            }
        };

        ringListener = new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                gameScreen.ringPathActor.advance();
                game.nextStep();//draft for next player
                if (GAME.current().isBot()) {
                    STAGE.addAction(GAME.current().bot.run());
                }
            }
        };

        nextButton = new TextButton(">>", Risk.ccskin, "arcade");

        combatButton = new TextButton("", Risk.ccskin, "arcade-sword");
        combatButton.setVisible(false);
        combatButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (invader != null) {
                    AttackScreen as = new AttackScreen(main, gameScreen, game, invader, defender, from, to);
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
        progressBar[6] = new Image(inactiveTexture);

        add(progressBar[0]).expand().uniform().center().minWidth(35).pad(3);
        add(progressBar[1]).expand().uniform().center().minWidth(35).pad(3);
        add(progressBar[2]).expand().uniform().center().minWidth(35).pad(3);
        add(progressBar[3]).expand().uniform().center().minWidth(35).pad(3);
        add(progressBar[4]).expand().uniform().center().minWidth(35).pad(3);
        add(progressBar[5]).expand().uniform().center().minWidth(35).pad(3);
        add(progressBar[6]).expand().uniform().center().minWidth(35).pad(3);
        row();

        add(stepLabel).colspan(6).center();
        row();

        add(combatButton).colspan(3).center();
        add(nextButton).colspan(3).center();
        row();

        CheckBox missions = new CheckBox(" Missions", Risk.skin, "selection-small");
        missions.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                CheckBox p = (CheckBox) actor;
                gameScreen.setMissions(p.isChecked());
            }
        });
        add(missions).colspan(2).left();

        CheckBox cbtext = new CheckBox(" Labels", Risk.skin, "selection-small");
        cbtext.setChecked(true);
        cbtext.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Risk.textToggle = ((CheckBox) actor).isChecked();
            }
        });
        add(cbtext).colspan(2).right();

        CheckBox logs = new CheckBox(" Log", Risk.skin, "selection-small");
        logs.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                CheckBox p = (CheckBox) actor;
                gameScreen.toggleLog(p.isChecked());
            }
        });
        add(logs).colspan(2).left();

        nextStep(Step.DRAFT);
    }

    @Override
    public void nextStep(Step step) {

        stepLabel.setText(step.desc());

        progressBar[0].setDrawable(inactiveTexture);
        progressBar[1].setDrawable(inactiveTexture);
        progressBar[2].setDrawable(inactiveTexture);
        progressBar[3].setDrawable(inactiveTexture);
        progressBar[4].setDrawable(inactiveTexture);
        progressBar[5].setDrawable(inactiveTexture);
        progressBar[6].setDrawable(inactiveTexture);

        if (currentListener != null) {
            nextButton.removeListener(currentListener);
        }

        if (step == Step.DRAFT) {
            if (!game.current().isBot()) {
                currentListener = draftListener;
                nextButton.addListener(draftListener);
            }
            progressBar[0].setDrawable(activeTexture);
        } else if (step == Step.COMBAT) {
            if (!game.current().isBot()) {
                currentListener = endCombatlistener;
                nextButton.addListener(endCombatlistener);
            }
            progressBar[1].setDrawable(activeTexture);
        } else if (step == Step.FORTIFY) {
            if (!game.current().isBot()) {
                currentListener = fortifyListener;
                nextButton.addListener(fortifyListener);
            }
            progressBar[2].setDrawable(activeTexture);
        } else if (step == Step.TCARD) {
            if (!game.current().isBot()) {
                currentListener = tcardListener;
                nextButton.addListener(tcardListener);
            }
            progressBar[3].setDrawable(activeTexture);
        } else if (step == Step.ACARD) {
            if (!game.current().isBot()) {
                currentListener = acardListener;
                nextButton.addListener(acardListener);
            }
            progressBar[4].setDrawable(activeTexture);
        } else if (step == Step.REPLACE) {
            if (!game.current().isBot()) {
                currentListener = replaceListener;
                nextButton.addListener(replaceListener);
            }
            progressBar[5].setDrawable(activeTexture);
        } else if (step == Step.RING) {
            if (!game.current().isBot()) {
                currentListener = ringListener;
                nextButton.addListener(ringListener);
            }
            progressBar[6].setDrawable(activeTexture);
        }
    }

    public void setCombat(Army invader, Army defender, TerritoryCard from, TerritoryCard to) {
        this.invader = invader;
        this.defender = defender;
        this.from = from;
        this.to = to;
        this.combatButton.setVisible(true);
    }

    public void setConqueredTerritory(boolean conqueredTerritory) {
        this.conqueredTerritory = conqueredTerritory;
    }

    public void clearCombat(boolean conqueredTerritory) {
        this.conqueredTerritory = conqueredTerritory;
        this.combatButton.setVisible(false);
        this.invader = null;
        this.defender = null;
        this.from = null;
        this.to = null;
    }
}
