package pacman.entries.pacman.selectionpolicies;

import pacman.entries.pacman.GameNode;

/**
 * Provides UCB calculation based on the UCB1 formula.
 */
public class Ucb1SelectionPolicy extends UcbSelectionPolicyBase
{
	@Override
	public double getUcbValue(GameNode node)
	{
		return node.getAverageScore() + Math.sqrt(2 * Math.log(node.getParent().getNumberOfVisits()) / node.getNumberOfVisits());
	}
}
