package edu.upenn.cis455.webserver;
import java.net.Socket;
import java.util.ArrayList;

/**
 * This thread pool contains fixed-sized workers 
 * to process tasks in the queue. 
 * @author Yayang Tian
 */
public class ThreadPool {
	private ArrayList<Worker> pool = new ArrayList<Worker>();
	private int numWorkers = 10;
	
	public ThreadPool(int num){
		numWorkers = num;
		for(int i = 0; i < numWorkers; i ++){
			// each worker should remember the queue to serve
			Worker worker = new Worker(Dispatcher.getTaskQueue());
			pool.add(worker);
			worker.start();
		}
	}
	
	public void addTask(Socket task){
		if(!Dispatcher.isShutdown()){
			TaskQueue queue = Dispatcher.getTaskQueue();
			queue.add(task);
		}
		else throw new IllegalStateException
		("You cannot add task because thread pool is stopped.");
	}
	
	public synchronized void stopAll(){
		if(pool != null){
			for(Worker w : pool){
				w.stopThread();
			}
		}
	}
	
	public synchronized ArrayList<Worker> getWorkers(){
		return pool;
	}
	public boolean shutdown(){
		return Dispatcher.isShutdown();
	}
}
