package lotr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Align;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lotr.ai.BaseBot;
import lotr.ai.HeuristicBot;

public class NewGameDialog extends Window {

    Actor previousKeyboardFocus, previousScrollFocus;
    private final FocusListener focusListener;

    private final ClaimTerritoryScreen screen;
    private final Game game;

    public NewGameDialog(Game game, ClaimTerritoryScreen screen) {
        super("Army Selection", Risk.skin.get("dialog", Window.WindowStyle.class));
        this.screen = screen;
        this.game = game;

        setSkin(Risk.skin);
        setModal(true);
        defaults().pad(5);

        Table table = new Table();
        table.align(Align.left | Align.top).pad(5);
        table.columnDefaults(0).expandX().left().uniformX();
        table.columnDefaults(1).expandX().left().uniformX();

        ScrollPane sp = new ScrollPane(table, Risk.skin);
        add(sp).expand().fill().minWidth(200);

        table.add(new Label("Select 3 or 4 armies to play.", Risk.skin));
        table.row();
        table.add(new Label("1 army may be controlled by the player.", Risk.skin));
        table.row();
        table.add(new Label("The remaining armies will be controlled by AI Bot.", Risk.skin));
        table.row();

        table.add(new Label("", Risk.skin));
        table.row();

        Table inner = new Table();
        inner.align(Align.left | Align.top).pad(1);
        inner.columnDefaults(0).left();
        inner.columnDefaults(1).left();

        CheckBox cbr = new CheckBox("RED", Risk.skin, "selection-blue");
        CheckBox cbb = new CheckBox("BLACK", Risk.skin, "selection-blue");
        CheckBox cbg = new CheckBox("GREEN", Risk.skin, "selection-blue");
        CheckBox cby = new CheckBox("YELLOW   ", Risk.skin, "selection-blue");
        ButtonGroup buttonGroup1 = new ButtonGroup(cbr, cbb, cbg, cby);
        buttonGroup1.setMaxCheckCount(4);
        buttonGroup1.setMinCheckCount(3);
        CheckBox cbrb = new CheckBox("BOT", Risk.skin, "selection-blue");
        CheckBox cbbb = new CheckBox("BOT", Risk.skin, "selection-blue");
        CheckBox cbgb = new CheckBox("BOT", Risk.skin, "selection-blue");
        CheckBox cbyb = new CheckBox("BOT", Risk.skin, "selection-blue");
        ButtonGroup buttonGroup2 = new ButtonGroup(cbrb, cbbb, cbgb, cbyb);
        buttonGroup2.setMaxCheckCount(3);
        buttonGroup2.setMinCheckCount(2);
        inner.add(cbr);
        inner.add(cbrb);
        inner.row();
        inner.add(cbb);
        inner.add(cbbb);
        inner.row();
        inner.add(cbg);
        inner.add(cbgb);
        inner.row();
        inner.add(cby);
        inner.add(cbyb);
        inner.row();

        table.add(inner);
        table.row();

        table.add(new Label("", Risk.skin));
        table.row();

        TextButton close = new TextButton("OK", Risk.skin);
        table.add(close).size(120, 25).center();
        close.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event.toString().equals("touchDown")) {

                    boolean fourplayers = buttonGroup1.getAllChecked().size == 4;
                    if (fourplayers && buttonGroup2.getAllChecked().size != 3) {
                        return false;
                    } 
                    
                    if (!fourplayers && buttonGroup2.getAllChecked().size != 2) {
                        return false;
                    }

                    if (fourplayers) {
                        Army red = new Army(Constants.ArmyType.RED, Constants.ClassType.EVIL, 45);
                        game.setRed(red);
                        if (cbrb.isChecked()) {
                            red.botType = BaseBot.Type.HEURISTIC;
                            red.bot = new HeuristicBot(game, red);
                        }

                        Army black = new Army(Constants.ArmyType.BLACK, Constants.ClassType.EVIL, 45);
                        game.setBlack(black);
                        if (cbbb.isChecked()) {
                            black.botType = BaseBot.Type.HEURISTIC;
                            black.bot = new HeuristicBot(game, black);
                        }

                        Army green = new Army(Constants.ArmyType.GREEN, Constants.ClassType.GOOD, 45);
                        game.setGreen(green);
                        if (cbgb.isChecked()) {
                            green.botType = BaseBot.Type.HEURISTIC;
                            green.bot = new HeuristicBot(game, green);
                        }

                        Army yellow = new Army(Constants.ArmyType.YELLOW, Constants.ClassType.GOOD, 45);
                        game.setYellow(yellow);
                        if (cbyb.isChecked()) {
                            yellow.botType = BaseBot.Type.HEURISTIC;
                            yellow.bot = new HeuristicBot(game, yellow);
                        }
                    } else {
                        if (cbr.isChecked()) {
                            Army red = new Army(Constants.ArmyType.RED, Constants.ClassType.EVIL, 52);
                            game.setRed(red);
                            if (cbrb.isChecked()) {
                                red.botType = BaseBot.Type.HEURISTIC;
                                red.bot = new HeuristicBot(game, red);
                            }
                        }
                        if (cbb.isChecked()) {
                            Army black = new Army(Constants.ArmyType.BLACK, Constants.ClassType.EVIL, 52);
                            game.setBlack(black);
                            if (cbbb.isChecked()) {
                                black.botType = BaseBot.Type.HEURISTIC;
                                black.bot = new HeuristicBot(game, black);
                            }
                        }
                        if (cbg.isChecked()) {
                            Army green = new Army(Constants.ArmyType.GREEN, Constants.ClassType.GOOD, 52);
                            game.setGreen(green);
                            if (cbgb.isChecked()) {
                                green.botType = BaseBot.Type.HEURISTIC;
                                green.bot = new HeuristicBot(game, green);
                            }
                        }
                        if (cby.isChecked()) {
                            Army yellow = new Army(Constants.ArmyType.YELLOW, Constants.ClassType.GOOD, 52);
                            game.setYellow(yellow);
                            if (cbyb.isChecked()) {
                                yellow.botType = BaseBot.Type.HEURISTIC;
                                yellow.bot = new HeuristicBot(game, yellow);
                            }
                        }
                    }

                    //split deck into good and evil territories
                    List<TerritoryCard> evil = TerritoryCard.shuffledTerritoriesOfClass(Constants.ClassType.EVIL);
                    List<TerritoryCard> good = TerritoryCard.shuffledTerritoriesOfClass(Constants.ClassType.GOOD);
                    List<TerritoryCard> neutral = TerritoryCard.shuffledTerritoriesOfClass(Constants.ClassType.NEUTRAL);

                    if (fourplayers) {
                        game.red.pickTerritories(evil, 8);
                        game.black.pickTerritories(evil, 8);
                        game.green.pickTerritories(good, 8);
                        game.yellow.pickTerritories(good, 8);
                    } else {
                        if (game.red == null || game.black == null) {
                            game.green.pickTerritories(good, 8);
                            game.yellow.pickTerritories(good, 8);
                            game.green.pickTerritories(neutral, 8);
                            game.yellow.pickTerritories(neutral, 8);
                            if (game.red == null) {
                                game.black.pickTerritories(evil, 16);
                            } else {
                                game.red.pickTerritories(evil, 16);
                            }
                        } else {
                            game.red.pickTerritories(evil, 8);
                            game.black.pickTerritories(evil, 8);
                            game.red.pickTerritories(neutral, 8);
                            game.black.pickTerritories(neutral, 8);
                            if (game.green == null) {
                                game.yellow.pickTerritories(good, 16);
                            } else {
                                game.green.pickTerritories(good, 16);
                            }
                        }

                    }

                    List<TerritoryCard> temp = new ArrayList<>();
                    for (TerritoryCard c : TerritoryCard.values()) {
                        temp.add(c);
                    }
                    Random rand = new Random();
                    while (!temp.isEmpty()) {
                        int r = rand.nextInt(temp.size());
                        TerritoryCard c = temp.remove(r);
                        game.territoryCards.add(c);
                    }

                    hide();

                }
                return false;
            }
        });

        cbr.setChecked(true);
        cbb.setChecked(true);
        cbg.setChecked(true);
        cby.setChecked(true);
        
        cbrb.setChecked(true);
        cbbb.setChecked(true);
        cbgb.setChecked(false);
        cbyb.setChecked(true);

        focusListener = new FocusListener() {
            @Override
            public void keyboardFocusChanged(FocusListener.FocusEvent event, Actor actor, boolean focused) {
                if (!focused) {
                    focusChanged(event);
                }
            }

            @Override
            public void scrollFocusChanged(FocusListener.FocusEvent event, Actor actor, boolean focused) {
                if (!focused) {
                    focusChanged(event);
                }
            }

            private void focusChanged(FocusListener.FocusEvent event) {
                Stage stage = getStage();
                if (isModal() && stage != null && stage.getRoot().getChildren().size > 0 && stage.getRoot().getChildren().peek() == NewGameDialog.this) {
                    Actor newFocusedActor = event.getRelatedActor();
                    if (newFocusedActor != null && !newFocusedActor.isDescendantOf(NewGameDialog.this) && !(newFocusedActor.equals(previousKeyboardFocus) || newFocusedActor.equals(previousScrollFocus))) {
                        event.cancel();
                    }
                }
            }
        };
    }

    public void show(Stage stage) {

        clearActions();

        removeCaptureListener(ignoreTouchDown);

        previousKeyboardFocus = null;
        Actor actor = stage.getKeyboardFocus();
        if (actor != null && !actor.isDescendantOf(this)) {
            previousKeyboardFocus = actor;
        }

        previousScrollFocus = null;
        actor = stage.getScrollFocus();
        if (actor != null && !actor.isDescendantOf(this)) {
            previousScrollFocus = actor;
        }

        pack();

        stage.addActor(this);
        stage.setScrollFocus(this);

        Gdx.input.setInputProcessor(stage);

        Action action = sequence(Actions.alpha(0), Actions.fadeIn(0.4f, Interpolation.fade));
        addAction(action);

        setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round((stage.getHeight() - getHeight()) / 2));
    }

    public void hide() {
        Action action = sequence(fadeOut(0.4f, Interpolation.fade), Actions.removeListener(ignoreTouchDown, true), Actions.removeActor());

        Stage stage = getStage();

        if (stage != null) {
            removeListener(focusListener);
        }

        if (action != null) {
            addCaptureListener(ignoreTouchDown);
            addAction(sequence(action, Actions.removeListener(ignoreTouchDown, true), Actions.removeActor()));
        } else {
            remove();
        }

        this.screen.init();
    }

    protected InputListener ignoreTouchDown = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            event.cancel();
            return false;
        }
    };

}
