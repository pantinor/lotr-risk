package lotr;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static lotr.AdventureCard.Type.EVENT;
import static lotr.AdventureCard.Type.MISSION;
import static lotr.AdventureCard.Type.POWER;

public enum AdventureCard {

    ELVEN_HALLS(MISSION, null, TerritoryCard.NORTH_MIRKWOOD, 1, 2, false),
    MOUNT_DOOM(MISSION, null, TerritoryCard.NORTH_MIRKWOOD, 2, 2, false),
    EREBOR(MISSION, Region.RHUN, TerritoryCard.ESGAROTH, 2, 4, false),
    HOBBITON(MISSION, Region.ERIADOR, TerritoryCard.THE_SHIRE, 2, 2, false),
    HARLOND(MISSION, Region.ERIADOR, TerritoryCard.HARLINDON, 0, 0, true),
    GREY_HAVENS(MISSION, Region.ERIADOR, TerritoryCard.MITHLOND, 4, 2, false),
    WEATHERTOP(MISSION, Region.ARNOR, TerritoryCard.WEATHER_HILLS, 2, 2, false),
    MOUNT_GUNDABAD(MISSION, Region.ARNOR, TerritoryCard.EASTERN_ANGMAR, 4, 4, false),
    CARN_DUM(MISSION, Region.ARNOR, TerritoryCard.FORODWAITH, 2, 2, false),
    BREE(MISSION, Region.ARNOR, TerritoryCard.FORNOST, 6, 2, false),
    LOTHLORIEN(MISSION, Region.RHOVANION, TerritoryCard.LORIEN, 0, 0, true),
    RAUROS(MISSION, Region.RHOVANION, TerritoryCard.THE_WOLD, 2, 2, false),
    DAGORLAD(MISSION, Region.RHOVANION, TerritoryCard.DEAD_MARSHES, 2, 2, false),
    EASTERLING_ENCAMPMENT(MISSION, Region.RHOVANION, TerritoryCard.BROWN_LANDS, 2, 6, false),
    OSGILIATH(MISSION, Region.GONDOR, TerritoryCard.ITHILIEN, 4, 2, false),
    ERECH(MISSION, Region.GONDOR, TerritoryCard.VALE_OF_ERECH, 6, 6, false),
    PELARGIR(MISSION, Region.GONDOR, TerritoryCard.LEBENNIN, 5, 5, false),
    DOL_AMROTH(MISSION, null, TerritoryCard.BELFALAS, 2, 2, false),
    HARADRIM_ENCAMPMENT(MISSION, Region.HARADAWAITH, TerritoryCard.KHAND, 4, 2, false),
    EDORAS(MISSION, Region.ROHAN, TerritoryCard.GAP_OF_ROHAN, 0, 0, true),
    THARBAD(MISSION, Region.ROHAN, TerritoryCard.MINHIRIATH, 4, 4, false),
    GATES_OF_MORIA(MISSION, Region.ROHAN, TerritoryCard.EREGION, 4, 2, false),
    //
    APPOINT_A_SECOND_LEADER_1(EVENT, "PLAY IMMEDIATELY.", "Place a second Leader in any Territory you control. If you already have 2 Leaders in play, discard this card."),
    APPOINT_A_SECOND_LEADER_2(EVENT, "PLAY IMMEDIATELY.", "Place a second Leader in any Territory you control. If you already have 2 Leaders in play, discard this card."),
    APPOINT_A_SECOND_LEADER_3(EVENT, "PLAY IMMEDIATELY.", "Place a second Leader in any Territory you control. If you already have 2 Leaders in play, discard this card."),
    APPOINT_A_SECOND_LEADER_4(EVENT, "PLAY IMMEDIATELY.", "Place a second Leader in any Territory you control. If you already have 2 Leaders in play, discard this card."),
    WINTER_STORMS(EVENT, "PLAY IMMEDIATELY.", "Draw the top 3 Territory cards. No player can invade into or out of these Territories until the start of your next turn."),
    RAIDERS(EVENT, "PLAY IMMEDIATELY.", "Draw the top 3 Territory cards. Half of the battalions in each of these Territories are destroyed. Round any losses down. Example: 5 battallons = lose 2 battalions. 1 battallon must always remain."),
    MUSTERING_OF_MIDDLE_EARTH(EVENT, "PLAY IMMEDIATELY.", "Draw the top 3 Territory cards. Each of these Territories receive 3 extra battalions."),
    LOCAL_UPRISING(EVENT, "PLAY IMMEDIATELY.", "Draw a Territory card and secretly look at it. When you control that Territory, reveal the card and add 4 battalions to that Territory."),
    CORSAIRS_OF_UMBAR(EVENT, "PLAY IMMEDIATELY.", "Until the start of your next turn, all seaborne invasions lose 3 battalions before they begin invading.  1 battalion must always remain."),
    THE_BLACK_GATES_OPEN(EVENT, "PLAY IMMEDIATELY.", "If an Evil force controls Udun Vale, that player receives 10 extra battalions there. Otherwise, the Good force loses 2 battalions there. 1 battalion must always remain."),
    THEYVE_BROUGHT_A_CAVE_TROLL(EVENT, "PLAY IMMEDIATELY.", "If an evil force controls Moria, that player receive4s 2 extra battalions there, otherwise, the good force loses 2 battalions there. 1 battalion must always remain."),
    THE_ENTMOOT(EVENT, "PLAY IMMEDIATELY.", "If a Good force controls Fangorn, that player receives 2 extra battalions there. Otherwise, the Evil force loses 2 battalions there.  1 battallon must always remain."),
    ARAGORN_ARRIVES(EVENT, "PLAY IMMEDIATELY.", "If a Good force controls Minas Tirith, that player receives 10 extra battalions there. Otherwise, the Evil force loses 2 battalions there.  1 battalion must always remain."),
    //
    GRIMA_WORMTONGUE_1(POWER, "PLAY BEFORE INVADING A TERRITORY.", "Remove up to 2 enemy battalions from the Territory you are invading and add them to your invading Territory. 1 defending battalion must always remain."),
    GRIMA_WORMTONGUE_2(POWER, "PLAY BEFORE INVADING A TERRITORY.", "Remove up to 2 enemy battalions from the Territory you are invading and add them to your invading Territory. 1 defending battalion must always remain."),
    COURAGE_ALONE_WILL_NOT_SAVE_YOU(POWER, "PLAY AFTER AN ENEMY DECLARES AN INVASION ON ONE OF YOUR TERRITORIES.", "Gain 4 extra battalions in the Territory being invaded."),
    YOU_SHALL_NOT_PASS_1(POWER, "PLAY AT THE START OF YOUR TURN.", "Choose one bridge to block until the start of your next turn. No battalions may cross the bridge unless accompanied by a Leader."),
    YOU_SHALL_NOT_PASS_2(POWER, "PLAY AT THE START OF YOUR TURN.", "Choose one bridge to block until the start of your next turn. No battalions may cross the bridge unless accompanied by a Leader."),
    AMBUSH(POWER, "PLAY BEFORE INVADING A TERRITORY.", "Gain 3 extra battalions in the Territory you are invading from."),
    THE_ENEMY_IS_AT_HAND(POWER, "PLAY BEFORE INVADING A TERRITORY WITH A STRONGHOLD.", "The Stronghold loses its +1 advantage until the invasion ends."),
    SIEGE_MACHINES(POWER, "PLAY BEFORE INVADING A TERRITORY WITH A STRONGHOLD.", "The Stronghold loses its +1 advantage until the invasion ends."),
    MOVE_BY_NIGHT(POWER, "PLAY AFTER YOUR LAST INVASION FOR THIS TURN.", "Make up to 3 extra redeployments."),
    THE_WAY_IS_UNDER_THE_MOUNTAINS_1(POWER, "PLAY AFTER DECLARING AN INVASION.", "Invade from one Territory next to a mountain range into one other Territory next to the same mountain range."),
    THE_WAY_IS_UNDER_THE_MOUNTAINS_2(POWER, "PLAY AFTER DECLARING AN INVASION.", "Invade from one Territory next to a mountain range into one other Territory next to the same mountain range."),
    VIOLENT_STORMS(POWER, "PLAY AFTER A SEABORNE BATTLE IS DECLARED ON ONE OF YOUR TERRITORIES WITH A PORT.", "Destroy all attacking battalions in this battle  not this invasion."),
    BORNE_TO_ANOTHER_PLACE(POWER, "PLAY AT THE START OF YOUR TURN.", "Move one of your Leaders to any Territory you control. The Leader can then continue as normal."),
    HUNT_FOR_THE_RINGBEARER(POWER, "PLAY AT THE START OF YOUR TURN.", "If you control the Territory with the Fellowship, place 4 battalions there. Otherwise, move the Fellowship forward 1 space (no need to roll the die)."),
    STRATEGIC_WITHDRAWAL(POWER, "PLAY AFTER AN ENEMY DECLARES AN INVASION ON ONE OF YOUR TERRITORIES.", "You may move all but 1 battalion from your invaded Territory into an adjacent Territory that you control."),
    BLACK_SAILS(POWER, "PLAY AFTER DECLARING A SEABORNE INVASION.", "Add 1 to your highest die roll for the entire invasion."),
    A_BALROG_IS_COME(POWER, "PLAY WHEN THE FELLOWSHIP ATTEMPTS TO LEAVE MORIA (AFTER THE DIE ROLL).", "Force the Fellowship to remain in Moria until the next turn."),
    BOROMIR_TRIES_TO_SEIZE_THE_ONE_RING(POWER, "PLAY AS THE FELLOWSHIP ATTEMPTS TO LEAVE A TERRITORY (AFTER THE DIE ROLL, IF ANY).", "Force the Fellowship to remain in the same Territory until the next turn."),
    KNIFE_IN_THE_DARK(POWER, "PLAY WHEN THE FELLOWSHIP ATTEMPTS TO LEAVE A TERRITORY WITH NO DIE ROLL.", "Force the Fellowship to roll a die and score higher than 3 to leave the Territory."),
    DANGEROUS_CROSSING(POWER, "PLAY WHEN THE FELLOWSHIP ATTEMPTS TO LEAVE A TERRITORY WITH NO DIE ROLL.", "Force the Fellowship to roll a die and score higher than 3 to leave the Territory."),
    GOLLUM(POWER, "PLAY WHEN THE FELLOWSHIP ATTEMPTS TO LEAVE A TERRITORY WITH NO DIE ROLL.", "Force the Fellowship to roll a die and score higher than 3 to leave the Territory."),
    FARAMIR_FINDS_THE_RINGBEARER(POWER, "PLAY AFTER THE FELLOWSHIP ROLLS THE DIE TO LEAVE A TERRITORY.", "Choose to either add or subtract 2 from the die roll."),
    CAPTURED_BY_ORCS(POWER, "PLAY AFTER THE FELLOWSHIP ROLLS THE DIE TO LEAVE GORGOROTH.", "Subtract 1 from the die roll."),
    SHELOBS_LAIR(POWER, "PLAY AFTER THE FELLOWSHIP ROLLS THE DIE TO LEAVE MINAS MORGUL.", "Subtract 2 from the die roll."),
    SMEAGOL(POWER, "PLAY AFTER THE FELLOWSHIP ROLLS THE DIE TO LEAVE A TERRITORY.", "Add 2 to the die roll.");

