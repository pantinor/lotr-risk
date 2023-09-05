package lotr.ai;

import java.util.List;
import lotr.Constants.ArmyType;
import lotr.Game;

/**
 * The algorithms are adapted from open source work documented here:
 *
 * https://github.com/arman-aminian/risk-game-ai-agent/tree/master
 */
public class HeuristicAI {

    private static final int ATTACK_ATTEMPTS = 2;
    static final int TREE_MAX_LEVEL = 5;
    static final int MINIMUM_DIFFERENCE_TO_ATTACK = 2;

    private int nodeIndex = 0;

    public Attack attack(ArmyType attacker, Game game) {

        HeuristicMap map = new HeuristicMap(game);

        TreeNode root = new TreeNode(true, 0, TREE_MAX_LEVEL, nodeIndex);

        this.nodeIndex++;

        int playerIndex = 0;
        for (int i = 0; i < ArmyType.values().length; i++) {
            if (attacker == ArmyType.values()[i]) {
                playerIndex = i;
                break;
            }
        }

        root = makeTree(root, attacker, map, ATTACK_ATTEMPTS, playerIndex);

        return Minimax.getNextAttack(root);
    }

    private TreeNode makeTree(TreeNode root, ArmyType attacker, HeuristicMap map, int numberOfAttacksLeft, int playerIndex) {

        HeuristicMap childMap = new HeuristicMap(map);

        boolean isChangingType = false;

        if (root.getDepth_node() == 0) {
            double[] features = HeuristicMap.heuristicFeatures(attacker, map);
            double val = ((features[0]) + (features[1]) + (features[2]) + (features[3]));
            root.setValue_node(val);
            return root;
        }

        List<Attack> chances = childMap.attackChances(attacker, childMap);

        ArmyType nextAttacker = attacker;

        for (int i = 0; i < chances.size(); i++) {

            childMap = new HeuristicMap(map);

            int attacksLeft;
            boolean nodeType;

            if (numberOfAttacksLeft == 0) {
                attacksLeft = ATTACK_ATTEMPTS;
                nodeType = !root.isType_node();

                if (playerIndex == ArmyType.values().length - 1) {
                    nextAttacker = ArmyType.values()[0];
                    playerIndex = 0;
                } else {
                    nextAttacker = ArmyType.values()[playerIndex + 1];
                    playerIndex++;
                }

                isChangingType = true;
            } else {
                attacksLeft = numberOfAttacksLeft - 1;
                nodeType = root.isType_node();
            }

            numberOfAttacksLeft--;

            TreeNode child = new TreeNode(nodeType, 0, root.getDepth_node() - 1, chances.get(i), this.nodeIndex);

            this.nodeIndex++;

            childMap.updateMap(chances.get(i));

            if (isChangingType) {
                HeuristicMap.heuristicReinforce(attacker, childMap);
            }

            child = makeTree(child, nextAttacker, childMap, attacksLeft, playerIndex);

            root.addChild(child);
        }

        childMap = new HeuristicMap(map);

        if (playerIndex == ArmyType.values().length - 1) {
            nextAttacker = ArmyType.values()[0];
            playerIndex = 0;
        } else {
            nextAttacker = ArmyType.values()[playerIndex + 1];
            playerIndex++;
        }

        TreeNode noOpChild = new TreeNode(!root.isType_node(), 0, root.getDepth_node() - 1, this.nodeIndex);

        this.nodeIndex++;

        HeuristicMap.heuristicReinforce(attacker, childMap);

        noOpChild = makeTree(noOpChild, nextAttacker, childMap, ATTACK_ATTEMPTS, playerIndex);

        root.addChild(noOpChild);

        return root;
    }

}
