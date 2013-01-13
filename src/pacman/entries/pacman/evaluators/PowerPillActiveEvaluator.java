package pacman.entries.pacman.evaluators;

import java.util.Collection;

import pacman.entries.pacman.GameNode;
import pacman.entries.pacman.MonteCarloPacManSimulator;
import pacman.game.Constants.GHOST;
import pacman.game.Game;


/**
 * Adds a penalty to a move which eats a power pill if there is still a power pill active.
 */
public class PowerPillActiveEvaluator implements ITreeEvaluator
{
	private static final int DEFAULT_PENALTY = 300;
	private int penalty;
	
	
	public PowerPillActiveEvaluator(int penalty)
	{
		this.penalty = penalty;
	}
	
	
	public PowerPillActiveEvaluator()
	{
		this(DEFAULT_PENALTY);
	}
	
	
	@Override
	public void evaluateTree(MonteCarloPacManSimulator simulator)
	{
		//get the children of the root node, if there aren't any we can't make any decisions
		Collection<GameNode> children = simulator.getPacManChildren();
		
		if (children == null)
			return;
		
		Game game = simulator.getGameState();
		
		if (isPowerPillActive(game))
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
	 * Returns true if any ghosts are edible; otherwise, returns false.
	 * @param game
	 * @return
	 */
	private boolean isPowerPillActive(Game game)
	{
		int edibleTime = game.getGhostEdibleTime(GHOST.BLINKY)
			+ game.getGhostEdibleTime(GHOST.INKY)
			+ game.getGhostEdibleTime(GHOST.PINKY)
			+ game.getGhostEdibleTime(GHOST.SUE);
		
		return edibleTime > 0;
	}
}
