package pacman;
import static pacman.game.Constants.DELAY;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import pacman.controllers.Controller;
import pacman.entries.pacman.MonteCarloPacManParameters;
import pacman.entries.pacman.MyPacMan;
import pacman.game.Constants.MOVE;
import pacman.game.Game;


public class Runner
{
	private final Executor exec = new Executor();
	private MultithreadedWorker worker;
	
	
	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("Usage: java -jar PacMan.java <parameters script>");
			return;
		}
		else
		{
			Runner runner = new Runner();
			
			try
			{
				runner.run(args[0]);
			}
			catch (FileNotFoundException e)
			{
				System.out.println("File not found: " + args[0]);
			}
			catch (ScriptException e)
			{
				System.out.println("Error: " + e.getMessage());
			}
			catch (IllegalStateException e)
			{
				System.out.println("Error: " + e.getMessage());
			}
		}
	}
	
	public class ScriptHost
	{
		public Queue<MonteCarloPacManParameters> runs;
		public String mode;
		public int numberOfThreads;
		
		public ScriptHost()
		{
			runs = new LinkedList<MonteCarloPacManParameters>();
			mode = "interactive";
			numberOfThreads = 6;
		}
	}
	
	
	public void run(String path) throws FileNotFoundException, ScriptException
	{
		ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        ScriptHost host = new ScriptHost();
        
        String script = new Scanner(new File(path)).useDelimiter("\\Z").next();
        
        String hostScript = 
        	"importPackage(Packages.pacman.entries.pacman);" +
        	"importPackage(Packages.pacman.entries.pacman.evaluators);" +
        	"importPackage(Packages.pacman.entries.pacman.evaluators.ensemble);" +
        	"importPackage(Packages.pacman.entries.pacman.selectionpolicies);" +
        	"importPackage(Packages.pacman.controllers.examples);" +
        	"with (host) { " +
        	script +
        	"}";
        
        engine.put("host", host);
        engine.eval(hostScript);
        
        if (host.mode.equals("interactive"))
        {
        	runInteractive(host.runs.remove());
        }
        else if (host.mode.equals("batch"))
        {
        	runBatch(host.runs, host.numberOfThreads);
        }
        else
        {
        	throw new IllegalStateException("Unknown mode: " + host.mode);
        }
	}
	
	
	private void runBatch(Queue<MonteCarloPacManParameters> runs, int numberOfThreads)
	{
		worker = new MultithreadedWorker(numberOfThreads);
		final Random random = new Random();
		
		try
		{
			while (!runs.isEmpty())
			{
				MonteCarloPacManParameters runbatch = runs.remove();
				
				for (int i = 0; i < 20; i++)
				{
					final MonteCarloPacManParameters run = runbatch.copy();
					
					worker.queue(new Runnable()
					{
						@Override
						public void run()
						{
							Controller<MOVE> pacman = new MyPacMan(run);
					    	Game game;
					    	
					    	game = new Game(random.nextLong());
					    	
					    	while (!game.gameOver())
					    	{
					    		game.advanceGame(
					    			pacman.getMove(game.copy(), System.currentTimeMillis() + DELAY),
					    			run.opponent.getMove(game.copy(), System.currentTimeMillis() + DELAY)
					    		);
					    	}
					    	
					    	System.out.printf("%s\t%d\n", run.experimentName, game.getScore());
						}
					});
				}
			}
		}
		catch (IllegalAccessException ex)
		{
			ex.printStackTrace();
		}
		catch (InstantiationException ex)
		{
			ex.printStackTrace();
		}
	}
	
	
	private void runInteractive(MonteCarloPacManParameters p)
	{
		exec.runGame(new MyPacMan(p), p.opponent, true, 5);
	}
}
