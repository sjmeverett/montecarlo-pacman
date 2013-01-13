package pacman.entries.pacman;

import java.util.Random;

import pacman.controllers.Controller;
import pacman.entries.pacman.evaluators.ITreeEvaluator;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Implements a PacMan agent using Monte Carlo tree search.
 */
public class MyPacMan extends Controller<MOVE>
{
	//we don't want to run simulations right up until the move is due, or we'll miss making a move
	private int timeBuffer = 2;
	private int lastEdibleScore;
	
	private MonteCarloPacManSimulator simulator;
	private MonteCarloPacManParameters parameters;
	
	/**
	 * Constructor.
	 * @param parameters Parameters governing various aspects of the algorithms used.
	 */
	public MyPacMan(MonteCarloPacManParameters parameters)
	{
		this.parameters = parameters;
	}
	
	
	public MyPacMan()
	{
		this.parameters = new MonteCarloPacManParameters();
	}
	

	public MOVE getMove(Game game, long timeDue) 
	{
		MOVE move = MOVE.NEUTRAL;
		
		if (simulator == null)
		{
			//first move, just pick a random one because it shouldn't really matter too much
			Random random = new Random();
			MOVE[] possibleMoves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());
			move = possibleMoves[random.nextInt(possibleMoves.length)];
			
			//lastEdibleScore won't be set yet since this is the first move
			lastEdibleScore = game.getGhostCurrentEdibleScore();
			
			//make a new simulator with a copy of the game state so that we can play the move
			//we picked, in order for the simulator to know what direction Ms Pac-Man is going
			simulator = new MonteCarloPacManSimulator(game, parameters);
			simulator.playMove(move);
		}
		else
		{
			//update the simulator with the current game state
			simulator.setGameState(game);
		}
		
		
		//if we need to make a decision this step, and there's evaluators to be run,
		//give them some time to run
		if (simulator.isAtNode(lastEdibleScore) && parameters.additionalEvaluators != null)
		{
			timeDue -= 20;
		}
		
		
		//run simulations until it's time to return (if in real-time mode)
		if (parameters.simulationCount == -1)
		{
			while (System.currentTimeMillis() < timeDue - timeBuffer)
			//for (int i = 0; i < 20; i++)
			{
				simulator.runSimulation();
			}
		}
		
		//check if we need to make a decision
		//if it's the first move, move will already have been assigned a random value
		if (simulator.isAtNode(lastEdibleScore) && move == MOVE.NEUTRAL)
		{
			//if we're not in real-time mode, run the number of simulations required
			if (parameters.simulationCount > -1)
			{
				for (int i = 0; i < parameters.simulationCount; i++)
				{
					simulator.runSimulation();
				}
			}
			
			//let other evaluators add their 'opinion'
			runAdditionalEvaluators();
			
			//pick the move with the best score
			GameNode node = simulator.bestNode();
			
			if (node == null)
			{
				//haven't reached the expansion threshold yet
				//if we're using ghost positions, hopefully we can use the best overall
				//it might still return MOVE.NEUTRAL though
				if (parameters.useGhostPositions)
					move = simulator.bestOverallMove();
			}
			else
			{
				move = node.getMove();
			}
			
			if (parameters.discardTreeOnDecision || node == null)
			{
				//make a fresh tree etc for the next decision
				//System.out.println("Set new root");
				simulator = new MonteCarloPacManSimulator(game, parameters);
			}
			else
			{
				//use chosen node as new root
				simulator.setRootNode(node);
			}
			
			if (System.currentTimeMillis() > timeDue)
			{
				//oops, too late: increase the time buffer so we're not late next time
				//timeBuffer++;
				//System.out.println("Too late!");
			}
		}
		
		//save the edible score so that we can detect if it changes
		lastEdibleScore = game.getGhostCurrentEdibleScore();
		
        return move;
	}
	
	
	/**
	 * Runs any registered tree evaluators. 
	 */
	private void runAdditionalEvaluators()
	{
		if (parameters.additionalEvaluators != null)
		{
			for (ITreeEvaluator evaluator: parameters.additionalEvaluators)
			{
				evaluator.evaluateTree(simulator);
			}
		}
	}
}