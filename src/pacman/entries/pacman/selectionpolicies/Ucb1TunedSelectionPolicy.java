package pacman.entries.pacman.selectionpolicies;

import pacman.entries.pacman.GameNode;

/**
 * Provides UCB calculation based on the UCB1-tuned algorithm presented in
 * "Finite-time Analysis of the Multiarmed Bandit Problem" (Auer et al, 2002).
 */
public class Ucb1TunedSelectionPolicy extends UcbSelectionPolicyBase
{
	@Override
	public double getUcbValue(GameNode node)
	{
		return node.getAverageScore() * 
			Math.sqrt(Math.log(node.getParent().getNumberOfVisits()) / node.getNumberOfVisits() 
				* Math.min(0.25, getVarianceUcb(node)));
	}
	
	public double getVarianceUcb(GameNode node)
	{
		return node.getVariance() + Math.sqrt(2 * Math.log(node.getParent().getNumberOfVisits()) / node.getNumberOfVisits());
	}
}
