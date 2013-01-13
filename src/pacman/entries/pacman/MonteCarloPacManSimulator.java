package pacman.entries.pacman;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import pacman.game.Constants.MOVE;
import pacman.game.Game;


/**
 * Abstracts the Pac-Man game as a game tree and allows Monte Carlo search of this tree.
 */
public class MonteCarloPacManSimulator
{
	private Game game;
	private MonteCarloPacManParameters parameters;
	private Set<Integer> activePowerPills;
	private GameNode rootNode;
	private Stack<Game> gameStates;
	
	/**
	 * Constructor.
	 * @param game The object describing the current game state.
	 * @param pacmanModel The model to use for Pac-Man behaviour during the simulations.
	 * @param ghostModel The model to use for ghost behaviour during the simulations.
	 * @param parameters An object describing the parameters to the simulation.
	 */
	public MonteCarloPacManSimulator(Game game, MonteCarloPacManParameters parameters)
	{
		this.game = game;
		this.parameters = parameters;
		this.rootNode = new GameNode();
		this.gameStates = new Stack<Game>();
		
		this.activePowerPills = new HashSet<Integer>();
		updateActivePowerPills(game.getActivePowerPillsIndices());
	}
	
	
	/**
	 * Attempts to get the best move.  If useGhostPositions is turned on,
	 * then the best move is returned for the current ghost position.
	 * @return
	 */
	public GameNode bestNode()
	{
		double currentScore, max = Double.NEGATIVE_INFINITY;
		GameNode bestNode = null;
		GameNode searchNode;
		
		if (parameters.useGhostPositions)
		{
			//get the node that corresponds to the current ghost position
			searchNode = rootNode.getChild(game);
			//System.out.println("Picked: " + searchNode.getNumberOfVisits());
		}
		else
		{
			//if we don't care about ghost positions, just use the root node
			searchNode = rootNode;
		}
		
		//make sure the search node has been expanded
		if (searchNode.getChildren() != null)
		{
			for (GameNode node: searchNode.getChildren())
			{
				currentScore = node.getAverageScore();
				
				if (currentScore > max)
				{
					max = currentScore;
					bestNode = node;
				}
			}
		}
		
		return bestNode;
	}
	
	
	/**
	 * Gets the collection of nodes which represents the moves PacMan can make.  This is just the children
	 * of the root node unless ghost positions are being used, in which case it is the children of the node
	 * which represents the current ghost position.
	 * @return
	 */
	public Collection<GameNode> getPacManChildren()
	{
		Collection<GameNode> children;
		
		//figure out which collection of children to return
		if (parameters.useGhostPositions)
			children = rootNode.getChild(game).getChildren();
		else
			children = rootNode.getChildren();
		
		//if there is no children, return an empty list instead of null
		if (children != null)
			return children;
		else
			return new ArrayList<GameNode>();
	}
	
	
	
