package lotr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

public class AdventureCardHelpDialog extends Dialog {

    private final int WIDTH = 1300;
    private final int HEIGHT = 800;

    private final InputProcessor input;
    private final ScrollPane scrollPane;

    public AdventureCardHelpDialog(GameScreen screen) {
        super("", Risk.skin.get("dialog", Window.WindowStyle.class));
        this.input = Gdx.input.getInputProcessor();

        Table table = new Table();
        table.pad(5);

        for (AdventureCard card : AdventureCard.values()) {

            Color textColor;

            switch (card.type()) {
                case MISSION:
                    textColor = Color.YELLOW;
                    break;
                case POWER:
                    textColor = Color.SKY;
                    break;
                case EVENT:
                    textColor = Color.GREEN;
                    break;
                default:
                    textColor = Color.WHITE;
            }

            table.add(new Label(card.type().toString(), Risk.skin, "default-font", textColor)).left();
            table.add(new Label(card.title(), Risk.skin, "default-font", textColor)).left();

            if (card.type() == AdventureCard.Type.MISSION) {
                Label missionLabel = new Label(
                        "Region: " + (card.region() != null ? card.region() : "—")
                        + " | Territory: " + (card.territory() != null ? card.territory() : "—")
                        + " | Evil Bonus: " + card.evilBonus()
                        + " | Good Bonus: " + card.goodBonus()
                        + (card.drawExtraCard() ? " - Draws extra card when completed" : ""),
                        Risk.skin, "default-font", textColor);
                missionLabel.setWrap(true);
                table.add(missionLabel).left().width(600);
            } else {
                Label textLabel = new Label(card.text2(), Risk.skin, "default-font", textColor);
                textLabel.setWrap(true);
                table.add(textLabel).left().width(600);
            }

            table.row();
        }

        scrollPane = new ScrollPane(table, Risk.skin);
        scrollPane.setScrollingDisabled(true, false);

        getContentTable().add(scrollPane).maxWidth(WIDTH).maxHeight(HEIGHT);

        addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                hide();
                return true; // Event handled
            }
        });

    }

    @Override
    public Dialog show(Stage stage, Action action) {
        Gdx.input.setInputProcessor(stage);
        stage.setScrollFocus(scrollPane);
        return super.show(stage, action);
    }

    @Override
    public void hide() {
        super.hide();
        Gdx.input.setInputProcessor(input);
    }

}
