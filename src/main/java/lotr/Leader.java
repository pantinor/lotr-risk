package lotr;

import com.google.gson.annotations.Expose;

public class Leader {

    @Expose
    public Constants.ArmyType type;
    @Expose
    public TerritoryCard territory;

    public Leader() {
    }

    public Leader(Constants.ArmyType t) {
        this.type = t;
    }

    public Constants.ArmyType getType() {
        return type;
    }

    public void setType(Constants.ArmyType type) {
        this.type = type;
    }

    public TerritoryCard getTerritory() {
        return territory;
    }

    public void setTerritory(TerritoryCard territory) {
        this.territory = territory;
    }

}
