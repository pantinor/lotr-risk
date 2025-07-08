package lotr.ai;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import lotr.AdventureCard;
import lotr.Army;
import lotr.Battalion;
import lotr.Constants;
import lotr.Game;
import lotr.Game.Step;
import lotr.GameScreen;
import lotr.Leader;
import lotr.Location;
import lotr.Region;
import static lotr.Risk.GAME;
import lotr.TerritoryCard;
import lotr.util.CardAction;
import lotr.util.Dice;
import lotr.util.Logger;
import lotr.util.RingPathAction;

public abstract class BaseBot {

    public static enum Type {
        HEURISTIC;
    }

    public static class SortWrapper implements Comparable {

        int factor;
        TerritoryCard territory;

        public SortWrapper(int factor, TerritoryCard c) {
            this.factor = factor;
            this.territory = c;
        }

        @Override
        public int compareTo(Object obj) {
            SortWrapper other = (SortWrapper) obj;
            if (this.factor > other.factor) {
                return 1;
            } else if (this.factor < other.factor) {
                return -1;
            } else {
                return 0;
            }
        }

    }

    private static class FortificationMove implements Comparable<FortificationMove> {

        TerritoryCard from;
        TerritoryCard to;
        double score;

        FortificationMove(TerritoryCard from, TerritoryCard to, double score) {
            this.from = from;
            this.to = to;
            this.score = score;
        }

        @Override
        public int compareTo(FortificationMove other) {
            // Sort in descending order to get the best score first
            return Double.compare(other.score, this.score);
        }
    }

    final Dice dice = new Dice();
    final Random rand = new Random();

    final Game game;
    final Army army;

    private GameScreen gameScreen;
    private Logger logger;
    private RingPathAction rpa;
    private CardAction cardAction;

    boolean conqueredTerritory, conqueredSOPWithLeader;

    public BaseBot(Game game, Army army) {
        this.game = game;
        this.army = army;
    }

    public void set(GameScreen gameScreen, RingPathAction rpa, CardAction cardAction) {
        this.gameScreen = gameScreen;
        this.logger = gameScreen.logs;
        this.rpa = rpa;
        this.cardAction = cardAction;
    }

    public void log(String text, Color color) {
        if (this.logger != null) {
            this.logger.log(text, color);
        } else {
            //System.out.println(text);
        }
    }

    public SequenceAction run() {

        SequenceAction s = Actions.sequence();

        if (army.claimedTerritories().isEmpty()) {
            //TODO game over remove me from game
            s.addAction(Actions.delay(1));
            return s;
        }

        conqueredTerritory = false;
        conqueredSOPWithLeader = false;

        RunnableAction r1 = new RunnableAction();
        r1.setRunnable(() -> {
            reinforce();
            game.nextStep();//attack
        });
        s.addAction(r1);

        s.addAction(Actions.delay(1));

        RunnableAction r2 = new RunnableAction();
        r2.setRunnable(() -> {
            AdventureCard[] advCards = game.current().adventureCards.toArray(new AdventureCard[0]);
            for (AdventureCard c : advCards) {
                if (c.type() == AdventureCard.Type.POWER) {
                    cardAction.process(c);
                }
            }
            attack();
            game.nextStep();//fortify
        });
        s.addAction(r2);

        s.addAction(Actions.delay(1));

        RunnableAction r3 = new RunnableAction();
        r3.setRunnable(() -> {
            fortify();
            game.nextStep();//tcard
        });
        s.addAction(r3);

        s.addAction(Actions.delay(1));

        RunnableAction r4 = new RunnableAction();
        r4.setRunnable(() -> {
            if (conqueredTerritory) {
                if (!game.territoryCards.isEmpty()) {
                    TerritoryCard newCard = game.territoryCards.remove(0);
                    game.current().territoryCards.add(newCard);
                    log(String.format("%s collected territory card [%s].", game.current().armyType, newCard.title()), game.current().armyType.color());
                }
            }
            game.nextStep();//acard
        });
        s.addAction(r4);

        s.addAction(Actions.delay(1));

        RunnableAction r5 = new RunnableAction();
        r5.setRunnable(() -> {

            if (conqueredSOPWithLeader) {
                cardAction.drawAdventureCard(false);
            }

            AdventureCard[] advCards = game.current().adventureCards.toArray(new AdventureCard[0]);
            for (AdventureCard c : advCards) {
                cardAction.process(c);
            }

            game.nextStep();//replace
        });
        s.addAction(r5);

        s.addAction(Actions.delay(1));

        RunnableAction r6 = new RunnableAction();
        r6.setRunnable(() -> {
            if (game.current().leader1.territory == null && game.current().leader2.territory == null) {
                List<TerritoryCard> claimedTerritories = game.current().claimedTerritories();
                List<Location> strongholds = game.current().ownedStrongholds(claimedTerritories);
                game.current().leader1.territory = !strongholds.isEmpty() ? strongholds.get(0).getTerritory() : claimedTerritories.get(0);
                log(String.format("%s replaced a leader to %s.", army.armyType, game.current().leader1.territory), army.armyType.color());
            } else if (game.current().leader1.territory != null && game.current().leader2.territory != null) {
                log(String.format("%s has both leaders on the map.", army.armyType), army.armyType.color());
            } else {
                log(String.format("%s is only missing one leader, and cannot replace a leader at this turn.", army.armyType), army.armyType.color());
            }
            game.nextStep();//ring
        });
        s.addAction(r6);

        s.addAction(Actions.delay(1));

        RunnableAction r7 = new RunnableAction();
        r7.setRunnable(() -> {
            rpa.advance();//advance the ring
            game.nextStep();//draft next player and start turn
            if (GAME.current().isBot()) {
                s.addAction(game.current().bot.run());
            }
        });
        s.addAction(r7);

        return s;

    }

