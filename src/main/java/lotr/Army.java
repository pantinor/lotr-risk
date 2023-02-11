package lotr;

import java.util.ArrayList;
import java.util.List;
import lotr.Constants.ArmyType;
import lotr.Constants.ClassType;

public class Army {

    public final ArmyType armyType;
    public final ClassType classType;
    public final boolean defenseOnly;
    public final List<Battalion> battalions = new ArrayList<>();

    public Army(ArmyType a, ClassType c, int startingBattalions, boolean defenseOnly) {
        this.armyType = a;
        this.classType = c;
        this.defenseOnly = defenseOnly;

        for (int i = 0; i < startingBattalions; i++) {
            this.battalions.add(new Battalion(this));
        }
    }

    public void pickTerritories(List<TerritoryCard> deck, int count) {
        //set 1 battalion in each owned territories
        List<TerritoryCard> cards = TerritoryCard.randomCards(deck, count);
        for (TerritoryCard c : cards) {
            Battalion b = this.battalions.remove(0);
            TerritoryCard.addBattalion(c, b);
        }
    }

}
