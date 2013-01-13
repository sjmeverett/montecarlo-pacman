package pacman.entries.pacman.selectionpolicies;

import java.util.Collection;

import pacman.entries.pacman.GameNode;

public abstract class UcbSelectionPolicyBase implements ISelectionPolicy
{

	@Override
	public GameNode selectChild(GameNode node)
	{
		Collection<GameNode> children = node.getChildren();
		GameNode selectedChild = null;
		double max = Double.NEGATIVE_INFINITY;
		double currentUcb;
		
		if (children == null)
			throw new IllegalStateException("Cannot call selectChild on a leaf node.");
		
		for (GameNode child: children)
		{
			currentUcb = getUcbValue(child);
			
			if (Double.isNaN(currentUcb))
			{
				throw new IllegalStateException(String.format("UCB is not a number. Parent number of visits: %d, Number of visits: %d, Score: %.2f, Variance: %.2f, Variance UCB: %.2f", 
						node.getNumberOfVisits(), child.getNumberOfVisits(), child.getAverageScore(), child.getVariance(), ((Ucb1TunedSelectionPolicy)this).getVarianceUcb(child)));
			}
			else if (currentUcb > max)
			{
				max = currentUcb;
				selectedChild = child;
			}
		}
		
		return selectedChild;
	}

	@Override
	public boolean getEvaluateAllChildrenOnExpansion()
	{
		return true;
	}
	
	public abstract double getUcbValue(GameNode node);
}