    public void attack(TerritoryCard from, TerritoryCard to) {

        if (to == null) {
            return;
        }

        Army defender = game.getOccupyingArmy(to);

        while (rollAttack(from, to)) {

            int defenderCount = game.battalionCount(to);
            if (defenderCount == 0) {

                if (game.hasLeader(defender, to)) {
                    game.removeLeader(defender, to);
                    log(String.format("%s's leader on %s was defeated!", defender.armyType, to.title()), defender.armyType.color());
                }

                int tempCount;
                int reinforceCount = tempCount = game.battalionCount(from) - 1;
                for (Battalion b : army.getBattalions()) {
                    if (b.territory == from && reinforceCount > 0) {
                        b.territory = to;
                        reinforceCount--;
                    }
                }

                if (game.hasLeader(army, from)) {
                    game.moveLeader(army, from, to);
                    if (Location.getSiteOfPower(to) != null) {
                        conqueredSOPWithLeader = true;
                    }
                }

                conqueredTerritory = true;

                if (this.gameScreen != null) {
                    this.gameScreen.lookAt(to);
                }

                log(String.format("%s conquered %s and reinforced with %d battalions.", army.armyType, to.title(), tempCount), army.armyType.color());

                // Attack on this territory is over, so exit the loop.
                break;
            }

            // Exit loop if the attacker can no longer continue the assault from this territory.
            if (game.battalionCount(from) <= 1) {
                log(String.format("%s aborted their attack on %s.", army.armyType, to.title()), Color.SCARLET);
                break;
            }

        }
    }

