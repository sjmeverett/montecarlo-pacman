package pacman.entries.pacman.evaluators;

import java.util.Collection;

import pacman.entries.pacman.GameNode;
import pacman.entries.pacman.MonteCarloPacManSimulator;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Game;

/**
 * Implements an evaluator which applies a penalty to moves which eat power pills if there
 * are no ghosts nearby.
 */
public class PowerPillDistanceEvaluator implements ITreeEvaluator
{
	private static final double DEFAULT_MINIMUM_DISTANCE = 10;
	private static final int DEFAULT_PENALTY = 300;
	
	private double minimumDistance;
	private int penalty;
	
	/**
	 * Constructor.  Allows specification of parameters.
	 * @param minimumDistance The minimum distance within which a ghost must be to avoid a move eating a power pill to be penalised.
	 * @param penalty The amount to subtract from the node score if there are no ghosts nearby.
	 */
	public PowerPillDistanceEvaluator(double minimumDistance, int penalty)
	{
		this.minimumDistance = minimumDistance;
		this.penalty = penalty;
	}
	
	/**
	 * Default constructor.
	 */
	public PowerPillDistanceEvaluator()
	{
		this(DEFAULT_MINIMUM_DISTANCE, DEFAULT_PENALTY);
	}


	@Override
	public void evaluateTree(MonteCarloPacManSimulator simulator)
	{
		//get the children of the root node, if there aren't any we can't make any decisions
		Collection<GameNode> children = simulator.getPacManChildren();
		
		if (children == null)
			return;
		
		Game game = simulator.getGameState();
		
		//we're actually using the closest ghost distance from the beginning of the move, not when the
		//pill is actually eaten, but it saves simulating when it could make the wrong guess
		//about ghost behaviour anyway, and it's "close enough"
		if (getNearestGhostDistance(game) < minimumDistance)
		{
			//no ghosts nearby, penalise nodes which eat power pills
			for (GameNode child: children)
			{
				if (child.getMoveEatsPowerPill())
				{
					child.addScoreBonus(-penalty);
				}
			}
		}
	}
	
	/**
	 * Gets the distance of the closest ghost.
	 * @param game
	 * @return
	 */
	private double getNearestGhostDistance(Game game)
	{
		int[] ghostIndices = new int[GHOST.values().length];
		int i = 0;
		
		for (GHOST ghost: GHOST.values())
		{
			ghostIndices[i++] = game.getGhostCurrentNodeIndex(ghost);
		}
		
		int currentIndex = game.getPacmanCurrentNodeIndex();
		int closestIndex = game.getClosestNodeIndexFromNodeIndex(currentIndex, ghostIndices, DM.PATH);
		return game.getDistance(currentIndex, closestIndex, DM.PATH);
	}
}
