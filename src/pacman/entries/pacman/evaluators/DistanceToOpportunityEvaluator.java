package pacman.entries.pacman.evaluators;

import java.util.Collection;

import pacman.entries.pacman.GameNode;
import pacman.entries.pacman.MonteCarloPacManSimulator;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * A tree evaluator which increases the score of the node which brings the
 * Ms. Pac-Man closer to eating a pill. 
 */
public class DistanceToOpportunityEvaluator implements ITreeEvaluator
{
	private static final int DEFAULT_GHOST_SCORE = 400;
	private static final int DEFAULT_PILL_SCORE = 200;
	private int ghostScore, pillScore;
	
	public DistanceToOpportunityEvaluator(int ghostScore, int pillScore)
	{
		this.ghostScore = ghostScore;
		this.pillScore = pillScore;
	}
	
	public DistanceToOpportunityEvaluator()
	{
		this(DEFAULT_GHOST_SCORE, DEFAULT_PILL_SCORE);
	}
	
	@Override
	public void evaluateTree(MonteCarloPacManSimulator simulator)
	{
		//get the children of the root node, if there isn't any we can't make any decisions
		Collection<GameNode> children = simulator.getPacManChildren();
		
		if (children.size() == 0)
			return;
		
		//get the current game state
		Game game = simulator.getGameState();
		
		//get the move towards the nearest edible ghost if there is one, and give it a bonus of [ghostScore]
		MOVE ghostMove = getMoveTowardsEdibleGhost(game);
		
		if (ghostMove != MOVE.NEUTRAL)
		{
			addBonus(children, ghostMove, ghostScore);
		}
		
		//get the move towards the nearest pill and give it a bonus of [pillScore]
		MOVE pillMove = getMoveTowardsPill(game);
		addBonus(children, pillMove, pillScore);
	}
	
	
	/**
	 * Finds the closest edible ghost and returns the move which moves PacMan towards it; if no ghosts are edible,
	 * MOVE.NEUTRAL is returned instead.
	 * @param game
	 * @return
	 */
	private MOVE getMoveTowardsEdibleGhost(Game game)
	{
		//inspired by StarterPacMan
		int currentIndex = game.getPacmanCurrentNodeIndex();
		int min = Integer.MAX_VALUE;
		int closestGhostIndex = -1;
		int distance;
		int ghostIndex; 
		
		for (GHOST ghost: GHOST.values())
		{
			if (game.getGhostEdibleTime(ghost) > 0)
			{
				ghostIndex = game.getGhostCurrentNodeIndex(ghost);
				distance = game.getShortestPathDistance(currentIndex, ghostIndex);
				
				if (distance < min)
				{
					min = distance;
					closestGhostIndex = ghostIndex;
				}
			}
		}
		
		if (closestGhostIndex > -1)
		{
			return game.getNextMoveTowardsTarget(currentIndex, closestGhostIndex, DM.PATH);
		}
		else
		{
			return MOVE.NEUTRAL;
		}
	}
	
	
	/**
	 * Gets the move which moves PacMan closer to the nearest pill.
	 * @param game
	 * @return
	 */
	private MOVE getMoveTowardsPill(Game game)
	{
		//inspired by StarterPacMan
		int currentIndex = game.getPacmanCurrentNodeIndex();
		int[] pills = game.getActivePillsIndices();
		int closestIndex = game.getClosestNodeIndexFromNodeIndex(currentIndex, pills, DM.PATH);
		
		return game.getNextMoveTowardsTarget(currentIndex, closestIndex, DM.PATH);
	}
	
	
	/**
	 * Adds the specified amount onto the score of the child node which represents the given move.
	 * @param children
	 * @param move
	 * @param score
	 */
	private void addBonus(Collection<GameNode> children, MOVE move, int bonus)
	{
		for (GameNode node: children)
		{
			if (node.getMove() == move)
			{
				node.addScoreBonus(bonus);
				return;
			}
		}
	}
}
