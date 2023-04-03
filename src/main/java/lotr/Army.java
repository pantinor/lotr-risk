package lotr;

import com.google.gson.annotations.Expose;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lotr.Constants.ArmyType;
import lotr.Constants.ClassType;
import lotr.ai.BaseBot;
import org.apache.commons.collections.CollectionUtils;

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
    public List<TerritoryCard> territoryCards = new ArrayList<>();
    @Expose
    public List<AdventureCard> adventureCards = new ArrayList<>();

    public BaseBot bot;

    @Expose
    public BaseBot.Type botType;
    
    public TerritoryCard missionIndication1 = null;
    public TerritoryCard missionIndication2 = null;

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

    public boolean isBot() {
        return this.bot != null;
    }

    public BaseBot getBot() {
        return bot;
    }

    public void setBot(BaseBot bot) {
        this.bot = bot;
    }

    public void addTerritoryCard(TerritoryCard c) {
        this.territoryCards.add(c);
    }

    public void removeTerritoryCard(TerritoryCard c) {
        this.territoryCards.remove(c);
    }

    public void addAdventureCard(AdventureCard c) {
        this.adventureCards.add(c);
    }

    public void removeAdventureCard(AdventureCard c) {
        this.adventureCards.remove(c);
    }

    //set 1 battalion in each owned territories
    public void pickTerritories(List<TerritoryCard> deck, int count) {

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

    public void addBattalion(TerritoryCard tc) {
        Battalion b = new Battalion(this.armyType);
        b.territory = tc;
        this.battalions.add(b);
    }

    public void removeBattalion(TerritoryCard tc) {
        Battalion tmp = null;

        for (Battalion b : this.battalions) {
            if (b.territory == tc) {
                tmp = b;
                break;
            }
        }
        if (tmp != null) {
            this.battalions.remove(tmp);
        }
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
    
    public Map<Region, Integer> percentOwnershipInEachRegion(List<TerritoryCard> claimedTerritories) {
        Map<Region, Integer> tmp = new HashMap<>();
        for (Region r : Region.values()) {
            int numberOfTerritoriesInRegion = r.territories().size();
            int territoriesOwnedInRegion = CollectionUtils.intersection(r.territories(), claimedTerritories).size();
            tmp.put(r, numberOfTerritoriesInRegion / (territoriesOwnedInRegion + 1));
        }
        return Risk.sortByDescendingValues(tmp);
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

    /**
     * Territories are connected if all the territories between are controlled
     * by this army.Used when fortifying.
     *
     *
     * @param from
     * @param to
     * @return is connected
     */
    public boolean isConnected(TerritoryCard from, TerritoryCard to) {
        List<TerritoryCard> claimedTerritories = claimedTerritories();
        if (claimedTerritories.contains(from) && claimedTerritories.contains(to)) {
            Map<TerritoryCard, TerritoryCard> connectedTerritories = new HashMap<>();
            return connected(from, to, claimedTerritories, connectedTerritories);
        }
        return false;
    }

    private boolean connected(TerritoryCard next, TerritoryCard to, List<TerritoryCard> claimedTerritories, Map<TerritoryCard, TerritoryCard> connectedTerritories) {
        for (TerritoryCard adj : next.adjacents()) {
            if (adj == to) {
                return true;
            } else {
                if (claimedTerritories.contains(adj) && !connectedTerritories.containsKey(adj)) {
                    connectedTerritories.put(adj, adj);
                    if (connected(adj, to, claimedTerritories, connectedTerritories)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
