package lotr.util;

import lotr.AdventureCard;

public interface CardAction {

    public void drawAdventureCard();

    public void process(AdventureCard card);

}
