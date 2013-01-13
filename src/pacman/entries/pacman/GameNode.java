package pacman.entries.pacman;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;


public class GameNode
{
	private MOVE move;
	private int totalScore, numberOfVisits, scoreBonus;
	private GameNode parent;
	private Map<Object, GameNode> children;
	private int nodeIndex;
	private long sumOfSquares;
	private double mean;
	private boolean moveEatsPowerPill;
	private boolean moveEatsPills;
	private long ghostPositions;
	
	/**
	 * Constructor for root nodes. 
	 */
	public GameNode()
	{
		totalScore = 0;
		numberOfVisits = 0;
		move = MOVE.NEUTRAL;
		nodeIndex = -1;
		scoreBonus = 0;
	}
	
	/**
	 * Constructor for child nodes.
	 * @param parent The parent of this node.
	 * @param move The move that this node represents.
	 */
	private GameNode(GameNode parent, MOVE move)
	{
		this();
		this.parent = parent;
		this.move = move;
	}
	
	
	/**
	 * Updates the score and visit count of this node.
	 * @param score
	 */
	public void updateScore(int score)
	{
		totalScore += score;
		numberOfVisits++;
		
		//calculations for variance - Knuth, Art of Computer Programming, volume 2
		if (numberOfVisits == 1)
		{
			mean = score;
			sumOfSquares = 0;
		}
		else
		{
			double lastMean = mean;
			mean += (score - lastMean) / numberOfVisits;
			sumOfSquares += (score - lastMean) * (score - mean);
		}
	}
	
	
	/**
	 * Adds a bonus to the average score.
	 * @param bonus
	 */
	public void addScoreBonus(int bonus)
	{
		this.scoreBonus += bonus;
	}
	
	
	/**
	 * Expands this node by adding children based on the possible moves from the current position in game. 
	 * @param game
	 */
	public void expand(Game game)
	{
		MOVE[] possibleMoves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());
		children = new HashMap<Object, GameNode>(possibleMoves.length);
		
		//node index is stored so that hints can be drawn on the Pac-Man view
		nodeIndex = game.getPacmanCurrentNodeIndex();
		
		for (int i = 0; i < possibleMoves.length; i++)
		{
			children.put(possibleMoves[i], new GameNode(this, possibleMoves[i]));
		}
	}
	
	
	/**
	 * Determines if this node has any children or not.
	 * @return True if the node has no children; otherwise, false.
	 */
	public boolean isLeafNode()
	{
		return children == null;
	}
	
	
	/**
	 * Gets the number of times this node has been visited.
	 * @return
	 */
	public int getNumberOfVisits()
	{
		return numberOfVisits;
	}
	
	
	/**
	 * Increments the visit count by 1.
	 */
	public void incrementNumberOfVisits()
	{
		numberOfVisits++;
	}

	
	/**
	 * Gets the move that this node represents.
	 * @return
	 */
	public MOVE getMove()
	{
		return move;
	}
	
	
	/**
	 * Gets the children of this node.
	 * @return
	 */
	public Collection<GameNode> getChildren()
	{
		if (children == null)
			return null;
		
		return children.values();
	}
	
	
	/**
	 * Gets the index of the Pac-Man game node this tree node represents.
	 * @return
	 */
	public int getNodeIndex()
	{
		return nodeIndex;
	}
	
	
	/**
	 * Gets the parent node for this node.
	 * @return
	 */
	public GameNode getParent()
	{
		return parent;
	}
	
	
	/**
	 * Gets the total score for the node.
	 * @return
	 */
	public int getTotalScore()
	{
		return totalScore;
	}
	
	
	/**
	 * Gets the average score for this node, plus any bonuses.
	 * @return
	 */
	public double getAverageScore()
	{
		if (numberOfVisits > 0)
			return mean + scoreBonus;
		else
			return scoreBonus;
	}
	
	
	/**
	 * Gets the variance (σ²) for this node.
	 * @return
	 */
	public double getVariance()
	{
		if (numberOfVisits > 1)
			return sumOfSquares / (numberOfVisits - 1);
		else
			return 0;
	}
	
	
	/**
	 * Gets whether or not this move eats a power pill.
	 * @return True if, after executing this move, a power pill is eaten; otherwise, false.
	 */
	public boolean getMoveEatsPowerPill()
	{
		return moveEatsPowerPill;
	}
	
	
	/**
	 * Sets whether or not this move eats a power pill.
	 * @param value
	 */
	public void setMoveEatsPowerPill(boolean value)
	{
		moveEatsPowerPill = value;
	}
	
	
	/**
	 * Gets whether or not this move results in one or more pills being eaten.
	 * @return True if, after executing this move, one or more pills are eaten; otherwise, false.
	 */
	public boolean getMoveEatsPills()
	{
		return moveEatsPills;
	}
	
	
	/**
	 * Sets whether or not this move eats one or more pills.
	 * @param value
	 */
	public void setMoveEatsPills(boolean value)
	{
		moveEatsPills = value;
	}
	
	
	/**
	 * Gets whether or not there exists a move subsequent to this move which results in pills being eaten.
	 * @return
	 */
	public boolean getCanEatPillsOnSubsequentMove()
	{
		//if there's no children, return false
		if (children == null)
			return false;
		
		//basically, this condition is true if any of the child moves eat pills
		for (GameNode child: children.values())
		{
			if (child.getMoveEatsPills())
				return true;
		}
		
		return false;
	}
	
	
	/**
	 * Gets the child node which corresponds to the specified move, or null if no such child exists.
	 * @param move
	 * @return
	 */
	public GameNode getChild(MOVE move)
	{
		if (children == null)
			return null;
		
		return children.get(move);
	}
	
	
	/**
	 * Gets the child node which corresponds to the ghost positions in the specified game instance.
	 * @param game
	 * @return The node representing the ghost position if there is one; otherwise, a new one is created,
	 * added to the children collection, and returned.
	 */
	public GameNode getChild(Game game)
	{
		//translate the ghost positions in the game into a long integer
		long position = getGhostPositions(game);
		GameNode node;
		
		//check if there's any children yet
		if (children == null)
		{
			children = new HashMap<Object, GameNode>();
		}
		else
		{
			node = children.get(position);
			
			if (node != null)
			{
				//found the current position, increment it's number of visits
				node.incrementNumberOfVisits();
				return node;
			}
		}

		//if there's no children, or the position couldn't be found, we'll need to add it
		node = new GameNode(this, MOVE.NEUTRAL);
		node.incrementNumberOfVisits();
		node.setGhostPositions(position);
		children.put(position, node);

		return node;
	}
	
	
	/**
	 * Gets the long integer containing all the ghost positions for the specified game instance.
	 * @param game
	 * @return
	 */
	private long getGhostPositions(Game game)
	{
		long positions;
		positions = game.getGhostCurrentNodeIndex(GHOST.BLINKY);
		positions <<= 16;
		positions |= game.getGhostCurrentNodeIndex(GHOST.INKY);
		positions <<= 16;
		positions |= game.getGhostCurrentNodeIndex(GHOST.PINKY);
		positions <<= 16;
		positions |= game.getGhostCurrentNodeIndex(GHOST.SUE);
		return positions;
	}
	
	
	/**
	 * Gets the ghost positions stored for this node.
	 * @return
	 */
	public long getGhostPositions()
	{
		return ghostPositions;
	}
	
	/**
	 * Sets the ghost positions to the specified value.
	 * @param value
	 */
	public void setGhostPositions(long value)
	{
		ghostPositions = value;
	}
}
