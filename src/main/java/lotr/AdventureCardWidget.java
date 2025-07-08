package lotr;

import com.badlogic.gdx.Gdx;
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
import java.util.Collections;
import java.util.List;
import static lotr.AdventureCard.APPOINT_A_SECOND_LEADER_1;
import static lotr.AdventureCard.APPOINT_A_SECOND_LEADER_2;
import static lotr.AdventureCard.APPOINT_A_SECOND_LEADER_3;
import static lotr.AdventureCard.APPOINT_A_SECOND_LEADER_4;
import static lotr.AdventureCard.ARAGORN_ARRIVES;
import static lotr.AdventureCard.THEYVE_BROUGHT_A_CAVE_TROLL;
import static lotr.AdventureCard.THE_BLACK_GATES_OPEN;
import static lotr.AdventureCard.THE_ENTMOOT;
import static lotr.AdventureCard.Type.EVENT;
import static lotr.AdventureCard.Type.MISSION;
import lotr.Constants.ClassType;
import static lotr.Constants.ClassType.EVIL;
import static lotr.Constants.ClassType.GOOD;
import lotr.util.CardAction;
import lotr.util.Logger;
import lotr.util.Sound;
import lotr.util.Sounds;

public class AdventureCardWidget extends Table implements CardAction {

    public static final float WIDTH = 500;
    private static final float HEIGHT = 500;
    private static final TextureRegionDrawable BG = new TextureRegionDrawable(Risk.fillRectangle(1, 1, new Color(.3f, .3f, .3f, .8f)));
    List<CheckBox> boxes = new ArrayList<>();

    private final Game game;
    private final Stage stage;
    private final Logger logger;
    private boolean visible = false;

    public AdventureCardWidget(Stage stage, Game game, Logger logger) {
        this.game = game;
        this.stage = stage;
        this.logger = logger;
        show();
    }

