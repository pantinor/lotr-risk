package lotr;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lotr.Constants.ArmyType;
import lotr.Constants.BattalionType;
import static lotr.Constants.BattalionType.DARK_RIDER;
import static lotr.Constants.BattalionType.EAGLE;
import static lotr.Constants.BattalionType.ELVEN_ARCHER;
import lotr.Constants.ClassType;
import static lotr.Constants.ClassType.EVIL;
import static lotr.Constants.ClassType.GOOD;
import static lotr.Constants.ClassType.NEUTRAL;

public enum TerritoryCard {

    WILD_CARD_1(null, null),
    WILD_CARD_2(null, null),
    ESGAROTH(NEUTRAL, EAGLE),
    HARAD(NEUTRAL, EAGLE),
    EREGION(NEUTRAL, ELVEN_ARCHER),
    ITHILIEN(NEUTRAL, ELVEN_ARCHER),
    THE_SHIRE(GOOD, DARK_RIDER),
    OLD_FOREST(NEUTRAL, ELVEN_ARCHER),
    BROWN_LANDS(EVIL, EAGLE),
    EASTERN_MIRKWOOD(EVIL, DARK_RIDER),
    DEEP_HARAD(EVIL, EAGLE),
    MINHIRIATH(NEUTRAL, EAGLE),
    UMBAR(EVIL, EAGLE),
    DEAD_MARSHES(EVIL, DARK_RIDER),
    GAP_OF_ROHAN(GOOD, EAGLE),
    SOUTH_RHUN(NEUTRAL, DARK_RIDER),
    NEAR_HARAD(NEUTRAL, ELVEN_ARCHER),
    NORTH_RHUN(NEUTRAL, DARK_RIDER),
    UDUN_VALE(EVIL, ELVEN_ARCHER),
    ANFALAS(GOOD, DARK_RIDER),
    BUCKLAND(NEUTRAL, DARK_RIDER),
    SOUTH_MIRKWOOD(EVIL, EAGLE),
    MINAS_MORGUL(EVIL, ELVEN_ARCHER),
    LEBENNIN(GOOD, ELVEN_ARCHER),
    THE_WOLD(GOOD, DARK_RIDER),
    SOUTH_DOWNS(NEUTRAL, DARK_RIDER),
    BARAD_DUR(EVIL, DARK_RIDER),
    WEATHER_HILLS(NEUTRAL, ELVEN_ARCHER),
    DUNLAND(NEUTRAL, EAGLE),
    ENEDWAITH(NEUTRAL, DARK_RIDER),
    GLADDEN_FIELDS(NEUTRAL, EAGLE),
    EASTERN_ANGMAR(EVIL, ELVEN_ARCHER),
    DRUWAITH_IAUR(NEUTRAL, DARK_RIDER),
    KHAND(NEUTRAL, EAGLE),
    HARONDOR(NEUTRAL, DARK_RIDER),
    EMYN_MUIL(EVIL, ELVEN_ARCHER),
    MORIA(EVIL, EAGLE),
    FANGORN(EVIL, DARK_RIDER),
    RHUDAUR(GOOD, ELVEN_ARCHER),
    CARROCK(NEUTRAL, EAGLE),
    RHUN_HILLS(NEUTRAL, ELVEN_ARCHER),
    LORIEN(GOOD, DARK_RIDER),
    FORODWAITH(NEUTRAL, DARK_RIDER),
    ANDUIN_VALLEY(NEUTRAL, ELVEN_ARCHER),
    ANGMAR(NEUTRAL, EAGLE),
    BELFALAS(GOOD, EAGLE),
    NORTH_DOWNS(NEUTRAL, DARK_RIDER),
    ANDRAST(GOOD, EAGLE),
    GORGOROTH(EVIL, ELVEN_ARCHER),
    WEST_ROHAN(GOOD, ELVEN_ARCHER),
    NORTH_MIRKWOOD(GOOD, DARK_RIDER),
    EVENDIM_HILLS(GOOD, ELVEN_ARCHER),
    VALE_OF_ERECH(GOOD, EAGLE),
    MINAS_TIRITH(GOOD, ELVEN_ARCHER),
    SOUTH_ITHILIEN(NEUTRAL, ELVEN_ARCHER),
    LAMEDON(GOOD, DARK_RIDER),
    NURN(NEUTRAL, DARK_RIDER),
    MITHLOND(NEUTRAL, EAGLE),
    WITHERED_HEATH(EVIL, ELVEN_ARCHER),
    LUNE_VALLEY(NEUTRAL, ELVEN_ARCHER),
    FORNOST(GOOD, EAGLE),
    TOWER_HILLS(NEUTRAL, EAGLE),
    FORLINDON(NEUTRAL, EAGLE),
    MOUNT_DOOM(EVIL, DARK_RIDER),
    BORDERLANDS(NEUTRAL, ELVEN_ARCHER),
    HARLINDON(NEUTRAL, ELVEN_ARCHER),;

