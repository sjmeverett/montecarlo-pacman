package pacman.entries.pacman.evaluators.ensemble;

import pacman.entries.pacman.GameNode;
import pacman.entries.pacman.MonteCarloPacManSimulator;
import pacman.entries.pacman.evaluators.ITreeEvaluator;


/**
 * This evaluator modifies the tree only when the best move will eat a power pill.  It looks
 * for 2 move delaying sequences (i.e. a move followed by its opposite) which offer a better
 * score than just eating the pill, and adds a bonus to the first move of the delaying
 * sequence such that its average score becomes that of the completed delay-then-eat action.
 * 
 * NOTE: this evaluator does not work with useGhostPositions = true!
 */
public class PowerPillModificationEvaluator implements ITreeEvaluator
{
	@Override
	public void evaluateTree(MonteCarloPacManSimulator simulator)
	{
		GameNode bestNode = simulator.bestNode();
		
		//this evaluator only runs if the best node eats a power pill
		if (bestNode.getMoveEatsPowerPill())
		{
			double bestAverage = bestNode.getAverageScore();
			
			for (GameNode child: simulator.getPacManChildren())
			{
				if (child == bestNode)
					continue;
				
				//we're checking for 2 step delaying moves, i.e. a move followed by its opposite
				GameNode oppositeNode = child.getChild(child.getMove().opposite());
				
				if (oppositeNode == null)
					continue;
				
				//get the move which actually eats the pill
				GameNode eatNode = oppositeNode.getChild(bestNode.getMove());
				
				if (eatNode == null)
					continue;
				
				//does the delaying move offer a better score than just eating the power pill
				if (eatNode.getAverageScore() > bestAverage)
				{
					//add a bonus such that the score of the first move of the delaying sequence is
					//the same as the score after completing the delaying sequence
					int scoreBonus = (int)Math.round(eatNode.getAverageScore() - child.getAverageScore());
					
					//...but only if its positive
					if (scoreBonus > 0)
						child.addScoreBonus(scoreBonus);
				}
			}
		}
	}
}
