package lotr;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;

public class LogScrollPane extends ScrollPane {

    public static final float WIDTH = 600;
    private static final float HEIGHT = 400;

    private final Table internalTable;
    private final Label.LabelStyle ls = new Label.LabelStyle(Risk.skin.get("default-font", BitmapFont.class), Color.WHITE);

    public LogScrollPane() {
        super(new Table(), Risk.skin);

        setSize(WIDTH, HEIGHT);
        setPosition(Risk.SCREEN_WIDTH - WIDTH, -HEIGHT);

        internalTable = (Table) this.getActor();
        internalTable.align(Align.topLeft);

        setScrollingDisabled(true, false);
    }

    public void add(String text) {
        add(text, true);
    }

    public void add(String text, boolean scrollBottom) {

        if (text == null) {
            return;
        }

        Label label = new Label(text, ls);
        label.setWrap(true);
        label.setAlignment(Align.topLeft, Align.left);

        internalTable.add(label).pad(1).width(WIDTH - 10).maxWidth(WIDTH - 10);
        internalTable.row();

        pack();
        if (scrollBottom) {
            scrollTo(0, 0, 0, 0);
        }

    }

    public void show() {
        if (getActions().size > 0) {
            clearActions();
        }
        setPosition(Risk.SCREEN_WIDTH - WIDTH, -HEIGHT);
        addAction(Actions.sequence(Actions.moveBy(0, HEIGHT, 1f, Interpolation.sine)));
    }

    public void hide() {
        if (getActions().size > 0) {
            clearActions();
        }
        addAction(Actions.sequence(Actions.moveBy(0, -HEIGHT, 1f, Interpolation.sine)));
    }

    @Override
    public void clear() {
        internalTable.clear();
        pack();
    }

    @Override
    public float getPrefWidth() {
        return this.getWidth();
    }

    @Override
    public float getPrefHeight() {
        return this.getHeight();
    }

    @Override
    public float getMaxWidth() {
        return this.getWidth();
    }

    @Override
    public float getMaxHeight() {
        return this.getHeight();
    }

}
