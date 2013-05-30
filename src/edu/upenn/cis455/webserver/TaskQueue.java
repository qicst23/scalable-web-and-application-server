package edu.upenn.cis455.webserver;

import java.net.Socket;
import java.util.LinkedList;

/**
 * This class queues incoming client request.
 * @author Yayang Tian
 */
public class TaskQueue {
	private LinkedList<Socket> queue;
	private final int CAPACITY = 1000;
	String rootDir;// each task is based on the root directory
	
	public TaskQueue(String root){
		queue = new LinkedList<Socket>();
		rootDir = root;
	}
	
	public synchronized boolean add(Socket socket){
		if(queue.size() < 1000){
			queue.add(socket);
			// Important: when task added to Q, wake up sleeping threads 
			notify();
			return true;
		}else
			return false;                 
	}
	
	public synchronized Socket remove() throws InterruptedException{
		if(queue.size() == 0){
			// all worker threads wait for tasks if Q is empty
			wait(); 
		}
		return queue.remove();
	}
	
	public int size(){
		return queue.size();
	}
	
	public int max(){
		return CAPACITY;
	}
}