    protected boolean rollAttack(TerritoryCard from, TerritoryCard to) {

        boolean strongholdBonusSuppressed = false;
        Army invader = game.getOccupyingArmy(from);
        Army defender = game.getOccupyingArmy(to);

        if (AdventureCard.THE_ENEMY_IS_AT_HAND.isUsed()) {
            AdventureCard.THE_ENEMY_IS_AT_HAND.setUsed(false);
            strongholdBonusSuppressed = true;
            log(String.format("%s used POWER card %s to suppress the stronghold bonus on the attack.",
                    invader.armyType, AdventureCard.THE_ENEMY_IS_AT_HAND.title()), invader.armyType.color());
        }

        if (AdventureCard.SIEGE_MACHINES.isUsed()) {
            AdventureCard.SIEGE_MACHINES.setUsed(false);
            strongholdBonusSuppressed = true;
            log(String.format("%s used POWER card %s to suppress the stronghold bonus on the attack.",
                    invader.armyType, AdventureCard.SIEGE_MACHINES.title()), invader.armyType.color());
        }

        int invaderCount = game.battalionCount(from);
        int defenderCount = game.battalionCount(to);

        if (AdventureCard.GRIMA_WORMTONGUE_1.isUsed()) {
            AdventureCard.GRIMA_WORMTONGUE_1.setUsed(false);
            if (defenderCount > 2) {
                invader.addBattalion(from);
                invader.addBattalion(from);
                defender.removeBattalion(to);
                defender.removeBattalion(to);
            }
            if (defenderCount == 2) {
                invader.addBattalion(from);
                defender.removeBattalion(to);
            }
            log(String.format("%s used POWER card %s to add battalions on the attack.",
                    invader.armyType, AdventureCard.GRIMA_WORMTONGUE_1.title()), invader.armyType.color());
        }

        if (AdventureCard.GRIMA_WORMTONGUE_2.isUsed()) {
            AdventureCard.GRIMA_WORMTONGUE_2.setUsed(false);
            if (defenderCount > 2) {
                invader.addBattalion(from);
                invader.addBattalion(from);
                defender.removeBattalion(to);
                defender.removeBattalion(to);
            }
            if (defenderCount == 2) {
                invader.addBattalion(from);
                defender.removeBattalion(to);
            }
            log(String.format("%s used POWER card %s to add battalions on the attack.",
                    invader.armyType, AdventureCard.GRIMA_WORMTONGUE_2.title()), invader.armyType.color());
        }

        if (AdventureCard.AMBUSH.isUsed()) {
            AdventureCard.AMBUSH.setUsed(false);
            invader.addBattalion(from);
            invader.addBattalion(from);
            invader.addBattalion(from);
            log(String.format("%s used POWER card %s to add battalions on the attack.",
                    invader.armyType, AdventureCard.AMBUSH.title()), invader.armyType.color());
        }

        if (invaderCount == 1) {
            return false;
        }

        if (defenderCount == 0) {
            return false;
        }

        int attackerLosses = 0;
        int defenderLosses = 0;
        int attackerDice = invaderCount == 2 ? 1 : invaderCount == 3 ? 2 : 3;
        int defenderDice = defenderCount == 1 ? 1 : 2;

        int[] attackerRolls = new int[attackerDice];
        int[] defenderRolls = new int[defenderDice];

        for (int i = 0; i < attackerDice; i++) {
            int r = dice.roll();
            if (game.hasLeader(army, from)) {
                r++;
            }
            attackerRolls[i] = r;
        }

        for (int i = 0; i < defenderDice; i++) {
            int r = dice.roll();
            if (game.hasLeader(defender, to)) {
                r++;
            }
            if (!strongholdBonusSuppressed && game.isDefendingStrongHold(to)) {
                r++;
            }
            defenderRolls[i] = r;
        }

        if (attackerRolls[0] > defenderRolls[0]) {
            defenderLosses++;
        } else {
            attackerLosses++;
        }

        if (attackerDice > 1 && defenderDice > 1) {
            if (attackerRolls[1] > defenderRolls[1]) {
                defenderLosses++;
            } else {
                attackerLosses++;
            }
        }

        for (int i = 0; i < attackerLosses; i++) {
            army.removeBattalion(from);
        }

        for (int i = 0; i < defenderLosses; i++) {
            defender.removeBattalion(to);
        }

        //log(String.format("%s lost %d battalion(s) from %s and %s lost %d battalion(s) from %s.",
        //        army.armyType, attackerLosses, from, defender.armyType, defenderLosses, to), Color.SCARLET);
        return true;
    }

    public abstract void attack();

    public void reinforce() {
        List<TerritoryCard> claimedTerritories = army.claimedTerritories();
        List<Location> strongholds = army.ownedStrongholds(claimedTerritories);

        int[] r = getReinforcementCounts();

        int strongholdReinforcements = r[0];
        int territoryReinforcements = r[1];
        int regionReinforcements = r[2];
        int cardReinforcements = 0;
        int sumArchers = r[4];
        int sumRiders = r[5];
        int sumEagles = r[6];
        int wildcards = r[7];

        int wildcardsUsed = 0;

        int neededMixed = Math.max(0, 1 - sumArchers) + Math.max(0, 1 - sumRiders) + Math.max(0, 1 - sumEagles);
        if (wildcards >= neededMixed) {
            cardReinforcements = 10;
            wildcardsUsed = neededMixed;
        } else if (sumEagles + wildcards >= 3) {
            int needed = Math.max(0, 3 - sumEagles);
            cardReinforcements = 8;
            wildcardsUsed = Math.min(needed, wildcards);
        } else if (sumRiders + wildcards >= 3) {
            int needed = Math.max(0, 3 - sumRiders);
            cardReinforcements = 6;
            wildcardsUsed = Math.min(needed, wildcards);
        } else if (sumArchers + wildcards >= 3) {
            int needed = Math.max(0, 3 - sumArchers);
            cardReinforcements = 4;
            wildcardsUsed = Math.min(needed, wildcards);
        }

        for (Location s : strongholds) {
            army.addBattalion(s.getTerritory());
        }

        List<SortWrapper> sorted = sortedClaimedTerritories(Step.COMBAT);

        if (!sorted.isEmpty()) {
            int[] reinforcementCounts = {
                territoryReinforcements,
                regionReinforcements,
                cardReinforcements
            };

            int idx = 0;
            for (int count : reinforcementCounts) {
                for (int i = 0; i < count; i++) {
                    army.addBattalion(sorted.get(idx).territory);
                    idx = (idx + 1) % sorted.size();
                }
            }
        }

        log(String.format("%s reinforcements: %d (stronghold) + %d (territory) + %d (region) + %d (cards)",
                army.armyType, strongholdReinforcements, territoryReinforcements, regionReinforcements, cardReinforcements), army.armyType.color());

        game.turnInTerritoryCards(army, sumArchers, sumRiders, sumEagles, wildcardsUsed);

    }