    private ClassType type;
    private BattalionType battalion;
    private TerritoryCard[] adjacents;

    public static List<Territory> TERRITORIES = new ArrayList<>();

    private TerritoryCard(ClassType t, BattalionType b) {
        this.type = t;
        this.battalion = b;
    }

    public ClassType type() {
        return type;
    }

    public BattalionType battalionType() {
        return battalion;
    }

    public TerritoryCard[] adjacents() {
        return adjacents;
    }

    public static void init() {

        TERRITORIES.clear();

        for (TerritoryCard c : TerritoryCard.values()) {
            if (c.type() != null) {
                TERRITORIES.add(new Territory(c));
            }
        }

        for (TerritoryCard c : TerritoryCard.values()) {
            switch (c) {
                case WILD_CARD_1:
                    c.adjacents = new TerritoryCard[]{};
                    break;
                case WILD_CARD_2:
                    c.adjacents = new TerritoryCard[]{};
                    break;
                case ESGAROTH:
                    c.adjacents = new TerritoryCard[]{WITHERED_HEATH, NORTH_MIRKWOOD};
                    break;
                case HARAD:
                    c.adjacents = new TerritoryCard[]{HARONDOR, DEEP_HARAD, NEAR_HARAD};
                    break;
                case EREGION:
                    c.adjacents = new TerritoryCard[]{RHUDAUR, MORIA, DUNLAND};
                    break;
                case ITHILIEN:
                    c.adjacents = new TerritoryCard[]{DEAD_MARSHES, MINAS_TIRITH, SOUTH_ITHILIEN, MINAS_MORGUL};
                    break;
                case THE_SHIRE:
                    c.adjacents = new TerritoryCard[]{TOWER_HILLS, BUCKLAND};
                    break;
                case OLD_FOREST:
                    c.adjacents = new TerritoryCard[]{FORNOST, SOUTH_DOWNS, BUCKLAND, WEATHER_HILLS};
                    break;
                case BROWN_LANDS:
                    c.adjacents = new TerritoryCard[]{SOUTH_RHUN, EASTERN_MIRKWOOD, SOUTH_MIRKWOOD, EMYN_MUIL, DEAD_MARSHES, RHUN_HILLS};
                    break;
                case EASTERN_MIRKWOOD:
                    c.adjacents = new TerritoryCard[]{BROWN_LANDS, SOUTH_MIRKWOOD, ANDUIN_VALLEY, NORTH_MIRKWOOD};
                    break;
                case DEEP_HARAD:
                    c.adjacents = new TerritoryCard[]{UMBAR, HARAD};
                    break;
                case MINHIRIATH:
                    c.adjacents = new TerritoryCard[]{SOUTH_DOWNS, MITHLOND, DUNLAND, ENEDWAITH, BELFALAS, UMBAR};
                    break;
                case UMBAR:
                    c.adjacents = new TerritoryCard[]{DEEP_HARAD, BELFALAS, MINHIRIATH};
                    break;
                case DEAD_MARSHES:
                    c.adjacents = new TerritoryCard[]{BROWN_LANDS, EMYN_MUIL, ITHILIEN, UDUN_VALE};
                    break;
                case GAP_OF_ROHAN:
                    c.adjacents = new TerritoryCard[]{THE_WOLD, FANGORN, WEST_ROHAN, MINAS_TIRITH, ENEDWAITH};
                    break;
                case SOUTH_RHUN:
                    c.adjacents = new TerritoryCard[]{NORTH_RHUN, BROWN_LANDS};
                    break;
                case NEAR_HARAD:
                    c.adjacents = new TerritoryCard[]{KHAND, HARAD};
                    break;
                case NORTH_RHUN:
                    c.adjacents = new TerritoryCard[]{WITHERED_HEATH, SOUTH_RHUN, FORODWAITH};
                    break;
                case UDUN_VALE:
                    c.adjacents = new TerritoryCard[]{DEAD_MARSHES, MOUNT_DOOM};
                    break;
                case ANFALAS:
                    c.adjacents = new TerritoryCard[]{ANDRAST, VALE_OF_ERECH, DRUWAITH_IAUR};
                    break;
                case BUCKLAND:
                    c.adjacents = new TerritoryCard[]{THE_SHIRE, OLD_FOREST, SOUTH_DOWNS, FORNOST};
                    break;
                case SOUTH_MIRKWOOD:
                    c.adjacents = new TerritoryCard[]{ANDUIN_VALLEY, EMYN_MUIL, EASTERN_MIRKWOOD, BROWN_LANDS};
                    break;
                case MINAS_MORGUL:
                    c.adjacents = new TerritoryCard[]{GORGOROTH, ITHILIEN};
                    break;
                case LEBENNIN:
                    c.adjacents = new TerritoryCard[]{MINAS_TIRITH, LAMEDON, BELFALAS};
                    break;
                case THE_WOLD:
                    c.adjacents = new TerritoryCard[]{FANGORN, GAP_OF_ROHAN, EMYN_MUIL, LORIEN};
                    break;
                case SOUTH_DOWNS:
                    c.adjacents = new TerritoryCard[]{WEATHER_HILLS, OLD_FOREST, BUCKLAND, MINHIRIATH};
                    break;
                case BARAD_DUR:
                    c.adjacents = new TerritoryCard[]{GORGOROTH, MOUNT_DOOM};
                    break;
                case WEATHER_HILLS:
                    c.adjacents = new TerritoryCard[]{RHUDAUR, BORDERLANDS, FORNOST, OLD_FOREST, SOUTH_DOWNS};
                    break;
                case DUNLAND:
                    c.adjacents = new TerritoryCard[]{EREGION, ENEDWAITH, MINHIRIATH};
                    break;
                case ENEDWAITH:
                    c.adjacents = new TerritoryCard[]{GAP_OF_ROHAN, DUNLAND, MINHIRIATH};
                    break;
                case GLADDEN_FIELDS:
                    c.adjacents = new TerritoryCard[]{ANDUIN_VALLEY, MORIA, LORIEN};
                    break;
                case EASTERN_ANGMAR:
                    c.adjacents = new TerritoryCard[]{CARROCK, FORODWAITH};
                    break;
                case DRUWAITH_IAUR:
                    c.adjacents = new TerritoryCard[]{WEST_ROHAN, ANFALAS};
                    break;
                case KHAND:
                    c.adjacents = new TerritoryCard[]{NEAR_HARAD};
                    break;
                case HARONDOR:
                    c.adjacents = new TerritoryCard[]{HARAD, SOUTH_ITHILIEN};
                    break;
                case EMYN_MUIL:
                    c.adjacents = new TerritoryCard[]{ANDUIN_VALLEY, SOUTH_MIRKWOOD, BROWN_LANDS, THE_WOLD, DEAD_MARSHES};
                    break;
                case MORIA:
                    c.adjacents = new TerritoryCard[]{EREGION, GLADDEN_FIELDS};
                    break;
                case FANGORN:
                    c.adjacents = new TerritoryCard[]{LORIEN, THE_WOLD, GAP_OF_ROHAN};
                    break;
                case RHUDAUR:
                    c.adjacents = new TerritoryCard[]{CARROCK, WEATHER_HILLS, EREGION};
                    break;
                case CARROCK:
                    c.adjacents = new TerritoryCard[]{EASTERN_ANGMAR, RHUDAUR, ANDUIN_VALLEY, NORTH_MIRKWOOD};
                    break;
                case RHUN_HILLS:
                    c.adjacents = new TerritoryCard[]{BROWN_LANDS};
                    break;
                case LORIEN:
                    c.adjacents = new TerritoryCard[]{GLADDEN_FIELDS, FANGORN, THE_WOLD};
                    break;
                case FORODWAITH:
                    c.adjacents = new TerritoryCard[]{NORTH_RHUN, WITHERED_HEATH, EASTERN_ANGMAR, ANGMAR, BORDERLANDS, MITHLOND};
                    break;
                case ANDUIN_VALLEY:
                    c.adjacents = new TerritoryCard[]{CARROCK, EASTERN_MIRKWOOD, SOUTH_MIRKWOOD, EMYN_MUIL, GLADDEN_FIELDS};
                    break;
                case ANGMAR:
                    c.adjacents = new TerritoryCard[]{FORODWAITH, BORDERLANDS};
                    break;
                case BELFALAS:
                    c.adjacents = new TerritoryCard[]{LEBENNIN, LAMEDON, UMBAR, MINHIRIATH};
                    break;
                case NORTH_DOWNS:
                    c.adjacents = new TerritoryCard[]{BORDERLANDS, FORNOST};
                    break;
                case ANDRAST:
                    c.adjacents = new TerritoryCard[]{ANFALAS};
                    break;
                case GORGOROTH:
                    c.adjacents = new TerritoryCard[]{NURN, MOUNT_DOOM, BARAD_DUR, MINAS_MORGUL};
                    break;
                case WEST_ROHAN:
                    c.adjacents = new TerritoryCard[]{DRUWAITH_IAUR, GAP_OF_ROHAN};
                    break;
                case NORTH_MIRKWOOD:
                    c.adjacents = new TerritoryCard[]{CARROCK, ESGAROTH, EASTERN_MIRKWOOD};
                    break;
                case EVENDIM_HILLS:
                    c.adjacents = new TerritoryCard[]{TOWER_HILLS, BORDERLANDS, LUNE_VALLEY};
                    break;
                case VALE_OF_ERECH:
                    c.adjacents = new TerritoryCard[]{LAMEDON, ANFALAS};
                    break;
                case MINAS_TIRITH:
                    c.adjacents = new TerritoryCard[]{GAP_OF_ROHAN, ITHILIEN, LEBENNIN};
                    break;
                case SOUTH_ITHILIEN:
                    c.adjacents = new TerritoryCard[]{HARONDOR, ITHILIEN};
                    break;
                case LAMEDON:
                    c.adjacents = new TerritoryCard[]{VALE_OF_ERECH, BELFALAS, LEBENNIN};
                    break;
                case NURN:
                    c.adjacents = new TerritoryCard[]{GORGOROTH};
                    break;
                case MITHLOND:
                    c.adjacents = new TerritoryCard[]{TOWER_HILLS, LUNE_VALLEY, FORLINDON, FORODWAITH, MINHIRIATH};
                    break;
                case WITHERED_HEATH:
                    c.adjacents = new TerritoryCard[]{FORODWAITH, ESGAROTH, NORTH_RHUN};
                    break;
                case LUNE_VALLEY:
                    c.adjacents = new TerritoryCard[]{EVENDIM_HILLS, BORDERLANDS, TOWER_HILLS, MITHLOND};
                    break;
                case FORNOST:
                    c.adjacents = new TerritoryCard[]{NORTH_DOWNS, WEATHER_HILLS, BORDERLANDS, OLD_FOREST, BUCKLAND};
                    break;
                case TOWER_HILLS:
                    c.adjacents = new TerritoryCard[]{EVENDIM_HILLS, LUNE_VALLEY, THE_SHIRE, MITHLOND};
                    break;
                case FORLINDON:
                    c.adjacents = new TerritoryCard[]{MITHLOND};
                    break;
                case MOUNT_DOOM:
                    c.adjacents = new TerritoryCard[]{BARAD_DUR, GORGOROTH, UDUN_VALE};
                    break;
                case BORDERLANDS:
                    c.adjacents = new TerritoryCard[]{ANGMAR, FORODWAITH, WEATHER_HILLS, FORNOST, NORTH_DOWNS, EVENDIM_HILLS, LUNE_VALLEY};
                    break;
                case HARLINDON:
                    c.adjacents = new TerritoryCard[]{MITHLOND};
                    break;

            };
        }

    }

