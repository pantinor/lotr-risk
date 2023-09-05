package lotr.ai;

import java.util.ArrayList;
import java.util.List;
import static lotr.ai.HeuristicAI.TREE_MAX_LEVEL;

/**
 * 3.1 Algorithms Implementation is based on the minimax alpha-beta pruning
 * algorithm.
 *
 * Implementation is based on the minimax alpha-beta pruning algorithm. Player
 * constructs a prediction tree based on the DFS algorithm.
 *
 * Player constructs a prediction tree based on the DFS algorithm.
 *
 * 3.1.1 Prediction Tree
 *
 * Whenever it is the agent’s turn to play, a tree is made. The tree nodes are
 * the states of the game (map of the game in that state) and each edge is a
 * possible attack made from current state to attack’s result state of the map.
 * An attack action consists of start and target territories’ coordinates and
 * the number of units chosen to be in the attacking army.
 *
 * The root node is the current state of the map a player has to choose it’s
 * moves from. Although, the tree cannot predict until the ending state of the
 * game due to large number of possible moves a player can have in each state
 * which the memory and time limits do not allow. Therefore, player can only
 * construct the tree to a certain depth that we have defined in the code.
 *
 * There are also some simplifications applied to particular parts of the
 * agent’s gaming strategy that will be all explained in the following sections.
 *
 * In order to make the best possible decision heuristics are defined for the
 * deepest possible nodes (tree leaves) that evaluate how much reaching that
 * state can be beneficial for the player. Said heuristics will be further
 * explained in the report.
 *
 * When the values of the leaves are determined the tree will be passed on to
 * the minimax alpha-beta pruning and this well-known algorithm will find the
 * best series of actions the player can have.
 *
 * https://github.com/arman-aminian/risk-game-ai-agent
 */
public class Minimax {

    private static final int INFP = Integer.MAX_VALUE;
    private static final int INFN = Integer.MIN_VALUE;

    private static final List<TreeNode> FINAL_MOVES = new ArrayList<>();

    public static Attack getNextAttack(TreeNode start) {

        FINAL_MOVES.clear();

        TreeNode result = minimaxFunc(start, INFN, INFP);

        List<TreeNode> minimaxOutput = new ArrayList<>();
        int result_node_index = result.getNodeIndex();

        for (int i = FINAL_MOVES.size() - 1; i > 0; i--) {
            if (FINAL_MOVES.get(i - 1).getNodeIndex() == result_node_index) {
                minimaxOutput.add(FINAL_MOVES.get(i));
            }

        }

        for (TreeNode tn : minimaxOutput) {
            if (tn.getDepth_node() == TREE_MAX_LEVEL - 1 && tn.getLastAttack() != null) {
                return tn.getLastAttack();
            }
        }

        return null;

    }

    private static TreeNode minimaxFunc(TreeNode position, double alpha, double beta) {

        if (position.getDepth_node() == 0) {
            return position;
        }

        TreeNode maxEvalNode;
        TreeNode minEvalNode;
        TreeNode evalNode;

        if (position.isType_node()) {

            maxEvalNode = new TreeNode(true, INFN, position.getDepth_node() - 1);
            for (TreeNode child : position.getChildren_node()) {
                evalNode = minimaxFunc(child, alpha, beta);
                evalNode.setChildren_node(null);
                if (evalNode.getValue_node() > maxEvalNode.getValue_node()) {
                    maxEvalNode = evalNode;
                }
                alpha = Double.max(alpha, evalNode.getValue_node());
                if (beta <= alpha) {
                    break;
                }
            }

            FINAL_MOVES.add(maxEvalNode);
            FINAL_MOVES.add(position);

            return maxEvalNode;

        } else {

            minEvalNode = new TreeNode(false, INFP, position.getDepth_node() - 1);
            for (TreeNode child : position.getChildren_node()) {
                evalNode = minimaxFunc(child, alpha, beta);
                evalNode.setChildren_node(null);
                if (evalNode.getValue_node() < minEvalNode.getValue_node()) {
                    minEvalNode = evalNode;
                }

                beta = Double.min(beta, evalNode.getValue_node());
                if (beta <= alpha) {
                    break;
                }
            }

            FINAL_MOVES.add(minEvalNode);
            FINAL_MOVES.add(position);

            return minEvalNode;
        }

    }

}
