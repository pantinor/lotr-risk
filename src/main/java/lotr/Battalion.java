package lotr;

import com.google.gson.annotations.Expose;
import lotr.Constants.ArmyType;

public class Battalion {

    @Expose
    public ArmyType type;
    @Expose
    public TerritoryCard territory;

    public Battalion() {
    }

    public Battalion(ArmyType t) {
        this.type = t;
    }

    public ArmyType getType() {
        return type;
    }

    public void setType(ArmyType type) {
        this.type = type;
    }

    public TerritoryCard getTerritory() {
        return territory;
    }

    public void setTerritory(TerritoryCard territory) {
        this.territory = territory;
    }

}
