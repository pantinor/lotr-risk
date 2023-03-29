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
        table.align(Align.left | Align.top).pad(1);
        table.columnDefaults(0).expandX().left().uniformX();
        table.columnDefaults(1).expandX().left().uniformX();

        ScrollPane sp = new ScrollPane(table, Risk.skin);
        add(sp).expand().fill().minWidth(200);

        table.add(new Label("Select the number of players.", Risk.skin));
        table.row();
        table.add(new Label("Each player chooses a color and takes all of the battalions of that color.", Risk.skin));
        table.row();
        table.add(new Label("The yellow and green armies represent the good armies while the black and red ones represent the evil armies.", Risk.skin));
        table.row();
        table.add(new Label("In a 3 player game, one player will control a good army while the remaining two players control the evil armies.", Risk.skin));
        table.row();
        CheckBox cb4 = new CheckBox("4 Players", Risk.skin, "selection-blue");
        CheckBox cb3 = new CheckBox("3 Players", Risk.skin, "selection-blue");
        ButtonGroup buttonGroup = new ButtonGroup(cb4, cb3);
        buttonGroup.setMaxCheckCount(1);
        buttonGroup.setMinCheckCount(1);
        table.add(cb4);
        table.row();
        table.add(cb3);
        table.row();

        table.add(new Label("", Risk.skin));
        table.row();

        TextButton close = new TextButton("OK", Risk.skin);
        table.add(close).size(120, 25);
        close.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event.toString().equals("touchDown")) {

                    if (cb4.isChecked()) {
                        Army red = new Army(Constants.ArmyType.RED, Constants.ClassType.EVIL, 45);
                        game.setRed(red);

                        Army black = new Army(Constants.ArmyType.BLACK, Constants.ClassType.EVIL, 45);
                        game.setBlack(black);

                        Army green = new Army(Constants.ArmyType.GREEN, Constants.ClassType.GOOD, 45);
                        game.setGreen(green);

                        Army yellow = new Army(Constants.ArmyType.YELLOW, Constants.ClassType.GOOD, 45);
                        game.setYellow(yellow);
                    } else {
                        Army red = new Army(Constants.ArmyType.RED, Constants.ClassType.EVIL, 52);
                        game.setRed(red);

                        Army black = new Army(Constants.ArmyType.BLACK, Constants.ClassType.EVIL, 52);
                        game.setBlack(black);

                        Army green = new Army(Constants.ArmyType.GREEN, Constants.ClassType.GOOD, 52);
                        game.setGreen(green);
                    }

                    //split deck into good and evil territories
                    List<TerritoryCard> evil = TerritoryCard.shuffledTerritoriesOfClass(Constants.ClassType.EVIL);
                    List<TerritoryCard> good = TerritoryCard.shuffledTerritoriesOfClass(Constants.ClassType.GOOD);
                    List<TerritoryCard> neutral = TerritoryCard.shuffledTerritoriesOfClass(Constants.ClassType.NEUTRAL);

                    if (cb4.isChecked()) {
                        game.red.pickTerritories(evil, 8);
                        game.black.pickTerritories(evil, 8);
                        game.green.pickTerritories(good, 8);
                        game.yellow.pickTerritories(good, 8);

                        int humanPlayer = new Random().nextInt(4);
                        if (humanPlayer != 0) {
                            game.red.botType = BaseBot.Type.values()[new Random().nextInt(3)];
                        }
                        if (humanPlayer != 1) {
                            game.green.botType = BaseBot.Type.values()[new Random().nextInt(3)];
                        }
                        if (humanPlayer != 2) {
                            game.black.botType = BaseBot.Type.values()[new Random().nextInt(3)];
                        }
                        if (humanPlayer != 3) {
                            game.yellow.botType = BaseBot.Type.values()[new Random().nextInt(3)];
                        }
                    } else {
                        game.red.pickTerritories(evil, 8);
                        game.black.pickTerritories(evil, 8);
                        game.red.pickTerritories(neutral, 8);
                        game.black.pickTerritories(neutral, 8);
                        game.green.pickTerritories(good, 16);
                        
                        int humanPlayer = new Random().nextInt(3);
                        if (humanPlayer != 0) {
                            game.red.botType = BaseBot.Type.values()[new Random().nextInt(3)];
                        }
                        if (humanPlayer != 1) {
                            game.green.botType = BaseBot.Type.values()[new Random().nextInt(3)];
                        }
                        if (humanPlayer != 2) {
                            game.black.botType = BaseBot.Type.values()[new Random().nextInt(3)];
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

        this.screen.initArmies();
    }

    protected InputListener ignoreTouchDown = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            event.cancel();
            return false;
        }
    };

}
