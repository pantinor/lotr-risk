package lotr;

public enum Location {

    //strong holds
    MINES_OF_MORIA(TerritoryCard.MORIA, Region.RHOVANION, false),
    ANNUMINAS(TerritoryCard.EVENDIM_HILLS, Region.ERIADOR, false),
    ISENGARD(TerritoryCard.FANGORN, Region.ROHAN, false),
    HELMS_DEEP(TerritoryCard.WEST_ROHAN, Region.ROHAN, false),
    RIVENDELL(TerritoryCard.RHUDAUR, Region.ARNOR, false),
    DOL_GULDUR(TerritoryCard.SOUTH_MIRKWOOD, Region.MIRKWOOD, false),
    UDUN(TerritoryCard.UDUN_VALE, Region.MORDOR, false),
    BARAD_DUR(TerritoryCard.BARAD_DUR, Region.MORDOR, false),
    MINAS_MORGUL(TerritoryCard.MINAS_MORGUL, Region.MORDOR, false),
    MINAS_TIRITH(TerritoryCard.MINAS_TIRITH, Region.GONDOR, false),
    CITY_OF_THE_CORSAIRS(TerritoryCard.UMBAR, Region.HARADAWAITH, false),
    //sites of power
    HOBBITON(TerritoryCard.THE_SHIRE, Region.ERIADOR, true),
    ELVEN_HALLS(TerritoryCard.NORTH_MIRKWOOD, Region.MIRKWOOD, true),
    BREE(TerritoryCard.FORNOST, Region.ARNOR, true),
    CARN_DUM(TerritoryCard.FORODWAITH, Region.ARNOR, true),
    MOUNT_GUNDABAD(TerritoryCard.EASTERN_ANGMAR, Region.ARNOR, true),
    GREY_HAVENS(TerritoryCard.MITHLOND, Region.ERIADOR, true),
    THARAD(TerritoryCard.MINHIRIATH, Region.ROHAN, true),
    HARLOND(TerritoryCard.HARLINDON, Region.ERIADOR, true),
    LOTHLORIEN(TerritoryCard.LORIEN, Region.RHOVANION, true),
    WEATHERTOP(TerritoryCard.WEATHER_HILLS, Region.ARNOR, true),
    GATES_OF_MORIA(TerritoryCard.EREGION, Region.ROHAN, true),
    EREBOR(TerritoryCard.ESGAROTH, Region.RHUN, true),
    EDORAS(TerritoryCard.GAP_OF_ROHAN, Region.ROHAN, true),
    EASTERLING_ENCAMPMENT(TerritoryCard.BROWN_LANDS, Region.RHOVANION, true),
    OSGILIATH(TerritoryCard.ITHILIEN, Region.GONDOR, true),
    PELARGIR(TerritoryCard.LEBENNIN, Region.GONDOR, true),
    HARADRIM_ENCAMPMENT(TerritoryCard.KHAND, Region.HARADAWAITH, true),
    ERECH(TerritoryCard.VALE_OF_ERECH, Region.GONDOR, true),
    RAUROS(TerritoryCard.THE_WOLD, Region.RHOVANION, true),
    DOL_AMROTH(TerritoryCard.BELFALAS, Region.GONDOR, true),
    DAGORLAD(TerritoryCard.DEAD_MARSHES, Region.RHOVANION, true),
    MOUNT_DOOM(TerritoryCard.MOUNT_DOOM, Region.MORDOR, true),;

    ;

    private TerritoryCard territory;
    private Region region;
    boolean siteOfPower;
    private final String title;
    private final String capitalized;

    private Location(TerritoryCard t, Region r, boolean sop) {
        this.territory = t;
        this.region = r;
        this.siteOfPower = sop;

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

    public TerritoryCard getTerritory() {
        return territory;
    }

    public Region getRegion() {
        return region;
    }

    public String title() {
        return this.title;
    }

    public String capitalized() {
        return this.capitalized;
    }

    public boolean isSiteOfPower() {
        return siteOfPower;
    }

    public static Location getStronghold(TerritoryCard c) {
        for (Location l : Location.values()) {
            if (!l.isSiteOfPower() && l.territory == c) {
                return l;
            }
        }
        return null;
    }
    
    public static Location getSiteOfPower(TerritoryCard c) {
        for (Location l : Location.values()) {
            if (l.isSiteOfPower() && l.territory == c) {
                return l;
            }
        }
        return null;
    }

}
