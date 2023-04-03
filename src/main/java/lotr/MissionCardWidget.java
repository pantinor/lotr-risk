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
import lotr.Constants.ClassType;
import lotr.util.CardAction;
import lotr.util.Sound;
import lotr.util.Sounds;

public class MissionCardWidget extends Table implements CardAction {

    public static final float WIDTH = 500;
    private static final float HEIGHT = 500;
    private static final TextureRegionDrawable BG = new TextureRegionDrawable(Risk.fillRectangle(1, 1, new Color(.3f, .3f, .3f, .8f)));
    List<CheckBox> boxes = new ArrayList<>();

    private final Game game;
    private final Stage stage;
    private boolean visible = false;

    private Army a;
    private Army b;
    private TerritoryCard from;
    private TerritoryCard to;

    public MissionCardWidget(Stage stage, Game game) {
        this.game = game;
        this.stage = stage;
    }

    public void set(Army a, Army b, TerritoryCard from, TerritoryCard to) {

        this.clear();

        this.a = a;
        this.b = b;
        this.from = from;
        this.to = to;

        setSize(WIDTH, HEIGHT);

        align(Align.left | Align.top).pad(5);
        columnDefaults(0).left();

        for (AdventureCard c : a.adventureCards) {

            Table inner = new Table();
            inner.align(Align.left | Align.top).pad(5);
            columnDefaults(0).left();
            inner.setBackground(BG);

            CheckBox cb = new CheckBox(String.format("%s %s - %s (%d, %d)", c.title(), c.region() == null ? "ANY" : c.region(), c.territory(), c.evilBonus(), c.goodBonus()), Risk.skin, "selection-blue");
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

            String desc = null;
            if (c.type() == MISSION) {
                desc = c.drawExtraCard() ? "Draw 1 extra territory card at the end of your turn." : "Receive extra battalions in region or territory.";
            } else {
                desc = c.text1() + " " + c.text2();
            }
            Label l = new Label(desc, Risk.skin);
            l.setWrap(true);
            l.setWidth(WIDTH - 20);
            inner.add(l).width(WIDTH - 20).left();

            add(inner);
            row();

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
        process(card, a, b, from, to);
        hide();
    }

    @Override
    public void process(AdventureCard card, Army a, Army b, TerritoryCard from, TerritoryCard to) {

        if (a == null || from == null) {
            return;
        }

        if (card.type() == MISSION) {
            if (card.drawExtraCard()) {
                if (!game.territoryCards.isEmpty()) {
                    TerritoryCard newCard = game.territoryCards.remove(0);
                    game.current().territoryCards.add(newCard);

                    a.adventureCards.remove(card);
                    game.adventureCards.add(card);

                    Sounds.play(Sound.POSITIVE_EFFECT);
                }
            } else {
                Region r = card.region();
                if (r == null || r.territories().contains(from)) {
                    int count = game.current().classType == ClassType.GOOD ? card.goodBonus() : card.evilBonus();
                    for (int i = 0; i < count; i++) {
                        a.addBattalion(from);
                    }
                    a.adventureCards.remove(card);
                    game.adventureCards.add(card);
                    Sounds.play(Sound.POSITIVE_EFFECT);
                } else {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                }
            }
        } else {
            processPower(card, a, b, from, to);
        }

    }

    private void processPower(AdventureCard card, Army a, Army b, TerritoryCard from, TerritoryCard to) {

        switch (card) {
            case GRIMA_WORMTONGUE_1:
            case GRIMA_WORMTONGUE_2:
                if (game.battalionCount(to) > 1) {
                    b.removeBattalion(to);
                    a.addBattalion(from);
                }
                if (game.battalionCount(to) > 1) {
                    b.removeBattalion(to);
                    a.addBattalion(from);
                }
                a.adventureCards.remove(card);
                game.adventureCards.add(card);
                break;
            case AMBUSH:
                a.addBattalion(from);
                a.addBattalion(from);
                a.addBattalion(from);
                a.adventureCards.remove(card);
                game.adventureCards.add(card);
                break;

        }
    }

}
