package lotr;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lotr.Constants.ArmyType;
import lotr.Constants.ClassType;

public class Game {

    public static void main(String[] args) throws Exception {
        //4 player game

        TerritoryCard.init();

        Army red = new Army(ArmyType.RED, ClassType.EVIL, 45, false);
        Army green = new Army(ArmyType.GREEN, ClassType.GOOD, 45, false);
        Army grey = new Army(ArmyType.GREY, ClassType.EVIL, 45, false);
        Army yellow = new Army(ArmyType.YELLOW, ClassType.GOOD, 45, false);

        Game game = new Game();

        game.addArmy(red);
        game.addArmy(green);
        game.addArmy(grey);
        game.addArmy(yellow);

        List<TerritoryCard> evil = TerritoryCard.cardsOfClass(ClassType.EVIL);
        List<TerritoryCard> good = TerritoryCard.cardsOfClass(ClassType.GOOD);

        red.pickTerritories(evil, 8);
        grey.pickTerritories(evil, 8);
        green.pickTerritories(good, 8);
        yellow.pickTerritories(good, 8);

        List<TerritoryCard> deck = new ArrayList<>();
        Random rand = new Random();
        {
            for (TerritoryCard c : TerritoryCard.values()) {
                deck.add(c);
            }

            List<TerritoryCard> shuffled = new ArrayList<>();
            int count = deck.size();
            for (int i = 0; i < count; i++) {
                int r = rand.nextInt(deck.size());
                TerritoryCard c = deck.remove(r);
                shuffled.add(c);
            }

            deck = shuffled;
        }

        //claim empty territories
        Army[] armies = game.armies;
        int idx = rand.nextInt(4);
        while (true) {

            Army army = armies[idx];

            idx++;
            if (idx >= armies.length) {
                idx = 0;
            }

            Battalion b = army.battalions.remove(0);
            Territory t = TerritoryCard.findRandomEmptyTerritory();
            if (t != null) {
                t.battalions.add(b);
            } else {
                break;
            }

        }

        //reinforce territories
        while (true) {

            Army army = armies[idx];

            boolean done = true;
            for (Army a : armies) {
                if (!a.battalions.isEmpty()) {
                    done = false;
                }
            }
            if (done) {
                break;
            }

            idx++;
            if (idx >= armies.length) {
                idx = 0;
            }

            if (army.battalions.isEmpty()) {
                continue;
            }

            Battalion b = army.battalions.remove(0);
            List<Territory> terrs = TerritoryCard.getClaimedTerritories(army.armyType);
            Territory t = terrs.get(rand.nextInt(terrs.size()));
            t.battalions.add(b);
        }

        for (int i = 0; i < armies.length; i++) {
            Army army = armies[i];
            System.out.printf("Army [%s] [%s]\n", army.armyType, army.classType);
            List<Territory> terrs = TerritoryCard.getClaimedTerritories(army.armyType);
            for (Territory t : terrs) {
                System.out.printf("\t%s\t%d\n", t.card(), t.battalions.size());
            }
        }

    }

    Army[] armies = new Army[4];
    Army red, green, grey, yellow;
    List<TerritoryCard> deck;
    
    public void addArmy(Army army) {

        switch (army.armyType) {
            case RED:
                armies[ArmyType.RED.ordinal()] = army;
                red = army;
                break;
            case GREEN:
                armies[ArmyType.GREEN.ordinal()] = army;
                green = army;
                break;
            case GREY:
                armies[ArmyType.GREY.ordinal()] = army;
                grey = army;
                break;
            case YELLOW:
                armies[ArmyType.YELLOW.ordinal()] = army;
                yellow = army;
                break;
        }

    }

}