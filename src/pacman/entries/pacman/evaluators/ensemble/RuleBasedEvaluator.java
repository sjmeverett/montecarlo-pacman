package pacman.entries.pacman.evaluators.ensemble;

import pacman.entries.pacman.GameNode;
import pacman.entries.pacman.MonteCarloPacManSimulator;
import pacman.entries.pacman.evaluators.ITreeEvaluator;


/**
 * This evaluator applies two simple rules:
 * 
 * Rule 1: "For each action that will make the player eat pills, a score of 
 * 400 points is awarded to that action"
 * 
 * Rule 2: "For each action that moves the agent into a state where it could
 * eat pills with its next move (i.e. put the agent next to pills) a score
 * of 300 points is awarded"
 * 
 * NOTE: rule 2 does not work if useGhostPositions = true!
 */
public class RuleBasedEvaluator implements ITreeEvaluator
{
	private static final int RULE1_DEFAULT_BONUS = 400;
	private static final int RULE2_DEFAULT_BONUS = 300;
	private int rule1Bonus, rule2Bonus;
	
	
	public RuleBasedEvaluator(int rule1Bonus, int rule2Bonus)
	{
		this.rule1Bonus = rule1Bonus;
		this.rule2Bonus = rule2Bonus;
	}
	
	public RuleBasedEvaluator()
	{
		this(RULE1_DEFAULT_BONUS, RULE2_DEFAULT_BONUS);
	}
	
	@Override
	public void evaluateTree(MonteCarloPacManSimulator simulator)
	{
		LongRangePlanningEvaluator longRangePlanner = new LongRangePlanningEvaluator();
		
		for (GameNode child: simulator.getPacManChildren())
		{			
			if (child.getMoveEatsPills())
			{
				//rule 1: "For each action that will make the player eat pills, a score of
				//400 points is awarded to that action"
				child.addScoreBonus(rule1Bonus);
			}
			else if (child.getCanEatPillsOnSubsequentMove())
			{
				//rule2: "For each action that moves the agent into a state where it could
				//eat pills with its next move (i.e. put the agent next to pills) a score
				//of 300 points is awarded"
				child.addScoreBonus(rule2Bonus);
			}
			else
			{
				//run the long range planner if neither rule applies
				longRangePlanner.evaluateTree(simulator);
			}
		}
	}
}
