package pacman;

import pacman.entries.pacman.MonteCarloPacManParameters;
import pacman.entries.pacman.MyPacMan;

public class Program
{
	public static void main(String[] args)
	{
		Executor executor = new Executor();
		
		MonteCarloPacManParameters parameters = new MonteCarloPacManParameters();
		executor.runGameTimed(new MyPacMan(parameters), parameters.opponent, true);
	}
}