    public void show() {
        if (visible) {
            hide();
        } else {
            if (getActions().size > 0) {
                clearActions();
            }
            setPosition(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() - 15 - HEIGHT);
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

    public void set() {

        this.clear();

        setSize(WIDTH, HEIGHT);

        align(Align.left | Align.top).pad(5);
        columnDefaults(0).left();

        List<AdventureCard> tmp = new ArrayList<>();
        tmp.addAll(game.current().adventureCards);

        Collections.sort(tmp);

        for (AdventureCard c : tmp) {

            Table inner = new Table();
            inner.align(Align.left | Align.top).pad(5);
            columnDefaults(0).left();
            inner.setBackground(BG);

            String tag = String.format(" %s - %s", c.type(), c.title());
            if (c.type() == MISSION) {
                tag = String.format(" %s - %s  [%s - %s] (E:%d, G:%d)", c.type(), c.title(), c.region() == null ? "ANY" : c.region(), c.territory().title(), c.evilBonus(), c.goodBonus());
            }

            CheckBox cb = new CheckBox(tag, Risk.skin, "selection-blue");
            cb.setUserObject(c);
            cb.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                    if (!game.current().isBot()) {
                        AdventureCard card = (AdventureCard) actor.getUserObject();
                        process(card);
                        set();
                    }
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

    @Override
    public void process(AdventureCard card) {

        if (card.type() == MISSION) {
            if (card.drawExtraCard()) {
                if (!game.territoryCards.isEmpty()) {
                    TerritoryCard newCard = game.territoryCards.remove(0);
                    game.current().territoryCards.add(newCard);

                    game.current().removeAdventureCard(card);
                    game.adventureCards.add(card);

                    Sounds.play(Sound.ARMY_UPGRADE);
                    logger.log(String.format("%s picked up a extra territory card [%s].", game.current().armyType, card.title()), game.current().armyType.color());
                }
            } else {
                Region r = card.region();
                List<TerritoryCard> claimedTerritories = game.current().claimedTerritories();
                if (r != null) {
                    claimedTerritories.retainAll(r.territories());
                }
                List<Location> strongholds = game.current().ownedStrongholds(claimedTerritories);
                TerritoryCard from = !strongholds.isEmpty() ? strongholds.get(0).getTerritory() : !claimedTerritories.isEmpty() ? claimedTerritories.get(0) : null;

                if (from != null) {
                    int count = game.current().classType == ClassType.GOOD ? card.goodBonus() : card.evilBonus();
                    for (int i = 0; i < count; i++) {
                        game.current().addBattalion(from);
                    }
                    game.current().removeAdventureCard(card);
                    game.adventureCards.add(card);
                    Sounds.play(Sound.ARMY_UPGRADE);
                    logger.log(String.format("%s used adventure card [%s] to add battalions in %s.", game.current().armyType, card.title(), from.title()), game.current().armyType.color());

                } else {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    logger.log(String.format("%s could not use adventure card [%s].", game.current().armyType, card.title()), game.current().armyType.color());
                }
            }
        } else {
            logger.log(String.format("%s is using POWER card [%s].", game.current().armyType, card.title()), game.current().armyType.color());
            card.setUsed(true);
            game.current().removeAdventureCard(card);
            game.adventureCards.add(card);
        }

    }

    /**
     * Many of the Adventure Cards (4 of the 13 Event Cards) allow players to
     * place a second Leader on the board. Leaders can perish and these cards
     * allow you to replace them, which can be valuable as once you lose a
     * second Leader these cards are the only way to get it back. The Replace a
     * Leader step of your turn only allows you to place 1 Leader if you have
     * none on the board.
     *
     */
    @Override
    public void drawAdventureCard(boolean drawsAgain) {

        if (!game.adventureCards.isEmpty()) {

            AdventureCard newCard = game.adventureCards.remove(0);
            newCard.setUsed(false);

            if (drawsAgain) {
                logger.log(String.format("%s draws again and gets adventure card [%s] type [%s].", game.current().armyType, newCard.title(), newCard.type()), game.current().armyType.color());
            } else {
                logger.log(String.format("%s conquered a Site of Power and collects an adventure card [%s] type [%s].", game.current().armyType, newCard.title(), newCard.type()), game.current().armyType.color());
            }

            if (newCard.type() == EVENT) {
                //play immediately and draw another card
                boolean supported = false; //some of the cards not implemented

                if (newCard == APPOINT_A_SECOND_LEADER_1 || newCard == APPOINT_A_SECOND_LEADER_2
                        || newCard == APPOINT_A_SECOND_LEADER_3 || newCard == APPOINT_A_SECOND_LEADER_4) {

                    supported = true;

                    Leader leader1 = game.current().leader1;
                    Leader leader2 = game.current().leader2;

                    if (leader1.territory == null || leader2.territory == null) {
                        List<TerritoryCard> claimedTerritories = game.current().claimedTerritories();
                        List<Location> strongholds = game.current().ownedStrongholds(claimedTerritories);

                        Leader leaderToAppoint = (leader1.territory == null) ? leader1 : leader2;

                        leaderToAppoint.territory = !strongholds.isEmpty()
                                ? strongholds.get(0).getTerritory()
                                : claimedTerritories.get(0);
                    }
                }

                if (newCard == THE_BLACK_GATES_OPEN) {
                    supported = true;
                    Army owner = game.isClaimed(TerritoryCard.UDUN_VALE);
                    if (owner.classType == EVIL) {
                        for (int i = 0; i < 10; i++) {
                            owner.addBattalion(TerritoryCard.UDUN_VALE);
                        }
                        logger.log("Evil player reinforced Udun Vale with 10 battalions.", owner.armyType.color());
                    } else {
                        int count = game.battalionCount(TerritoryCard.UDUN_VALE);
                        if (count == 2) {
                            owner.removeBattalion(TerritoryCard.UDUN_VALE);
                        } else if (count > 2) {
                            owner.removeBattalion(TerritoryCard.UDUN_VALE);
                            owner.removeBattalion(TerritoryCard.UDUN_VALE);
                        }
                        logger.log("Good player loses battalions in Udun Vale.", owner.armyType.color());
                    }
                }

                if (newCard == THEYVE_BROUGHT_A_CAVE_TROLL) {
                    supported = true;
                    Army owner = game.isClaimed(TerritoryCard.MORIA);

                    if (owner.classType == EVIL) {
                        for (int i = 0; i < 2; i++) {
                            owner.addBattalion(TerritoryCard.MORIA);
                        }
                        logger.log("Evil player reinforced Moria with 2 battalions.", owner.armyType.color());
                    } else {
                        int count = game.battalionCount(TerritoryCard.MORIA);
                        if (count == 2) {
                            owner.removeBattalion(TerritoryCard.MORIA);
                        } else if (count > 2) {
                            owner.removeBattalion(TerritoryCard.MORIA);
                            owner.removeBattalion(TerritoryCard.MORIA);
                        }
                        logger.log("Good player loses battalions in Moria.", owner.armyType.color());
                    }
                }

                if (newCard == THE_ENTMOOT) {
                    supported = true;
                    Army owner = game.isClaimed(TerritoryCard.FANGORN);
                    logger.log(String.format("%s plays [%s] on territory [%s].", game.current().armyType, newCard.title(), TerritoryCard.FANGORN), game.current().armyType.color());

                    if (owner.classType == GOOD) {
                        for (int i = 0; i < 2; i++) {
                            owner.addBattalion(TerritoryCard.FANGORN);
                        }
                        logger.log("Good player reinforced Fangorn with 2 battalions.", owner.armyType.color());
                    } else {
                        int count = game.battalionCount(TerritoryCard.FANGORN);
                        if (count == 2) {
                            owner.removeBattalion(TerritoryCard.FANGORN);
                        } else if (count > 2) {
                            owner.removeBattalion(TerritoryCard.FANGORN);
                            owner.removeBattalion(TerritoryCard.FANGORN);
                        }
                        logger.log("Evil player loses battalions in Fangorn.", owner.armyType.color());
                    }
                }

                if (newCard == ARAGORN_ARRIVES) {
                    supported = true;
                    Army owner = game.isClaimed(TerritoryCard.MINAS_TIRITH);

                    if (owner.classType == GOOD) {
                        for (int i = 0; i < 10; i++) {
                            owner.addBattalion(TerritoryCard.MINAS_TIRITH);
                        }
                        logger.log("Good player reinforced Minas Tirith with 10 battalions.", owner.armyType.color());
                    } else {
                        int count = game.battalionCount(TerritoryCard.MINAS_TIRITH);
                        if (count == 2) {
                            owner.removeBattalion(TerritoryCard.MINAS_TIRITH);
                        } else if (count > 2) {
                            owner.removeBattalion(TerritoryCard.MINAS_TIRITH);
                            owner.removeBattalion(TerritoryCard.MINAS_TIRITH);
                        }
                        logger.log("Evil player loses battalions in Minas Tirith.", owner.armyType.color());
                    }
                }

                if (!supported) {
                    logger.log(String.format("%s Sorry but adventure card [%s] type [%s] is not supported at this time.",
                            game.current().armyType, newCard.title(), newCard.type()), game.current().armyType.color());
                }

                game.adventureCards.add(newCard);//put it back in the deck
                drawAdventureCard(true);

            } else {
                game.current().adventureCards.add(newCard);//add to the end

                //cannot have more than 4 adventure cards in hand
                if (game.current().adventureCards.size() > 4) {
                    List<AdventureCard> currentCards = game.current().adventureCards;

                    // Prioritize discarding a POWER card. If none exist, discard the first card in the list.
                    AdventureCard cardToDiscard = currentCards.stream()
                            .filter(card -> card.type() == AdventureCard.Type.POWER && card != AdventureCard.AMBUSH && card != AdventureCard.THE_ENEMY_IS_AT_HAND
                            && card != AdventureCard.GRIMA_WORMTONGUE_2 && card != AdventureCard.GRIMA_WORMTONGUE_1 && card != AdventureCard.SIEGE_MACHINES)
                            .findFirst()
                            .orElse(currentCards.get(0));

                    currentCards.remove(cardToDiscard);
                    game.adventureCards.add(cardToDiscard);
                    logger.log(String.format("%s had to discard adventure card [%s] type [%s] because they cannot have more than 4 adventure cards in hand.",
                            game.current().armyType, cardToDiscard.title(), cardToDiscard.type()), game.current().armyType.color());
                }
            }

        }
    }

}
