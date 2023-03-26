package lotr;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
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

    public MissionCardWidget(Stage stage, Game game, Army a, Army b, TerritoryCard ta, TerritoryCard tb, float x) {

        setBounds(x, Risk.SCREEN_HEIGHT - 15 - HEIGHT, WIDTH, HEIGHT);
        align(Align.left | Align.top).pad(3);
        columnDefaults(0).left();

        ScrollPane sp = new ScrollPane(this, Risk.skin);
        stage.addActor(sp);

        for (AdventureCard c : a.adventureCards) {
            if (c.type() == MISSION) {

                Table inner = new Table();
                inner.align(Align.left | Align.top).pad(5);
                columnDefaults(0).left();
                inner.setBackground(BG);

                CheckBox cb = new CheckBox("  " + c.title(), Risk.skin, "default");
                cb.setUserObject(c);
                cb.addListener(listener);
                boxes.add(cb);
                inner.add(cb).left();
                inner.row();

                Label l = new Label(c.getText1() + " " + c.getText2(), Risk.skin);
                l.setWrap(true);
                l.setWidth(WIDTH);
                inner.add(l).width(WIDTH).left();

                add(inner);
                row();
            }
        }
    }

    public void disable() {
        for (CheckBox cb : boxes) {
            cb.setDisabled(true);
        }
    }

    private ChangeListener listener = new ChangeListener() {
        @Override
        public void changed(ChangeListener.ChangeEvent event, Actor actor) {
            AdventureCard card = (AdventureCard) actor.getUserObject();
            switch(card) {
                case GRIMA_WORMTONGUE_1:
                case GRIMA_WORMTONGUE_2:
                    
                    break;
                case AMBUSH:
                    break;
                
            }
        }
    };

}