    /**
     * Evaluates all possible fortification moves and executes the one with the
     * highest strategic value. The logic prioritizes moving forces to set up
     * high-value attacks or to defend critical territories like strongholds. If
     * no such move is found, it performs a last-resort check to move an idle
     * leader to the front line.
     */
    public void fortify() {
        FortificationMove bestMove = findBestFortificationMove();

        if (bestMove != null) {
            fortify(bestMove.from, bestMove.to);
        } else {
            log(String.format("%s could not find a strategic territory to fortify.", army.armyType), army.armyType.color());
        }
    }

    int[] getReinforcementCounts() {
        List<TerritoryCard> claimedTerritories = army.claimedTerritories();
        List<Location> strongholds = army.ownedStrongholds(claimedTerritories);

        int strongholdReinforcements = strongholds.size();
        int territoryReinforcements = claimedTerritories.size() / 3 < 3 ? 3 : claimedTerritories.size() / 3;
        int regionReinforcements = 0;

        for (Region r : Region.values()) {
            if (claimedTerritories.containsAll(r.territories())) {
                regionReinforcements += r.reinforcements();
            }
        }

        int sumArchers = 0, sumRiders = 0, sumEagles = 0, wildcardcount = 0;

        for (TerritoryCard c : army.territoryCards) {
            Constants.BattalionType type = c.battalionType();
            if (type == Constants.BattalionType.ELVEN_ARCHER) {
                sumArchers++;
            } else if (type == Constants.BattalionType.DARK_RIDER) {
                sumRiders++;
            } else if (type == Constants.BattalionType.EAGLE) {
                sumEagles++;
            } else if (type == null) {
                wildcardcount++;
            }
        }

        return new int[]{
            strongholdReinforcements,
            territoryReinforcements,
            regionReinforcements,
            0, // placeholder for cardReinforcements
            sumArchers,
            sumRiders,
            sumEagles,
            wildcardcount
        };
    }

    /**
     * The purpose of fortification is to strengthen territories which are
     * potentially attacked by enemies, so it is natural to limit the action
     * space to only these.
     *
     * @param step the step
     * @return the territory
     */
    public abstract TerritoryCard pickClaimedTerritory(Step step);

    protected List<SortWrapper> sortedClaimedTerritories(Step step) {

        List<SortWrapper> sorted = new ArrayList<>();
        List<TerritoryCard> owned = army.claimedTerritories();

        for (TerritoryCard t : owned) {

            boolean hasLeader = game.hasLeader(army, t);
            int count = game.battalionCount(t);

            if (step == Step.COMBAT) {
                int strategicValue = 0;
                boolean enemyAdjacent = false;

                for (TerritoryCard adj : t.adjacents()) {
                    Army occupyingArmy = game.getOccupyingArmy(adj);
                    if (occupyingArmy != army) {
                        enemyAdjacent = true;
                        strategicValue++;
                        if (game.isStronghold(adj)) {
                            strategicValue += 2;
                        }
                        if (game.isSiteOfPower(adj)) {
                            strategicValue += 1;
                        }
                        if (game.hasLeader(occupyingArmy, adj)) {
                            strategicValue += 2;
                        }
                    }
                }

                if (enemyAdjacent) {
                    int factor = strategicValue;
                    // Multiply by existing battalions to favor reinforcing stronger positions
                    factor *= count;
                    // Leaders are force multipliers; give a bonus to reinforce them for an attack
                    if (hasLeader) {
                        factor *= 1.5;
                    }
                    sorted.add(new SortWrapper(factor, t));
                }

            } else if (step == Step.FORTIFY) {
                //check if territory has connected adjacents
                boolean friendlyadjacent = false;
                for (TerritoryCard adj : t.adjacents()) {
                    if (game.isClaimed(adj) == army) {
                        friendlyadjacent = true;
                        break;
                    }
                }
                //check if territory has enemy adjacents
                boolean enemyadjacent = false;
                for (TerritoryCard adj : t.adjacents()) {
                    if (game.isClaimed(adj) != army) {
                        enemyadjacent = true;
                        break;
                    }
                }
                if (count == 1 && hasLeader && !enemyadjacent && friendlyadjacent) {
                    sorted.clear();
                    sorted.add(new SortWrapper(count, t));
                    break;
                } else if (count > 1 && hasLeader && !enemyadjacent && friendlyadjacent) {
                    sorted.clear();
                    sorted.add(new SortWrapper(count, t));
                    break;
                } else if (hasLeader && enemyadjacent) {
                    //leave the leader there - no need to move him
                } else if (friendlyadjacent && count > 1) {
                    sorted.add(new SortWrapper(count, t));
                }
            }

        }
        Collections.sort(sorted, Collections.reverseOrder());
        return sorted;
    }

