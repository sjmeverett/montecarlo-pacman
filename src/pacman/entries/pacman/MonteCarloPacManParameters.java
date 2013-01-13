package pacman.entries.pacman;

import java.util.EnumMap;

import pacman.controllers.Controller;
import pacman.controllers.examples.Legacy;
import pacman.controllers.examples.StarterPacMan;
import pacman.entries.pacman.evaluators.DistanceToOpportunityEvaluator;
import pacman.entries.pacman.evaluators.ITreeEvaluator;
import pacman.entries.pacman.evaluators.PowerPillActiveEvaluator;
import pacman.entries.pacman.evaluators.PowerPillDistanceEvaluator;
import pacman.entries.pacman.selectionpolicies.ISelectionPolicy;
import pacman.entries.pacman.selectionpolicies.LevineUcbSelectionPolicy;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

/**
 * Represents a set of parameters for Monte Carlo simulations of the Ms Pac-Man game.
 */
public class MonteCarloPacManParameters
{
	/**
	 * The number of times a node has to be sampled before being expanded.
	 */
	public int nodeExpansionThreshold;
	
	/**
	 * The maximum number of cycles a simulation will be run for.
	 */
	public int maximumSimulationLength;
	
	/**
	 * The amount that will be subtracted from the score if Ms Pac-Man dies during a simulation.
	 */
	public int deathPenalty;
	
	/**
	 * True if the death penalty is to be scaled by the sample count of a node.
	 */
	public boolean scaleDeathPenalty;
	
	/**
	 * The amount that will be added to the score if Ms Pac-Man completes a level during a simulation.
	 */
	public int completionReward;
	
	/**
	 * The model to use when simulating Ms Pac-Man behaviour.
	 */
	public Controller<MOVE> pacManModel;
	
	/**
	 * The model to use when simulating ghost behaviour.
	 */
	public Controller<EnumMap<GHOST, MOVE>> ghostModel;
	
	/**
	 * An object which deals with selecting nodes to play.
	 */
	public ISelectionPolicy selectionPolicy;
	
	/**
	 * A collection of additional evaluators which are allowed to modify the scores of the nodes
	 * of the game tree according to their own 'opinion'.
	 */
	public ITreeEvaluator[] additionalEvaluators;
	
	/**
	 * True if the tree is to be discarded when a decision is made. 
	 */
	public boolean discardTreeOnDecision;
	
	/**
	 * Allows the set of parameters to be named for batches of experiments together.
	 */
	public String experimentName;
	
	/**
	 * The ghost controller to play against.
	 */
	public Controller<EnumMap<GHOST, MOVE>> opponent;
	
	/**
	 * The number of simulations to run, or -1 to run as many simulations as possible in real time.
	 */
	public int simulationCount;
	
	/**
	 * True if the decision graph is to be shown.
	 */
	public boolean showGraph;
	
	/**
	 * True if the positions of ghosts is to be taken into account.
	 */
	public boolean useGhostPositions;
	
	/**
	 * True if eating a ghost is to be represented as a node.  This allows Ms PacMan to make a decision
	 * directly after eating a ghost.  Note that this parameter won't have any effect if useGhostPositions = false,
	 * as eating ghosts isn't deterministic unless you take ghost positions into account.
	 */
	public boolean eatGhostNode;
	
	
	/**
	 * Constructor.  Sets default values for the parameters.
	 */
	public MonteCarloPacManParameters()
	{
		nodeExpansionThreshold = 30;
		maximumSimulationLength = 10000000;
		deathPenalty = 10000;
		scaleDeathPenalty = false;
		completionReward = 10000;
		pacManModel = new StarterPacMan();
		ghostModel = new Legacy();
		selectionPolicy = new LevineUcbSelectionPolicy(4000);
		additionalEvaluators = new ITreeEvaluator[] { new DistanceToOpportunityEvaluator(), new PowerPillDistanceEvaluator(), new PowerPillActiveEvaluator() };
		discardTreeOnDecision = true;
		opponent = new Legacy();
		simulationCount = -1;
		showGraph = false;
		useGhostPositions = true;
		eatGhostNode = true;
		
		/*nodeExpansionThreshold = 50;
		maximumSimulationLength = 10000000;
		deathPenalty = 10000;
		scaleDeathPenalty = false;
		completionReward = 10000;
		pacManModel = new StarterPacMan();
		ghostModel = new Legacy2TheReckoning();
		selectionPolicy = new LevineUcbSelectionPolicy(4000);
		additionalEvaluators = new ITreeEvaluator[] { new DistanceToOpportunityEvaluator(), new PowerPillDistanceEvaluator(), new PowerPillActiveEvaluator() };
		discardTreeOnDecision = true;
		opponent = new Legacy();
		simulationCount = -1;
		showGraph = false;
		useGhostPositions = true;
		eatGhostNode = false;*/
	}
	
	
	/**
	 * Returns an exact copy of the parameters object, with the pacManModel and ghostModel being new instances of the original types.
	 * @return
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public MonteCarloPacManParameters copy() throws IllegalAccessException, InstantiationException
	{
		MonteCarloPacManParameters p = new MonteCarloPacManParameters();
		p.nodeExpansionThreshold = nodeExpansionThreshold;
		p.maximumSimulationLength = maximumSimulationLength;
		p.deathPenalty = deathPenalty;
		p.scaleDeathPenalty = scaleDeathPenalty;
		p.completionReward = completionReward;
		p.pacManModel = pacManModel.getClass().newInstance();
		p.ghostModel = ghostModel.getClass().newInstance();
		p.selectionPolicy = selectionPolicy;
		p.additionalEvaluators = additionalEvaluators;
		p.discardTreeOnDecision = discardTreeOnDecision;
		p.experimentName = experimentName;
		p.opponent = opponent.getClass().newInstance();
		return p;
	}
}
