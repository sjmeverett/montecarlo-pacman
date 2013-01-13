package pacman.entries.pacman.selectionpolicies;

import pacman.entries.pacman.GameNode;

/**
 * Provides UCB calculation based on the UCB1 formula found in Levine's paper.
 */
public class LevineUcbSelectionPolicy extends UcbSelectionPolicyBase
{
	private static final double DEFAULT_BALANCE = 10000;
	private double balanceParameter;
	
	/**
	 * Constructor.  Uses the default balance parameter.
	 */
	public LevineUcbSelectionPolicy()
	{
		this.balanceParameter = DEFAULT_BALANCE;
	}
	
	
	/**
	 * Constructor.
	 * @param balanceParameter The balance parameter (balance between exploration and exploitation) to use.
	 */
	public LevineUcbSelectionPolicy(double balanceParameter)
	{
		this.balanceParameter = balanceParameter;
	}


	@Override
	public double getUcbValue(GameNode node)
	{
		return node.getAverageScore() + balanceParameter * 
			Math.sqrt(Math.log(node.getParent().getNumberOfVisits()) / node.getNumberOfVisits());
	}
}