    public static enum Type {
        MISSION, EVENT, POWER;
    }

    private Type type;
    private String text1;
    private String text2;

    private final Region region;
    private final TerritoryCard territory;
    private int evilBonus;
    private int goodBonus;
    private boolean drawExtraCard;

    private final String title;
    private final String capitalized;
    private boolean used;

    private AdventureCard(Type t, Region region, TerritoryCard terr, int eb, int gb, boolean drawExtraCard) {
        this.type = t;
        this.territory = terr;
        this.region = region;
        this.evilBonus = eb;
        this.goodBonus = gb;
        this.drawExtraCard = drawExtraCard;

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

    private AdventureCard(Type t, String text1, String text2) {
        this.type = t;
        this.text1 = text1;
        this.text2 = text2;
        this.territory = null;
        this.region = null;

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

    public Region region() {
        return region;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public TerritoryCard territory() {
        return territory;
    }

    public int goodBonus() {
        return goodBonus;
    }

    public int evilBonus() {
        return evilBonus;
    }

    public boolean drawExtraCard() {
        return drawExtraCard;
    }

    public Type type() {
        return type;
    }

    public String text1() {
        return text1;
    }

    public String text2() {
        return text2;
    }

    public String title() {
        return this.title;
    }

    public String capitalized() {
        return this.capitalized;
    }

    public static List<AdventureCard> shuffledCards() {
        List<AdventureCard> temp = new ArrayList<>();
        for (AdventureCard c : AdventureCard.values()) {
            temp.add(c);
        }
        List<AdventureCard> shuffled = new ArrayList<>();
        Random rand = new Random();
        while (!temp.isEmpty()) {
            int r = rand.nextInt(temp.size());
            AdventureCard c = temp.remove(r);
            shuffled.add(c);
        }
        return shuffled;
    }

    public static List<AdventureCard> shuffledCardsWithoutEvents() {
        List<AdventureCard> temp = new ArrayList<>();
        for (AdventureCard c : AdventureCard.values()) {
            if (c.type != Type.EVENT) {
                temp.add(c);
            }
        }
        List<AdventureCard> shuffled = new ArrayList<>();
        Random rand = new Random();
        while (!temp.isEmpty()) {
            int r = rand.nextInt(temp.size());
            AdventureCard c = temp.remove(r);
            shuffled.add(c);
        }
        return shuffled;
    }

}
