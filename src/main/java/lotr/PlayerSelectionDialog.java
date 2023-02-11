package lotr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
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

public class PlayerSelectionDialog extends Window {

    Actor previousKeyboardFocus, previousScrollFocus;
    private final FocusListener focusListener;

    private final StartScreen screen;
    Game game;

    public PlayerSelectionDialog(Game game, StartScreen screen) {
        super("Army Selection", Risk.skin.get("dialog", Window.WindowStyle.class));
        this.screen = screen;
        this.game = game;

        setSkin(Risk.skin);
        setModal(true);
        defaults().pad(5);
        
        Table table = new Table();
        table.align(Align.left | Align.top).pad(1);
        table.columnDefaults(0).expandX().left().uniformX();
        table.columnDefaults(1).expandX().left().uniformX();

        ScrollPane sp = new ScrollPane(table, Risk.skin);
        add(sp).expand().fill().minWidth(200);

        table.add(new Label("Select your army", Risk.skin));
        table.row();
        CheckBox cb1 = new CheckBox("Red Army (evil)", Risk.skin, "selection-blue");
        CheckBox cb2 = new CheckBox("Grey Army (evil)", Risk.skin, "selection-blue");
        CheckBox cb3 = new CheckBox("Green Army (good)", Risk.skin, "selection-blue");
        CheckBox cb4 = new CheckBox("Yellow Army (good)", Risk.skin, "selection-blue");
        ButtonGroup buttonGroup = new ButtonGroup(cb1, cb2, cb3, cb4);
        buttonGroup.setMaxCheckCount(1);
        buttonGroup.setMinCheckCount(0);
        table.add(cb1);
        table.add(cb2);
        table.row();
        table.add(cb3);
        table.add(cb4);
        table.row();

        table.add(new Label("", Risk.skin));
        table.row();

        table.add(new Label("Select at least 2 opposing armies.", Risk.skin));
        table.row();
        CheckBox cb11 = new CheckBox("Red Army (evil)", Risk.skin, "selection-yellow");
        CheckBox cb12 = new CheckBox("Grey Army (evil)", Risk.skin, "selection-yellow");
        CheckBox cb13 = new CheckBox("Green Army (good)", Risk.skin, "selection-yellow");
        CheckBox cb14 = new CheckBox("Yellow Army (good)", Risk.skin, "selection-yellow");
        table.add(cb11);
        table.add(cb12);
        table.row();
        table.add(cb13);
        table.add(cb14);
        table.row();

        table.add(new Label("", Risk.skin));
        table.row();

        TextButton close = new TextButton("OK", Risk.skin);
        table.add(close).size(120, 25);
        close.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event.toString().equals("touchDown")) {

                    int count = 0;
                    if (cb11.isChecked()) {
                        count++;
                    }
                    if (cb12.isChecked()) {
                        count++;
                    }
                    if (cb13.isChecked()) {
                        count++;
                    }
                    if (cb14.isChecked()) {
                        count++;
                    }

                    if (count < 2) {
                        return false;
                    }

                    if (cb1.isChecked() && cb11.isChecked()) {
                        return false;
                    }
                    if (cb2.isChecked() && cb12.isChecked()) {
                        return false;
                    }
                    if (cb3.isChecked() && cb13.isChecked()) {
                        return false;
                    }
                    if (cb4.isChecked() && cb14.isChecked()) {
                        return false;
                    }

                    int startingBattalionCount = (count == 1 ? 60 : (count == 2 ? 52 : 45));

                    if (cb1.isChecked()) {
                        Army red = new Army(Constants.ArmyType.RED, Constants.ClassType.EVIL, startingBattalionCount, false);
                        game.addArmy(red);
                    }
                    if (cb2.isChecked()) {
                        Army grey = new Army(Constants.ArmyType.GREY, Constants.ClassType.EVIL, startingBattalionCount, false);
                        game.addArmy(grey);
                    }
                    if (cb3.isChecked()) {
                        Army green = new Army(Constants.ArmyType.GREEN, Constants.ClassType.GOOD, startingBattalionCount, false);
                        game.addArmy(green);
                    }
                    if (cb4.isChecked()) {
                        Army yellow = new Army(Constants.ArmyType.YELLOW, Constants.ClassType.GOOD, startingBattalionCount, false);
                        game.addArmy(yellow);
                    }

                    if (cb11.isChecked()) {
                        Army red = new Army(Constants.ArmyType.RED, Constants.ClassType.EVIL, startingBattalionCount, false);
                        game.addArmy(red);
                    }
                    if (cb12.isChecked()) {
                        Army grey = new Army(Constants.ArmyType.GREY, Constants.ClassType.EVIL, startingBattalionCount, false);
                        game.addArmy(grey);
                    }
                    if (cb13.isChecked()) {
                        Army green = new Army(Constants.ArmyType.GREEN, Constants.ClassType.GOOD, startingBattalionCount, false);
                        game.addArmy(green);
                    }
                    if (cb14.isChecked()) {
                        Army yellow = new Army(Constants.ArmyType.YELLOW, Constants.ClassType.GOOD, startingBattalionCount, false);
                        game.addArmy(yellow);
                    }

                    //split deck into good and evil territories
                    List<TerritoryCard> evil = TerritoryCard.cardsOfClass(Constants.ClassType.EVIL);
                    List<TerritoryCard> good = TerritoryCard.cardsOfClass(Constants.ClassType.GOOD);
                    List<TerritoryCard> neutral = TerritoryCard.cardsOfClass(Constants.ClassType.NEUTRAL);

                    if (count == 3) {
                        game.red.pickTerritories(evil, 8);
                        game.grey.pickTerritories(evil, 8);
                        game.green.pickTerritories(good, 8);
                        game.yellow.pickTerritories(good, 8);
                    } else {
                        if (game.red != null && game.grey != null) {
                            game.red.pickTerritories(evil, 8);
                            game.grey.pickTerritories(evil, 8);
                            game.red.pickTerritories(neutral, 8);
                            game.grey.pickTerritories(neutral, 8);
                            if (game.green != null) {
                                game.green.pickTerritories(good, 16);
                            } else {
                                game.yellow.pickTerritories(good, 16);
                            }
                        } else {
                            game.green.pickTerritories(good, 8);
                            game.yellow.pickTerritories(good, 8);
                            game.green.pickTerritories(neutral, 8);
                            game.yellow.pickTerritories(neutral, 8);
                            if (game.red != null) {
                                game.red.pickTerritories(evil, 16);
                            } else {
                                game.grey.pickTerritories(evil, 16);
                            }
                        }
                    }

                    game.deck = new ArrayList<>();
                    Random rand = new Random();
                    {
                        for (TerritoryCard c : TerritoryCard.values()) {
                            game.deck.add(c);
                        }

                        List<TerritoryCard> shuffled = new ArrayList<>();
                        count = game.deck.size();
                        for (int i = 0; i < count; i++) {
                            int r = rand.nextInt(game.deck.size());
                            TerritoryCard c = game.deck.remove(r);
                            shuffled.add(c);
                        }

                        game.deck = shuffled;
                    }

                    hide();

                }
                return false;
            }
        });

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
                if (isModal() && stage != null && stage.getRoot().getChildren().size > 0 && stage.getRoot().getChildren().peek() == PlayerSelectionDialog.this) {
                    Actor newFocusedActor = event.getRelatedActor();
                    if (newFocusedActor != null && !newFocusedActor.isDescendantOf(PlayerSelectionDialog.this) && !(newFocusedActor.equals(previousKeyboardFocus) || newFocusedActor.equals(previousScrollFocus))) {
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

        //Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
    }

    protected InputListener ignoreTouchDown = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            event.cancel();
            return false;
        }
    };

}
