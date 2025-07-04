package lotr.util;

import lotr.AdventureCard;

public interface CardAction {

    public void drawAdventureCard(boolean drawsAgain);

    public void process(AdventureCard card);

}
