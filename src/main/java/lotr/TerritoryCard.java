package lotr;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lotr.Constants.BattalionType;
import static lotr.Constants.BattalionType.DARK_RIDER;
import static lotr.Constants.BattalionType.EAGLE;
import static lotr.Constants.BattalionType.ELVEN_ARCHER;
import lotr.Constants.ClassType;
import static lotr.Constants.ClassType.EVIL;
import static lotr.Constants.ClassType.GOOD;
import static lotr.Constants.ClassType.NEUTRAL;

public enum TerritoryCard {

    WILD_CARD_1(null, null, null),
    WILD_CARD_2(null, null, null),
    ESGAROTH(NEUTRAL, EAGLE, Region.RHUN),
    HARAD(NEUTRAL, EAGLE, Region.HARADAWAITH),
    EREGION(NEUTRAL, ELVEN_ARCHER, Region.ROHAN),
    ITHILIEN(NEUTRAL, ELVEN_ARCHER, Region.GONDOR),
    THE_SHIRE(GOOD, DARK_RIDER, Region.ERIADOR),
    OLD_FOREST(NEUTRAL, ELVEN_ARCHER, Region.ARNOR),
    BROWN_LANDS(EVIL, EAGLE, Region.RHOVANION),
    EASTERN_MIRKWOOD(EVIL, DARK_RIDER, Region.MIRKWOOD),
    DEEP_HARAD(EVIL, EAGLE, Region.HARADAWAITH),
    MINHIRIATH(NEUTRAL, EAGLE, Region.ROHAN),
    UMBAR(EVIL, EAGLE, Region.HARADAWAITH),
    DEAD_MARSHES(EVIL, DARK_RIDER, Region.RHOVANION),
    GAP_OF_ROHAN(GOOD, EAGLE, Region.ROHAN),
    SOUTH_RHUN(NEUTRAL, DARK_RIDER, Region.RHUN),
    NEAR_HARAD(NEUTRAL, ELVEN_ARCHER, Region.HARADAWAITH),
    NORTH_RHUN(NEUTRAL, DARK_RIDER, Region.RHUN),
    UDUN_VALE(EVIL, ELVEN_ARCHER, Region.MORDOR),
    ANFALAS(GOOD, DARK_RIDER, Region.GONDOR),
    BUCKLAND(NEUTRAL, DARK_RIDER, Region.ARNOR),
    SOUTH_MIRKWOOD(EVIL, EAGLE, Region.MIRKWOOD),
    MINAS_MORGUL(EVIL, ELVEN_ARCHER, Region.MORDOR),
    LEBENNIN(GOOD, ELVEN_ARCHER, Region.GONDOR),
    THE_WOLD(GOOD, DARK_RIDER, Region.RHOVANION),
    SOUTH_DOWNS(NEUTRAL, DARK_RIDER, Region.ARNOR),
    BARAD_DUR(EVIL, DARK_RIDER, Region.MORDOR),
    WEATHER_HILLS(NEUTRAL, ELVEN_ARCHER, Region.ARNOR),
    DUNLAND(NEUTRAL, EAGLE, Region.ROHAN),
    ENEDWAITH(NEUTRAL, DARK_RIDER, Region.ROHAN),
    GLADDEN_FIELDS(NEUTRAL, EAGLE, Region.RHOVANION),
    EASTERN_ANGMAR(EVIL, ELVEN_ARCHER, Region.ARNOR),
    DRUWAITH_IAUR(NEUTRAL, DARK_RIDER, Region.GONDOR),
    KHAND(NEUTRAL, EAGLE, Region.HARADAWAITH),
    HARONDOR(NEUTRAL, DARK_RIDER, Region.HARADAWAITH),
    EMYN_MUIL(EVIL, ELVEN_ARCHER, Region.RHOVANION),
    MORIA(EVIL, EAGLE, Region.RHOVANION),
    FANGORN(EVIL, DARK_RIDER, Region.ROHAN),
    RHUDAUR(GOOD, ELVEN_ARCHER, Region.ARNOR),
    CARROCK(NEUTRAL, EAGLE, Region.MIRKWOOD),
    RHUN_HILLS(NEUTRAL, ELVEN_ARCHER, Region.RHOVANION),
    LORIEN(GOOD, DARK_RIDER, Region.RHOVANION),
    FORODWAITH(NEUTRAL, DARK_RIDER, Region.ARNOR),
    ANDUIN_VALLEY(NEUTRAL, ELVEN_ARCHER, Region.MIRKWOOD),
    ANGMAR(NEUTRAL, EAGLE, Region.ARNOR),
    BELFALAS(GOOD, EAGLE, Region.GONDOR),
    NORTH_DOWNS(NEUTRAL, DARK_RIDER, Region.ARNOR),
    ANDRAST(GOOD, EAGLE, Region.GONDOR),
    GORGOROTH(EVIL, ELVEN_ARCHER, Region.MORDOR),
    WEST_ROHAN(GOOD, ELVEN_ARCHER, Region.ROHAN),
    NORTH_MIRKWOOD(GOOD, DARK_RIDER, Region.MIRKWOOD),
    EVENDIM_HILLS(GOOD, ELVEN_ARCHER, Region.ERIADOR),
    VALE_OF_ERECH(GOOD, EAGLE, Region.GONDOR),
    MINAS_TIRITH(GOOD, ELVEN_ARCHER, Region.GONDOR),
    SOUTH_ITHILIEN(NEUTRAL, ELVEN_ARCHER, Region.GONDOR),
    LAMEDON(GOOD, DARK_RIDER, Region.GONDOR),
    NURN(NEUTRAL, DARK_RIDER, Region.MORDOR),
    MITHLOND(NEUTRAL, EAGLE, Region.ERIADOR),
    WITHERED_HEATH(EVIL, ELVEN_ARCHER, Region.RHUN),
    LUNE_VALLEY(NEUTRAL, ELVEN_ARCHER, Region.ERIADOR),
    FORNOST(GOOD, EAGLE, Region.ARNOR),
    TOWER_HILLS(NEUTRAL, EAGLE, Region.ERIADOR),
    FORLINDON(NEUTRAL, EAGLE, Region.ARNOR),
    MOUNT_DOOM(EVIL, DARK_RIDER, Region.MORDOR),
    BORDERLANDS(NEUTRAL, ELVEN_ARCHER, Region.ARNOR),
    HARLINDON(NEUTRAL, ELVEN_ARCHER, Region.ERIADOR),;

