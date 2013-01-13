package pacman.entries.pacman.selectionpolicies;

import java.util.Random;

import pacman.entries.pacman.GameNode;

/**
 * Provides UCB calculation based on the example at http://mcts.ai/?q=code/simple_java
 */
public class MctsAiUcbSelectionPolicy extends UcbSelectionPolicyBase
{
	private static final double EPSILON = 1e-6;
	private static final Random random = new Random();
	
	@Override
	public double getUcbValue(GameNode node)
	{
		//includes a small random number so that tie-breaking is evenly distributed on unexplored nodes
		return (double)node.getTotalScore() / (node.getNumberOfVisits() + EPSILON) 
			+ Math.sqrt(Math.log(node.getParent().getNumberOfVisits() + 1) / (node.getNumberOfVisits() + EPSILON))
			+ random.nextDouble() * EPSILON;
	}

	@Override
	public boolean getEvaluateAllChildrenOnExpansion()
	{
		return true;
	}
}