	/**
	 * Gets the move which is on average best regardless of the ghost positions.
	 * @return
	 */
	public MOVE bestOverallMove()
	{
		Map<MOVE, Double> totals = new HashMap<MOVE, Double>();
		MOVE bestMove = MOVE.NEUTRAL;
		double bestScore = Double.NEGATIVE_INFINITY;
		
		//add up the average scores for the moves for each ghost position in the tree
		for (GameNode ghostNode: rootNode.getChildren())
		{
			if (ghostNode.isLeafNode())
				continue;
			
			for (GameNode pacmanNode: ghostNode.getChildren())
			{
				Double total = totals.get(pacmanNode.getMove());
				totals.put(pacmanNode.getMove(), total == null ? pacmanNode.getAverageScore() : total + pacmanNode.getAverageScore());
			}
		}
		
		//find the move with the highest total score
		for (Map.Entry<MOVE, Double> entry: totals.entrySet())
		{
			if (entry.getValue() > bestScore)
			{
				bestScore = entry.getValue();
				bestMove = entry.getKey();
			}
		}
		
		return bestMove;
	}
	
	
	/**
	 * Runs a Monte Carlo simulation from the current point in the game.
	 */
	public void runSimulation()
	{
		List<GameNode> visitedNodes = new ArrayList<GameNode>();
		
		//save the number of lives so we can tell if we've lost a life during the simulation
		int lives = game.getPacmanNumberOfLivesRemaining();
				
		//save the game at its current point so we can put it back after the simulation
		pushGameState();
		
		try
		{
			//the first node is the root node
			GameNode node = rootNode;
			visitedNodes.add(node);
			advanceGameToNextNode();
			
			//select the child representing the move played by the ghost team model
			if (parameters.useGhostPositions)
				node = node.getChild(game);
			
			//walk through the tree according to nodes with the highest UCB value,
			//until a leaf node is reached
			while (!node.isLeafNode())
			{
				node = parameters.selectionPolicy.selectChild(node);
				
				if (node == null)
					return;
					
				//save the nodes we visit so we can update their scores later
				visitedNodes.add(node);
				
				//move the game state to this node
				playMove(node.getMove());
				advanceGameToNextNode();
				
				if (parameters.useGhostPositions)
					node = node.getChild(game);
			}
			
			//expand the node and pick one of its children if it's been sampled enough,
			//otherwise just use the node we've arrived at (always expand the root node)
			if (node.getNumberOfVisits() >= parameters.nodeExpansionThreshold || node == rootNode)
			{
				node.expand(game);

				//some selection policies need all children to be evaluated first
				if (parameters.selectionPolicy.getEvaluateAllChildrenOnExpansion())
				{
					//run a simulation from each child
					for (GameNode child: node.getChildren())
					{
						int powerPillCount = game.getNumberOfActivePowerPills();
						int pillCount = game.getNumberOfActivePills();
						int level = game.getCurrentLevel();
						
						//copy the game and play the move that this child represents
						pushGameState();
						playMove(child.getMove());
						advanceGameToNextNode();
						
						//if the move ate a power pill, mark it as such
						if (game.getNumberOfActivePowerPills() < powerPillCount)
						{
							child.setMoveEatsPowerPill(true);
						}
						
						//if the move ate any pills, mark it as such
						if (game.getNumberOfActivePills() < pillCount)
						{
							child.setMoveEatsPills(true);
						}
						
						int score = 0;
						
						//if the move completes the level, give it a bonus
						if (game.getCurrentLevel() > level)
							score += parameters.completionReward;
						
						//if we're using ghost positions, make sure the current ghost position is
						//in the tree
						if (parameters.useGhostPositions)
							child.getChild(game);
						
						//run the roll out
						score += runSimulation(visitedNodes, lives);
						child.updateScore(score);
						
						//restore the game state
						popGameState();
					}
				}
				
				node = parameters.selectionPolicy.selectChild(node);
				
				if (node == null)
					return;
				
				visitedNodes.add(node);
				
				//move the game state to this node
				playMove(node.getMove());
				
				//make sure the ghost move is in the tree (if we care about such things)
				//and run the rollout
				if (parameters.useGhostPositions)			
					node.getChild(game);
			}
			
			runSimulation(visitedNodes, lives);
		}
		finally
		{
			//restore the game state
			popGameState();
		}
	}
	
	
	private int runSimulation(List<GameNode> visitedNodes, int lives)
	{
		int score = 0;

		//apply a penalty if we've lost a life
		if (game.getPacmanNumberOfLivesRemaining() < lives)
		{
			if (parameters.scaleDeathPenalty)
			{
				//scale the death penalty by the number of visits at the node being evaluated
				score -= visitedNodes.get(visitedNodes.size() - 1).getNumberOfVisits();
			}
			else
			{
				score -= parameters.deathPenalty;
			}
		}
		
		//simulate the game to the end and get the score
		score += rollout();
		
		//update the node scores
		for (GameNode n: visitedNodes)
		{
			n.updateScore(score);
		}
		
		return score;
	}
	
	
	/**
	 * Plays a game using the specified ghost and pacman models until the end of level, game over or
	 * simulation limit.
	 * @return The score at the end of the simulation.
	 */
	private int rollout()
	{
		//save the level so we can end the simulation if Pac-Man progresses onto the next level
		int level = game.getCurrentLevel();
		int i = 0;
		
		//run up to the end of the level, until game over or until we've reached the simulation limit
		while (i++ < parameters.maximumSimulationLength
			&& !game.gameOver()
			&& game.getCurrentLevel() == level)
		{
			game.advanceGame(parameters.pacManModel.getMove(game, 0), parameters.ghostModel.getMove(game, 0));
		}
		
		//update the score
		int score = game.getScore();
		
		return score;
	}
	
	
	/**
	 * Advances the specified game object to the next node in the graph, that is, the next Pac-Man decision point (or game over).
	 * @param ghostModel The controller to use to model the ghost's behaviour.
	 * @return True if Ms Pac-Man made it to the next node without being eaten; otherwise, false.
	 */
	public void advanceGameToNextNode()
	{
		MOVE move = game.getPacmanLastMoveMade();
		
		//save the ghost edible score so we can easily detect when a ghost is eaten
		int edibleScore = game.getGhostCurrentEdibleScore();
		
		while (!isAtNode(edibleScore))
		{
			//advance the game
			//opponent models that care about the amount of time they have to return an answer won't work here
			//but we can't allow lengthy simulations to run for ghost behaviour or we'll run out of time
			game.advanceGame(move, parameters.ghostModel.getMove(game, 0));
		}
	}
	
	
	/**
	 * Advances the game by playing the move specified for Ms. Pac-Man.
	 * @param node
	 */
	public void playMove(MOVE move)
	{
		//play the move that the node represents
		game.advanceGame(move, parameters.ghostModel.getMove(game, 0));
		
		//update active power pill indices if necessary
		int[] indices = game.getActivePowerPillsIndices();
		
		if (indices.length < activePowerPills.size())
		{
			updateActivePowerPills(indices);
		}
	}
	
	
	/**
	 * Determines if the current game position is a node in the graph; i.e., if it is a Pac-Man decision point (or game over).
	 * @return
	 */
	public boolean isAtNode(int edibleScore)
	{
		int nodeIndex = game.getPacmanCurrentNodeIndex();
		
		return game.gameOver() 
			|| game.isJunction(nodeIndex)
			|| activePowerPills.contains(nodeIndex)
			|| againstWall()
			|| (parameters.eatGhostNode && parameters.useGhostPositions && game.getGhostCurrentEdibleScore() != edibleScore);
	}
	
	
	/**
	 * Determines if Ms. Pac-Man has ran into a wall based on the direction she was previously going in.
	 * @return
	 */
	public boolean againstWall()
	{
		MOVE move = game.getPacmanLastMoveMade();
		MOVE[] possibleMoves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());
		
