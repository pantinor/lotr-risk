package lotr;

import com.google.gson.annotations.Expose;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lotr.Constants.ArmyType;
import lotr.Constants.ClassType;

public class Army {

    @Expose
    public ArmyType armyType;
    @Expose
    public ClassType classType;
    @Expose
    public List<Battalion> battalions;
    @Expose
    public Leader leader1;
    @Expose
    public Leader leader2;
    @Expose
    public List<TerritoryCard> territoryCards;

    public Army() {

    }

    public Army(ArmyType a, ClassType c, int startingBattalions) {
        this.armyType = a;
        this.classType = c;
        this.leader1 = new Leader(a);
        this.leader2 = new Leader(a);
        this.battalions = new ArrayList<>();

        for (int i = 0; i < startingBattalions; i++) {
            this.battalions.add(new Battalion(a));
        }
    }

    //set 1 battalion in each owned territories
    public void pickTerritories(List<TerritoryCard> deck, int count) {
        
        this.territoryCards = new ArrayList<>();
        this.territoryCards.addAll(deck);
        
        Random rand = new Random();

        List<Battalion> tmp = new ArrayList<>();
        tmp.addAll(this.battalions);

        for (int i = 0; i < count; i++) {
            int r = rand.nextInt(deck.size());
            TerritoryCard c = deck.remove(r);
            Battalion b = tmp.remove(0);
            b.territory = c;
        }
    }

    public boolean assignTerritory(TerritoryCard tc) {
        for (Battalion b : this.battalions) {
            if (b.territory == null) {
                b.territory = tc;
                return true;
            }
        }
        return false;
    }

    public List<TerritoryCard> claimedTerritories() {
        List<TerritoryCard> tmp = new ArrayList<>();
        for (Battalion b : this.battalions) {
            if (!tmp.contains(b.territory)) {
                tmp.add(b.territory);
            }
        }
        return tmp;
    }
    
    public List<Location> ownedStrongholds(List<TerritoryCard> claimedTerritories) {
        List<Location> tmp = new ArrayList<>();
        for (Location l : Location.values()) {
            if (!l.isSiteOfPower() && claimedTerritories.contains(l.getTerritory())) {
                tmp.add(l);
            }
        }
        return tmp;
    }

    public List<Region> ownedRegions(List<TerritoryCard> claimedTerritories) {
        List<Region> tmp = new ArrayList<>();
        for (Region r : Region.values()) {
            if (claimedTerritories.containsAll(r.territories())) {
                tmp.add(r);
            }
        }
        return tmp;
    }

    public ArmyType getArmyType() {
        return armyType;
    }

    public void setArmyType(ArmyType armyType) {
        this.armyType = armyType;
    }

    public ClassType getClassType() {
        return classType;
    }

    public void setClassType(ClassType classType) {
        this.classType = classType;
    }

    public List<Battalion> getBattalions() {
        return battalions;
    }

    public void setBattalions(List<Battalion> battalions) {
        this.battalions = battalions;
    }

    public Leader getLeader1() {
        return leader1;
    }

    public void setLeader1(Leader leader1) {
        this.leader1 = leader1;
    }

    public Leader getLeader2() {
        return leader2;
    }

    public void setLeader2(Leader leader2) {
        this.leader2 = leader2;
    }

}
