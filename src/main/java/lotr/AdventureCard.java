package lotr;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static lotr.AdventureCard.Type.EVENT;
import static lotr.AdventureCard.Type.MISSION;
import static lotr.AdventureCard.Type.POWER;

public enum AdventureCard {

    APPOINT_A_SECOND_LEADER_1(EVENT, "PLAY IMMEDIATELY.", "Place a second Leader in any Territory you control. If you already have 2 Leaders in play, discard this card."),
    APPOINT_A_SECOND_LEADER_2(EVENT, "PLAY IMMEDIATELY.", "Place a second Leader in any Territory you control. If you already have 2 Leaders in play, discard this card."),
    APPOINT_A_SECOND_LEADER_3(EVENT, "PLAY IMMEDIATELY.", "Place a second Leader in any Territory you control. If you already have 2 Leaders in play, discard this card."),
    APPOINT_A_SECOND_LEADER_4(EVENT, "PLAY IMMEDIATELY.", "Place a second Leader in any Territory you control. If you already have 2 Leaders in play, discard this card."),
    WINTER_STORMS(EVENT, "PLAY IMMEDIATELY.", "Draw the top 3 Territory cards. No player can invade into or out of these Territories until the start of your next turn."),
    RAIDERS(EVENT, "PLAY IMMEDIATELY.", "Draw the top 3 Territory cards. Half of the battalions in each of these Territories are destroyed. Round any losses down. Example: 5 battallons = lose 2 battalions. (1 battallon must always remain.)"),
    MUSTERING_OF_MIDDLE_EARTH(EVENT, "PLAY IMMEDIATELY.", "Draw the top 3 Territory cards. Each of these Territories receive 3 extra battalions."),
    LOCAL_UPRISING(EVENT, "PLAY IMMEDIATELY.", "Draw a Territory card and secretly look at it. When you control that Territory, reveal the card and add 4 battalions to that Territory."),
    CORSAIRS_OF_UMBAR(EVENT, "PLAY IMMEDIATELY.", "Until the start of your next turn, all seaborne invasions lose 3 battalions before they begin invading.  (1 battalion must always remain.)"),
    THE_BLACK_GATES_OPEN(EVENT, "PLAY IMMEDIATELY.", "If an Evil force controls Udun Vale, that player receives 10 extra battalions there. Otherwise, the Good force loses 2 battalions there. (1 battalion must always remain.)"),
    THEYVE_BROUGHT_A_CAVE_TROLL(EVENT, "PLAY IMMEDIATELY.", "If an evil force controls Moria, that player receive4s 2 extra battalions there, otherwise, the good force loses 2 battalions there. (1 battalion must always remain.)"),
    THE_ENTMOOT(EVENT, "PLAY IMMEDIATELY.", "If a Good force controls Fangorn, that player receives 2 extra battalions there. Otherwise, the Evil force loses 2 battalions there.  (1 battallon must always remain.)"),
    ARAGORN_ARRIVES(EVENT, "PLAY IMMEDIATELY.", "If a Good force controls Minas Tirith, that player receives 10 extra battalions there. Otherwise, the Evil force loses 2 battalions there.  (1 battalion must always remain.)"),
    //
    ELVEN_HALLS(POWER, null, null),
    MOUNT_DOOM(POWER, null, null),
    EREBOR(POWER, null, null),
    HOBBITON(POWER, null, null),
    HARLOND(POWER, null, null),
    GREY_HAVENS(POWER, null, null),
    WEATHERTOP(POWER, null, null),
    MOUNT_GUNDABAD(POWER, null, null),
    CARN_DUM(POWER, null, null),
    BREE(POWER, null, null),
    LOTHLORIEN(POWER, null, null),
    RAUROS(POWER, null, null),
    DAGORLAD(POWER, null, null),
    EASTERLING_ENCAMPMENT(POWER, null, null),
    OSGILIATH(POWER, null, null),
    ERECH(POWER, null, null),
    PELARGIR(POWER, null, null),
    DOL_AMROTH(POWER, null, null),
    HARADRIM_ENCAMPMENT(POWER, null, null),
    EDORAS(POWER, null, null),
    THARBAD(POWER, null, null),
    GATES_OF_MORIA(POWER, null, null),
    //
    GRIMA_WORMTONGUE_1(MISSION, "PLAY BEFORE INVADING A TERRITORY.", "Remove up to 2 enemy battalions from the Territory you are invading and add them to your invading Territory. (1 defending battalion must always"),
    GRIMA_WORMTONGUE_2(MISSION, "PLAY BEFORE INVADING A TERRITORY.", "Remove up to 2 enemy battalions from the Territory you are invading and add them to your invading Territory. (1 defending battalion must always"),
    COURAGE_ALONE_WILL_NOT_SAVE_YOU(MISSION, "PLAY AFTER AN ENEMY DECLARES AN INVASION ON ONE OF YOUR TERRITORIES.", "Gain 4 extra battalions in the Territory being invaded."),
    YOU_SHALL_NOT_PASS_1(MISSION, "PLAY AT THE START OF YOUR TURN.", "Choose one bridge to block until the start of your next turn. No battalions may cross the bridge unless accompanied by a Leader."),
    YOU_SHALL_NOT_PASS_2(MISSION, "PLAY AT THE START OF YOUR TURN.", "Choose one bridge to block until the start of your next turn. No battalions may cross the bridge unless accompanied by a Leader."),
    AMBUSH(MISSION, "PLAY BEFORE INVADING A TERRITORY.", "Gain 3 extra battalions in the Territory you are invading from."),
    THE_ENEMY_IS_AT_HAND(MISSION, "PLAY BEFORE INVADING A TERRITORY WITH A STRONGHOLD.", "The Stronghold loses its +1 advantage until the invasion ends."),
    MOVE_BY_NIGHT(MISSION, "PLAY AFTER YOUR LAST INVASION FOR THIS TURN.", "Make up to 3 extra redeployments."),
    THE_WAY_IS_UNDER_THE_MOUNTAINS_1(MISSION, "PLAY AFTER DECLARING AN INVASION.", "Invade from one Territory next to a mountain range into one other Territory next to the same mountain range."),
    THE_WAY_IS_UNDER_THE_MOUNTAINS_2(MISSION, "PLAY AFTER DECLARING AN INVASION.", "Invade from one Territory next to a mountain range into one other Territory next to the same mountain range."),
    VIOLENT_STORMS(MISSION, "PLAY AFTER A SEABORNE BATTLE IS DECLARED ON ONE OF YOUR TERRITORIES WITH A PORT.", "Destroy all attacking battalions in this battle  (not this invasion.)"),
    BORNE_TO_ANOTHER_PLACE(MISSION, "PLAY AT THE START OF YOUR TURN.", "Move one of your Leaders to any Territory you control. The Leader can then continue as normal."),
    A_BALROG_IS_COME(MISSION, "PLAY WHEN THE FELLOWSHIP ATTEMPTS TO LEAVE MORIA (AFTER THE DIE ROLL).", "Force the Fellowship to remain in Moria until the next turn."),
    BOROMIR_TRIES_TO_SEIZE_THE_ONE_RING(MISSION, "PLAY AS THE FELLOWSHIP ATTEMPTS TO LEAVE A TERRITORY (AFTER THE DIE ROLL, IF ANY).", "Force the Fellowship to remain in the same Territory until the next turn."),
    KNIFE_IN_THE_DARK(MISSION, "PLAY WHEN THE FELLOWSHIP ATTEMPTS TO LEAVE A TERRITORY WITH NO DIE ROLL.", "Force the Fellowship to roll a die and score higher than 3 to leave the Territory."),
    DANGEROUS_CROSSING(MISSION, "PLAY WHEN THE FELLOWSHIP ATTEMPTS TO LEAVE A TERRITORY WITH NO DIE ROLL.", "Force the Fellowship to roll a die and score higher than 3 to leave the Territory."),
    GOLLUM(MISSION, "PLAY WHEN THE FELLOWSHIP ATTEMPTS TO LEAVE A TERRITORY WITH NO DIE ROLL.", "Force the Fellowship to roll a die and score higher than 3 to leave the Territory."),
    HUNT_FOR_THE_RINGBEARER(MISSION, "PLAY AT THE START OF YOUR TURN.", "If you control the Territory with the Fellowship, place 4 battalions there. Otherwise, move the Fellowship forward 1 space (no need to roll the die)."),
    SIEGE_MACHINES(MISSION, "PLAY BEFORE INVADING A TERRITORY WITH A STRONGHOLD.", "The Stronghold loses its +1 advantage until the | invasion ends."),
    STRATEGIC_WITHDRAWAL(MISSION, "PLAY AFTER AN ENEMY DECLARES AN INVASION ON ONE OF YOUR TERRITORIES.", "You may move all but 1 battalion from your invaded Territory into an adjacent Territory that you control."),
    BLACK_SAILS(MISSION, "PLAY AFTER DECLARING A SEABORNE INVASION.", "Add 1 to your highest die roll for the entire invasion."),
    FARAMIR_FINDS_TTHE_RINGBEARER(MISSION, "PLAY AFTER THE FELLOWSHIP ROLLS THE DIE TO LEAVE A TERRITORY.", "Choose to either add or subtract 2 from the die roll."),
    CAPTURED_BY_ORCS(MISSION, "PLAY AFTER THE FELLOWSHIP ROLLS THE DIE TO LEAVE GORGOROTH.", "Subtract 1 from the die roll."),
    SHELOBS_LAIR(MISSION, "PLAY AFTER THE FELLOWSHIP ROLLS THE DIE TO LEAVE MINAS MORGUL.", "Subtract 2 from the die roll."),
    SMEAGOL(MISSION, "PLAY AFTER THE FELLOWSHIP ROLLS THE DIE TO LEAVE A TERRITORY.", "Add 2 to the die roll.");

    private Type type;
    private String text1;
    private String text2;

    private AdventureCard(Type t, String text1, String text2) {
        this.type = t;
        this.text1 = text1;
        this.text2 = text2;
    }

    public Type type() {
        return type;
    }

    public static enum Type {
        MISSION, EVENT, POWER;
    }

    public String getText1() {
        return text1;
    }

    public String getText2() {
        return text2;
    }

    public String title() {
        String name = this.toString();
        name = name.replace("_1", "").replace("_2", "").replace("_2", "").replace("_4", "").replace("_", " ");
        return name;
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