		for (int i = 0; i < possibleMoves.length; i++)
		{
			if (possibleMoves[i] == move)
				return false;
		}
		
		return true;
	}
	
	
	/**
	 * Saves the game state to a stack and uses a copy of it for the current game state.
	 */
	public Game pushGameState()
	{
		gameStates.push(game);
		game = game.copy();
		return game;
	}
	
	
	/**
	 * Pops a saved game state off the stack to use as the current game state.
	 */
	public void popGameState()
	{
		game = gameStates.pop();
	}
	
	
	/**
	 * Gets the current game state.
	 */
	public Game getGameState()
	{
		return game;
	}
	
	
	/**
	 * Sets the current game state.
	 * @param value
	 */
	public void setGameState(Game value)
	{
		game = value;
	}
	
	
	/**
	 * Gets the root node of the search tree.
	 * @return
	 */
	public GameNode getRootNode()
	{
		return rootNode;
	}
	
	
	/**
	 * Sets the root node of the tree.
	 * @param node
	 */
	public void setRootNode(GameNode node)
	{
		rootNode = node;
	}
	
	
	/**
	 * Updates the locations of the active power pills.
	 * @param indices
	 */
	private void updateActivePowerPills(int[] indices)
	{
		for (int index: indices)
		{
			activePowerPills.add(index);
		}
	}
}
