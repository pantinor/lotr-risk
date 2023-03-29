package lotr.util;

import java.util.Random;

public class Dice {

    private final Random r = new Random();
    private final int num;
    private final int sides;

    public Dice() {
        sides = 6;
        num = 1;
    }

    public int roll() {
        int sum = 0;
        for (int i = 0; i < num; i++) {
            sum += r.nextInt(sides) + 1;
        }
        return sum;
    }

}