    private ClassType type;
    private BattalionType battalion;
    private TerritoryCard[] adjacents;
    private Region region;

    private final String title;
    private final String capitalized;

    private TerritoryCard(ClassType t, BattalionType b, Region r) {
        this.type = t;
        this.battalion = b;
        this.region = r;

        String name = this.toString();
        this.title = name.replace("_1", "").replace("_2", "").replace("_2", "").replace("_4", "").replace("_", " ");

        char[] array = title().toLowerCase().toCharArray();
        array[0] = Character.toUpperCase(array[0]);
        for (int i = 1; i < array.length; i++) {
            if (Character.isWhitespace(array[i - 1])) {
                array[i] = Character.toUpperCase(array[i]);
            }
        }
        this.capitalized = new String(array);
    }

    public ClassType type() {
        return type;
    }

    public BattalionType battalionType() {
        return battalion;
    }

    public Region region() {
        return region;
    }

    public String title() {
        return this.title;
    }

    public String capitalized() {
        return this.capitalized;
    }

    public TerritoryCard[] adjacents() {
        return adjacents;
    }

    public static void init() {

        for (TerritoryCard c : TerritoryCard.values()) {

            if (c.region() != null) {
                Region r = c.region();
                r.territories().add(c);
            }

            switch (c) {
                case WILD_CARD_1:
                    c.adjacents = null;
                    break;
                case WILD_CARD_2:
                    c.adjacents = null;
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
                    c.adjacents = new TerritoryCard[]{TOWER_HILLS, LUNE_VALLEY, FORLINDON, FORODWAITH, MINHIRIATH, HARLINDON};
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

    public static TerritoryCard getTerritory(String name) {
        for (TerritoryCard t : TerritoryCard.values()) {
            String n = t.toString().toLowerCase().replace("_", " ");
            if (n.equals(name)) {
                return t;
            }
        }
        return null;
    }

    public static List<TerritoryCard> shuffledTerritoriesOfClass(ClassType t) {
        List<TerritoryCard> temp = new ArrayList<>();
        for (TerritoryCard c : TerritoryCard.values()) {
            if (c.type == t && c != WILD_CARD_1 && c != WILD_CARD_2) {
                temp.add(c);
            }
        }
        List<TerritoryCard> shuffled = new ArrayList<>();
        Random rand = new Random();
        while (!temp.isEmpty()) {
            int r = rand.nextInt(temp.size());
            TerritoryCard c = temp.remove(r);
            shuffled.add(c);
        }
        return shuffled;
    }
}