    /**
     * Finds the best possible fortification move by scoring all valid source
     * and destination pairs. This includes standard battalion moves and
     * last-resort leader repositioning.
     *
     * @return The best FortificationMove object, or null if no strategic move
     * is found.
     */
    private FortificationMove findBestFortificationMove() {
        List<FortificationMove> choices = new ArrayList<>();
        List<TerritoryCard> ownedTerritories = army.claimedTerritories();
        List<TerritoryCard> goodRegionCompletionCandidatesToAttack = findRegionCompletionTargetsInRegionWhereArmyControlsStrongHold(Step.FORTIFY);

        // --- Part 1A: Evaluate completing a region ---
        for (TerritoryCard attackable : goodRegionCompletionCandidatesToAttack) {
            Region region = Region.getRegion(attackable);
            List<TerritoryCard> ownedTerritoriesInRegionOfAttackable = ownedTerritories.stream()
                    .filter(c -> Region.getRegion(c) == region)
                    .collect(Collectors.toList());
            for (TerritoryCard from : ownedTerritoriesInRegionOfAttackable) {
                if (game.battalionCount(from) <= 1) {
                    continue;
                }
                for (TerritoryCard fortifiable : attackable.adjacents()) {
                    if (from == fortifiable) {
                        continue;
                    }
                    if (army.isConnected(from, fortifiable)) {
                        double score = calculateFortificationScore(from, fortifiable);
                        if (score > 0) {
                            choices.add(new FortificationMove(from, fortifiable, score));
                        }
                    }
                }
            }
        }

        if (choices.isEmpty()) {
            // --- Part 1B: Evaluate standard strategic moves ---
            for (TerritoryCard from : ownedTerritories) {
                // A move is only possible if there are battalions to move.
                if (game.battalionCount(from) <= 1) {
                    continue;
                }

                for (TerritoryCard to : ownedTerritories) {
                    if (from == to) {
                        continue;
                    }

                    if (army.isConnected(from, to)) {
                        double score = calculateFortificationScore(from, to);
                        if (score > 0) {
                            choices.add(new FortificationMove(from, to, score));
                        }
                    }
                }
            }
        }

        // --- Part 2: Last Resort - Check for idle leaders ---
        // This runs if no standard move with a positive score was found.
        if (choices.isEmpty()) {
            List<Leader> leaders = new ArrayList<>();
            if (army.leader1.territory != null) {
                leaders.add(army.leader1);
            }
            if (army.leader2.territory != null) {
                leaders.add(army.leader2);
            }

            for (Leader leader : leaders) {
                TerritoryCard from = leader.territory;
                // An idle leader is in a "safe" territory with no adjacent enemies.
                if (!hasEnemyNeighbors(from)) {
                    // Find the best frontline destination for this leader.
                    for (TerritoryCard to : ownedTerritories) {
                        if (from == to) {
                            continue;
                        }

                        // Destination must be on the frontline and connected.
                        if (hasEnemyNeighbors(to) && army.isConnected(from, to)) {
                            // Calculate score for moving the leader to this new position.
                            double score = calculateFortificationScore(from, to);
                            // Add a small bonus to ensure this move is chosen over doing nothing.
                            score += 1.0;
                            choices.add(new FortificationMove(from, to, score));
                        }
                    }
                }
            }
        }

        if (choices.isEmpty()) {
            return null;
        }

        Collections.sort(choices);
        return choices.get(0);
    }

