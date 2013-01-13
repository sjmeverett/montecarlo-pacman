package pacman.entries.pacman.evaluators.ensemble;

import pacman.entries.pacman.GameNode;
import pacman.entries.pacman.MonteCarloPacManSimulator;
import pacman.entries.pacman.evaluators.ITreeEvaluator;
import pacman.game.Game;
import pacman.game.Constants.DM;

/**
 * This evaluator awards a score to any move which decreases the distance to the nearest pill.
 */
public class LongRangePlanningEvaluator implements ITreeEvaluator
{
	private static final int DEFAULT_BONUS = 200;
	private int bonus;
	
	public LongRangePlanningEvaluator(int bonus)
	{
		this.bonus = bonus;
	}
	
	public LongRangePlanningEvaluator()
	{
		this(DEFAULT_BONUS);
	}
	
	
	@Override
	public void evaluateTree(MonteCarloPacManSimulator simulator)
	{
		//get the current distance to the nearest pill
		Game game = simulator.getGameState();
		double distance = getDistanceToNeartestPill(game);
		
		//see if it improves for each of the children
		for (GameNode child: simulator.getPacManChildren())
		{
			//save the game state and play the move
			game = simulator.pushGameState();
			simulator.playMove(child.getMove());
			simulator.advanceGameToNextNode();
			
			//award a bonus if the distance is less now
			double d = getDistanceToNeartestPill(game);
			
			if (d < distance)
				child.addScoreBonus(bonus);
			
			//restore the game state
			simulator.popGameState();
		}
	}
	
	
	private double getDistanceToNeartestPill(Game game)
	{
		//get the closest pill to the current position
		int[] pills = game.getActivePillsIndices();
		
		if (pills.length == 0)
			return Double.MAX_VALUE;
		
		int currentIndex = game.getPacmanCurrentNodeIndex();
		int closestPill = game.getClosestNodeIndexFromNodeIndex(currentIndex, pills, DM.MANHATTAN);
		
		//return the distance to it
		return game.getDistance(currentIndex, closestPill, DM.MANHATTAN);
	}
}
