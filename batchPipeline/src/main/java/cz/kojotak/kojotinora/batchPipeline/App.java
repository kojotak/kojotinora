package cz.kojotak.kojotinora.batchPipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class App {
	
	static final int INPUT_SIZE = 1000;
	static final long SLEEP=1000;

	final int useParallel;
	final int batchSize;
	final BlockingQueue<String> queue;
	final Semaphore semaphore = new Semaphore(1, true);
	final ExecutorService executor;
	final AtomicInteger toConsume = new AtomicInteger(INPUT_SIZE);
	
	public App(int useParallel, int queueSize, int batchSize) throws Exception {
		this.useParallel = useParallel;
		this.batchSize = batchSize;
		this.queue = new LinkedBlockingDeque<String>(queueSize);
		this.executor = Executors.newFixedThreadPool(2*useParallel+1);
		List<Callable<Object>> callables = new ArrayList<Callable<Object>>();
		for(int i = 1; i <= useParallel; i++){
			callables.add(Executors.callable(new Producer(i)));
			callables.add(Executors.callable(new Consumer(i)));
		}
		callables.add(Executors.callable(new Checker()));
		executor.invokeAll(callables);
		executor.shutdown();
	}
	
	public static void main(String[] args) throws Exception {
		App app = new App(4, 100, 10);
	}
	
	class Checker implements Runnable {

		public void run() {
			while(true){
				System.err.println("queue size: "+queue.size());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					System.err.println("end, size: "+queue.size());
				}
			}
		}
		
	}
	
	class Consumer implements Runnable {
		private int id;
		public Consumer(int id){
			this.id = id;
		}

		public void run() {
			Random random = new Random();
			while(toConsume.get() > 0){
				List<String> batch = new ArrayList<String>();
				try {
					semaphore.acquire();
					for(int i=1; i<= batchSize; i++){
						String string = queue.take();
						batch.add(string);
						if( toConsume.decrementAndGet() <= 0){
							break;
						}
					}
					semaphore.release(1);
				} catch (InterruptedException e) {
					System.err.println("Consumer "+id+" interrupted");
					return;
				}
				try {
					Thread.sleep( 50000 );
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
				System.out.println("Consumer "+id+": "+batch);
			}
			System.err.println("Consumer "+id+" finished");
			System.err.println(executor.shutdownNow());;
		}
		
	}
	
	class Producer implements Runnable {
		final int base;
		public Producer(int base){
			this.base = base;
		}
		public void run() {
			Random random = new Random();
			for(int i=1; i<= perThread(); i++){
				try {
					String token = "Producer "+base+": "+i; 
					queue.put(token);
					System.out.println(token);
					Thread.sleep( 2000 );
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
			}
			System.err.println("Producer "+base+" finished");
		}
		
	}
	
	int perThread(){
		return INPUT_SIZE / useParallel;
	}
	
	
}
