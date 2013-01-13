
package pacman;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MultithreadedWorker
{
	private final BlockingQueue<Runnable> queue;
	private final WorkerThread[] threads;
	
	
	public MultithreadedWorker(int numberOfThreads)
	{
		queue = new LinkedBlockingQueue<Runnable>();
		threads = new WorkerThread[numberOfThreads];
		
		for (int i = 0; i < threads.length; i++)
		{
			threads[i] = new WorkerThread();
			threads[i].start();
		}
	}
	
	
	public void queue(Runnable task)
	{
		queue.add(task);
	}
	
	
	public void queue(Runnable task, int iterations)
	{
		for (int i = 0; i < iterations; i++)
		{
			queue.add(task);
		}
	}
	
	
	private class WorkerThread extends Thread
	{
		@Override
		public void run()
		{
			while (true)
			{
				try
				{
					Runnable task = queue.take();
					task.run();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
	}
}
