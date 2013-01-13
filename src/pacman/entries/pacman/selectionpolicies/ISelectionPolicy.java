package pacman.entries.pacman.selectionpolicies;

import pacman.entries.pacman.GameNode;

/**
 * An object to make a decision on which node to visit next in the tree.
 */
public interface ISelectionPolicy
{
	/**
	 * Select a child node of the specified node, using some applicable algorithm.
	 * @param node
	 * @return
	 */
	GameNode selectChild(GameNode node);
	
	/**
	 * Gets whether or not this policy requires child nodes to be evaluated on expansion.
	 * @return True if evaluation is required on expansion; otherwise, false.
	 */
	boolean getEvaluateAllChildrenOnExpansion();
}
