package lotr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lotr.ai.BaseBot;
import lotr.ai.HeuristicBot;

public class NewGameDialog extends Dialog {

    private final ClaimTerritoryScreen screen;

    public NewGameDialog(Game game, ClaimTerritoryScreen screen) {
        super("Army Selection", Risk.skin.get("dialog", WindowStyle.class));
        this.screen = screen;

        setSkin(Risk.skin);
        getTitleTable().padTop(20);
        pad(10);

        Table table = getContentTable();

        table.add(new Label("In 3-player game, pick 2 Evil and 1 Good army.", Risk.skin)).padTop(20);
        table.row();
        table.add(new Label("In 4-player game, pick 2 Evil and 2 Good armies.", Risk.skin));
        table.row();
        table.add(new Label("Exactly one selected army must be player-controlled.", Risk.skin));
        table.row();
        table.add(new Label("The remaining armies will be controlled by AI Bot.", Risk.skin));
        table.row();

        table.add(new Label("", Risk.skin));
        table.row();

        Table inner = new Table();
        inner.align(Align.left | Align.top).pad(1);
        inner.columnDefaults(0).left();
        inner.columnDefaults(1).left();

        CheckBox cbr = new CheckBox("RED - EVIL", Risk.skin, "selection-red");
        CheckBox cbb = new CheckBox("BLACK - EVIL", Risk.skin, "selection-blue");
        CheckBox cbg = new CheckBox("GREEN - GOOD", Risk.skin, "selection-green");
        CheckBox cby = new CheckBox("YELLOW - GOOD", Risk.skin, "selection-yellow");
        ButtonGroup buttonGroup1 = new ButtonGroup(cbr, cbb, cbg, cby);
        buttonGroup1.setMaxCheckCount(4);
        buttonGroup1.setMinCheckCount(3);
        CheckBox cbrb = new CheckBox("BOT", Risk.skin, "selection-red");
        CheckBox cbbb = new CheckBox("BOT", Risk.skin, "selection-blue");
        CheckBox cbgb = new CheckBox("BOT", Risk.skin, "selection-green");
        CheckBox cbyb = new CheckBox("BOT", Risk.skin, "selection-yellow");
        ButtonGroup buttonGroup2 = new ButtonGroup(cbrb, cbbb, cbgb, cbyb);
        buttonGroup2.setMaxCheckCount(3);
        buttonGroup2.setMinCheckCount(2);
        inner.add(cbr).padRight(20);
        inner.add(cbrb);
        inner.row();
        inner.add(cbb).padRight(20);
        inner.add(cbbb);
        inner.row();
        inner.add(cbg).padRight(20);
        inner.add(cbgb);
        inner.row();
        inner.add(cby).padRight(20);
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

                    List<CheckBox> selectedArmies = new ArrayList<>();
                    List<CheckBox> selectedBots = new ArrayList<>();

                    if (cbr.isChecked()) {
                        selectedArmies.add(cbr);
                    }
                    if (cbb.isChecked()) {
                        selectedArmies.add(cbb);
                    }
                    if (cbg.isChecked()) {
                        selectedArmies.add(cbg);
                    }
                    if (cby.isChecked()) {
                        selectedArmies.add(cby);
                    }

                    if (cbrb.isChecked()) {
                        selectedBots.add(cbrb);
                    }
                    if (cbbb.isChecked()) {
                        selectedBots.add(cbbb);
                    }
                    if (cbgb.isChecked()) {
                        selectedBots.add(cbgb);
                    }
                    if (cbyb.isChecked()) {
                        selectedBots.add(cbyb);
                    }

                    int totalSelected = selectedArmies.size();
                    int botCount = selectedBots.size();

                    int evilCount = 0;
                    int goodCount = 0;
                    for (CheckBox cb : selectedArmies) {
                        if (cb == cbr || cb == cbb) {
                            evilCount++;
                        }
                        if (cb == cbg || cb == cby) {
                            goodCount++;
                        }
                    }

                    if (totalSelected == 3 && !(evilCount == 2 && goodCount == 1)) {
                        return false;
                    }

                    if (totalSelected == 4 && !(evilCount == 2 && goodCount == 2)) {
                        return false;
                    }

                    if (botCount != totalSelected - 1) {
                        return false;
                    }

                    boolean fourplayers = buttonGroup1.getAllChecked().size == 4;

                    if (fourplayers) {
                        Army red = new Army(Constants.ArmyType.RED, Constants.ClassType.EVIL, 30);
                        game.setRed(red);
                        if (cbrb.isChecked()) {
                            red.botType = BaseBot.Type.HEURISTIC;
                            red.bot = new HeuristicBot(game, red, 85);
                        }

                        Army black = new Army(Constants.ArmyType.BLACK, Constants.ClassType.EVIL, 30);
                        game.setBlack(black);
                        if (cbbb.isChecked()) {
                            black.botType = BaseBot.Type.HEURISTIC;
                            black.bot = new HeuristicBot(game, black, 85);
                        }

                        Army green = new Army(Constants.ArmyType.GREEN, Constants.ClassType.GOOD, 30);
                        game.setGreen(green);
                        if (cbgb.isChecked()) {
                            green.botType = BaseBot.Type.HEURISTIC;
                            green.bot = new HeuristicBot(game, green, 85);
                        }

                        Army yellow = new Army(Constants.ArmyType.YELLOW, Constants.ClassType.GOOD, 30);
                        game.setYellow(yellow);
                        if (cbyb.isChecked()) {
                            yellow.botType = BaseBot.Type.HEURISTIC;
                            yellow.bot = new HeuristicBot(game, yellow, 85);
                        }
                    } else {
                        if (cbr.isChecked()) {
                            Army red = new Army(Constants.ArmyType.RED, Constants.ClassType.EVIL, 35);
                            game.setRed(red);
                            if (cbrb.isChecked()) {
                                red.botType = BaseBot.Type.HEURISTIC;
                                red.bot = new HeuristicBot(game, red, 85);
                            }
                        }
                        if (cbb.isChecked()) {
                            Army black = new Army(Constants.ArmyType.BLACK, Constants.ClassType.EVIL, 35);
                            game.setBlack(black);
                            if (cbbb.isChecked()) {
                                black.botType = BaseBot.Type.HEURISTIC;
                                black.bot = new HeuristicBot(game, black, 85);
                            }
                        }
                        if (cbg.isChecked()) {
                            Army green = new Army(Constants.ArmyType.GREEN, Constants.ClassType.GOOD, 35);
                            game.setGreen(green);
                            if (cbgb.isChecked()) {
                                green.botType = BaseBot.Type.HEURISTIC;
                                green.bot = new HeuristicBot(game, green, 85);
                            }
                        }
                        if (cby.isChecked()) {
                            Army yellow = new Army(Constants.ArmyType.YELLOW, Constants.ClassType.GOOD, 35);
                            game.setYellow(yellow);
                            if (cbyb.isChecked()) {
                                yellow.botType = BaseBot.Type.HEURISTIC;
                                yellow.bot = new HeuristicBot(game, yellow, 85);
                            }
                        }
                    }

                    // Split deck into good, evil, and neutral territories
                    List<TerritoryCard> evil = TerritoryCard.shuffledTerritoriesOfClass(Constants.ClassType.EVIL);
                    List<TerritoryCard> good = TerritoryCard.shuffledTerritoriesOfClass(Constants.ClassType.GOOD);
                    List<TerritoryCard> neutral = TerritoryCard.shuffledTerritoriesOfClass(Constants.ClassType.NEUTRAL);

                    if (fourplayers) {
                        // 4-PLAYER GAME
                        game.green.pickTerritories(good, 5);     // Player 1: Good
                        game.red.pickTerritories(evil, 5);       // Player 2: Evil
                        game.yellow.pickTerritories(good, 4);    // Player 3: Good
                        game.black.pickTerritories(evil, 4);     // Player 4: Evil
                    } else {
                        // 3-PLAYER GAME — determine which one army is null
                        List<Army> evilArmies = new ArrayList<>();
                        List<Army> goodArmies = new ArrayList<>();

                        if (game.red != null) {
                            evilArmies.add(game.red);
                        }
                        if (game.black != null) {
                            evilArmies.add(game.black);
                        }
                        if (game.green != null) {
                            goodArmies.add(game.green);
                        }
                        if (game.yellow != null) {
                            goodArmies.add(game.yellow);
                        }

                        // Assign territories per player order:
                        // Player 1 (Evil #1): 5 Evil + 4 Neutral
                        evilArmies.get(0).pickTerritories(evil, 5);
                        evilArmies.get(0).pickTerritories(neutral, 4);

                        // Player 2 (Good): 9 Good
                        goodArmies.get(0).pickTerritories(good, 9);

                        // Player 3 (Evil #2): 4 Evil + 5 Neutral
                        evilArmies.get(1).pickTerritories(evil, 4);
                        evilArmies.get(1).pickTerritories(neutral, 5);
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

    }

    @Override
    public Dialog show(Stage stage, Action action) {
        Gdx.input.setInputProcessor(stage);
        Dialog d = super.show(stage, action);
        return d;
    }

    @Override
    public void hide() {
        super.hide();
        this.screen.init();
        Gdx.input.setInputProcessor(getStage());
    }
}
