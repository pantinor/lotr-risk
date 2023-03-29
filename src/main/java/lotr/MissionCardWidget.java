package lotr;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import java.util.ArrayList;
import java.util.List;
import static lotr.AdventureCard.Type.MISSION;

public class MissionCardWidget extends Table {

    public static final float WIDTH = 500;
    private static final float HEIGHT = 500;
    private static final TextureRegionDrawable BG = new TextureRegionDrawable(Risk.fillRectangle(1, 1, new Color(.3f, .3f, .3f, .8f)));
    List<CheckBox> boxes = new ArrayList<>();

    private final Game game;
    private final Stage stage;
    private boolean visible = false;

    private Army a;
    private Army b;
    private TerritoryCard ta;
    private TerritoryCard tb;

    public MissionCardWidget(Stage stage, Game game) {
        this.game = game;
        this.stage = stage;
    }

    public void set(Army a, Army b, TerritoryCard ta, TerritoryCard tb) {

        this.clear();

        this.a = a;
        this.b = b;
        this.ta = ta;
        this.tb = tb;

        setSize(WIDTH, HEIGHT);

        align(Align.left | Align.top).pad(5);
        columnDefaults(0).left();

        for (AdventureCard c : a.adventureCards) {
            if (c.type() == MISSION) {

                Table inner = new Table();
                inner.align(Align.left | Align.top).pad(5);
                columnDefaults(0).left();
                inner.setBackground(BG);

                CheckBox cb = new CheckBox("  " + c.title(), Risk.skin, "selection-blue");
                cb.setUserObject(c);
                cb.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                        AdventureCard card = (AdventureCard) actor.getUserObject();
                        process(card);
                    }
                });
                boxes.add(cb);
                inner.add(cb).left();
                inner.row();

                Label l = new Label(c.getText1() + " " + c.getText2(), Risk.skin);
                l.setWrap(true);
                l.setWidth(WIDTH - 20);
                inner.add(l).width(WIDTH - 20).left();

                add(inner);
                row();
            }
        }
    }

    public void show() {
        if (visible) {
            hide();
        } else {
            if (getActions().size > 0) {
                clearActions();
            }
            setPosition(Risk.SCREEN_WIDTH, Risk.SCREEN_HEIGHT - 15 - HEIGHT);
            addAction(Actions.sequence(Actions.show(), Actions.moveBy(-WIDTH, 0, 1f, Interpolation.sine)));
            visible = true;
        }
    }

    public void hide() {
        visible = false;
        if (getActions().size > 0) {
            clearActions();
        }
        addAction(Actions.sequence(Actions.moveBy(WIDTH, 0, 1f, Interpolation.sine), Actions.hide()));
    }

    private void process(AdventureCard card) {

        if (a == null || b == null || ta == null || tb == null) {
            return;
        }

        switch (card) {
            case GRIMA_WORMTONGUE_1:
            case GRIMA_WORMTONGUE_2:
                if (game.battalionCount(tb) > 1) {
                    b.removeBattalion(tb);
                    a.addBattalion(ta);
                }
                if (game.battalionCount(tb) > 1) {
                    b.removeBattalion(tb);
                    a.addBattalion(ta);
                }
                a.adventureCards.remove(card);
                break;
            case AMBUSH:
                a.addBattalion(ta);
                a.addBattalion(ta);
                a.addBattalion(ta);
                a.adventureCards.remove(card);
                break;

        }
        
        hide();

    }

}
