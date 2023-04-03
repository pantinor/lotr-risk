package lotr.util;

import lotr.AdventureCard;
import lotr.Army;
import lotr.TerritoryCard;

public interface CardAction {

    public void process(AdventureCard card, Army a, Army b, TerritoryCard from, TerritoryCard to);

}
