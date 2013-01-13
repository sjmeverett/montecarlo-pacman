package pacman.entries.pacman.selectionpolicies;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pacman.entries.pacman.GameNode;

/**
 * Selects nodes according to the ϵ-greedy algorithm in "Algorithms for the multi-armed bandit problem" (Kuleshov & Precup, 2000).
 */
public class EpsilonGreedySelectionPolicy implements ISelectionPolicy
{
	private static final double DEFAULT_EPSILON = 0.05;
	private static final Random random = new Random();
	private double epsilon;
	
	/**
	 * Constructor. Uses a default value for epsilon.
	 */
	public EpsilonGreedySelectionPolicy()
	{
		this.epsilon = DEFAULT_EPSILON;
	}
	
	/**
	 * Constructor.
	 * @param The epsilon value to use (the probability that the node is selected at random).
	 */
	public EpsilonGreedySelectionPolicy(double epsilon)
	{
		this.epsilon = epsilon;
	}

	
	@Override
	public GameNode selectChild(GameNode node)
	{
		List<GameNode> children = new ArrayList<GameNode>(node.getChildren());
		
		if (random.nextDouble() > epsilon)
		{
			double max = Double.NEGATIVE_INFINITY;
			GameNode selectedNode = null;
			double currentAverage;
			
			for (GameNode child: children)
			{
				currentAverage = child.getAverageScore();
				
				if (currentAverage > max)
				{
					max = currentAverage;
					selectedNode = child;
				}
			}
			
			return selectedNode;
		}
		else
		{
			return children.get(random.nextInt(children.size()));
		}
	}

	
	@Override
	public boolean getEvaluateAllChildrenOnExpansion()
	{
		return true;
	}
}