    public static Territory getTerritory(String name) {
        for (Territory t : TERRITORIES) {
            String n = t.card().toString().toLowerCase().replace("_", " ");
            if (n.equals(name)) {
                return t;
            }
        }
        return null;
    }

    public static List<TerritoryCard> cardsOfClass(ClassType t) {
        List<TerritoryCard> cards = new ArrayList<>();
        for (TerritoryCard c : TerritoryCard.values()) {
            if (c.type == t) {
                cards.add(c);
            }
        }
        return randomCards(cards, cards.size());
    }

    public static List<TerritoryCard> randomCards(List<TerritoryCard> cards, int count) {
        List<TerritoryCard> shuffled = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            int r = rand.nextInt(cards.size());
            TerritoryCard c = cards.remove(r);
            shuffled.add(c);
        }

        return shuffled;
    }

    public static void addBattalion(TerritoryCard card, Battalion b) {
        for (Territory t : TERRITORIES) {
            if (t.card() == card) {
                t.battalions.add(b);
            }
        }
    }

    public static Territory findRandomEmptyTerritory() {
        Random rand = new Random();
        List<Territory> empties = new ArrayList<>();
        for (Territory t : TERRITORIES) {
            if (t.battalions.isEmpty()) {
                empties.add(t);
            }
        }
        if (empties.isEmpty()) {
            return null;
        }
        int r = rand.nextInt(empties.size());
        return empties.get(r);
    }

    public static List<Territory> getClaimedTerritories(ArmyType at) {
        List<Territory> terrs = new ArrayList<>();
        for (Territory t : TERRITORIES) {
            if (!t.battalions.isEmpty() && t.battalions.get(0).army.armyType == at) {
                terrs.add(t);
            }
        }
        return terrs;
    }

    public static List<Territory> getClaimedTerritoriesWithLeaders(ArmyType at) {
        List<Territory> terrs = new ArrayList<>();
        for (Territory t : TERRITORIES) {
            if (!t.battalions.isEmpty() && t.battalions.get(0).army.armyType == at) {
                if (t.leader != null) {
                    terrs.add(t);
                }
            }
        }
        return terrs;
    }
}
