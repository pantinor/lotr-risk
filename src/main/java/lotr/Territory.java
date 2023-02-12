package lotr;

import java.util.ArrayList;
import java.util.List;

public class Territory {

    private final TerritoryCard card;
    public final List<Battalion> battalions = new ArrayList<>();
    public Leader leader;

    public Territory(TerritoryCard c) {
        this.card = c;
    }

    public TerritoryCard card() {
        return card;
    }


}