    /**
     * Calculates a strategic score for a potential fortification move from
     * 'from' to 'to'.
     *
     * @param from The source territory.
     * @param to The destination territory.
     * @return A score representing the strategic value of the move.
     */
    private double calculateFortificationScore(TerritoryCard from, TerritoryCard to) {
        double score = 0;

        // 1. Offensive Score: How good is 'to' as a staging ground for an attack?
        double highestEnemyValue = 0;
        for (TerritoryCard adj : to.adjacents()) {
            Army owner = game.getOccupyingArmy(adj);
            if (owner != null && owner != army) {
                double enemyValue = 1; // Base value for bordering an enemy
                if (game.isStronghold(adj)) {
                    enemyValue += 3;
                }
                if (Location.getSiteOfPower(adj) != null) {
                    enemyValue += 3;
                }
                // A higher battalion count makes it a harder, but more valuable target to be near
                enemyValue += game.battalionCount(adj) / 2.0;

                if (enemyValue > highestEnemyValue) {
                    highestEnemyValue = enemyValue;
                }
            }
        }
        score += highestEnemyValue * 1.5; // Weight offensive moves higher

        // 2. Defensive Score: How important is it to defend the 'to' territory?
        boolean toHasEnemyNeighbors = hasEnemyNeighbors(to);
        if (game.isStronghold(to) && toHasEnemyNeighbors) {
            score += 5;
        }

        // 3. Positional Score: Is this a good repositioning of forces?
        boolean fromHasEnemyNeighbors = hasEnemyNeighbors(from);
        if (!fromHasEnemyNeighbors && toHasEnemyNeighbors) {
            score += 3; // Bonus for moving from a safe rear territory to the front line
        }

        return score;
    }

    /**
     * Executes the fortification move, transferring battalions and leaders.
     *
     * @param from The source territory.
     * @param to The destination territory.
     */
    private void fortify(TerritoryCard from, TerritoryCard to) {

        int bcount = game.battalionCount(from) - 1;

        if (to == null) {
            log(String.format("%s did NOT find territory to fortify from %s with %d battalion(s).", army.armyType, from.title(), bcount), army.armyType.color());
            return;
        }

        log(String.format("%s fortified from %s to %s with %d battalion(s).", army.armyType, from.title(), to.title(), bcount), army.armyType.color());

        for (Battalion b : army.getBattalions()) {
            if (b.territory == from && bcount > 0) {
                b.territory = to;
                bcount--;
            }
        }

        if (game.hasLeader(army, from)) {
            game.moveLeader(army, from, to);
            //TODO check mission card
        }

        if (this.gameScreen != null) {
            this.gameScreen.lookAt(to);
        }
    }

    /**
     * Checks if a given territory is adjacent to any enemy-controlled
     * territories.
     *
     * @param tc The territory to check.
     * @return True if there is at least one enemy neighbor, false otherwise.
     */
    private boolean hasEnemyNeighbors(TerritoryCard tc) {
        for (TerritoryCard adj : tc.adjacents()) {
            Army owner = game.getOccupyingArmy(adj);
            if (owner != null && owner != this.army) {
                return true;
            }
        }
        return false;
    }

    protected List<TerritoryCard> findRegionCompletionTargetsInRegionWhereArmyControlsStrongHold(Game.Step step) {
        List<TerritoryCard> targets = new ArrayList<>();

        for (Region region : Region.values()) {
            List<TerritoryCard> regionTerritories = region.territories();
            int owned = 0;
            int ownedSH = 0;
            int enemy = 0;
            List<TerritoryCard> enemyTerritories = new ArrayList<>();

            for (TerritoryCard card : regionTerritories) {
                Army owner = this.game.isClaimed(card);
                if (owner.equals(this.army)) {
                    owned++;
                    if (this.game.isDefendingStrongHold(card)) {
                        ownedSH++;
                    }
                } else {
                    enemy++;
                    enemyTerritories.add(card);
                }
            }

            if (owned > 0 && ownedSH > 0 && enemy > 0) {
                for (TerritoryCard t : enemyTerritories) {
                    for (TerritoryCard adj : t.adjacents()) {
                        if (this.game.isClaimed(adj) == this.army) {
                            int count = this.game.battalionCount(adj);
                            if ((step == Game.Step.COMBAT && count > 1) || (step == Game.Step.FORTIFY && count == 1)) {
                                targets.add(t);
                                break;
                            }
                        }
                    }
                }
            }

        }

        return targets;
    }

}
