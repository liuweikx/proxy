package com.tigerknows.proxy.concurrency;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ThreadPool extends ThreadGroup {
	
	private Log log = LogFactory.getLog(ThreadPool.class);
	private boolean isClosed = false; // 线程池是否关闭
	private LinkedBlockingQueue <Runnable> workQueue; // 工作队列
	private String name; // 线程池的id
	private int queueSize = 30000;

	public ThreadPool(String name, int threadPoolID, int poolSize) { // poolSize 表示线程池中的工作线程的数量
		super(threadPoolID + ""); // 指定ThreadGroup的名称
		this.name = name;
		setDaemon(true); // 继承到的方法，设置是否守护线程池
		workQueue = new LinkedBlockingQueue<Runnable>(queueSize); // 创建工作队列
		for (int i = 0; i < poolSize; i++) {
			new WorkThread(i).start(); // 创建并启动工作线程,线程池数量是多少就创建多少个工作线程
		}
	}
	
//	public ThreadPool(int poolSize, int queueSize) { // poolSize 表示线程池中的工作线程的数量
//		super(threadPoolID + ""); // 指定ThreadGroup的名称
//		setDaemon(true); // 继承到的方法，设置是否守护线程池
//		this.queueSize = queueSize;
//		workQueue = new LinkedBlockingQueue<Runnable>(this.queueSize); // 创建工作队列
//		for (int i = 0; i < poolSize; i++) {
//			new WorkThread(i).start(); // 创建并启动工作线程,线程池数量是多少就创建多少个工作线程
//		}
//	}
	
	/** 向工作队列中加入一个新任务,由工作线程去执行该任务 */
	public synchronized void execute(Runnable task) {
		if (isClosed) {
			throw new IllegalStateException();
		}
		if (task != null) {
			try {
				workQueue.put(task);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}// 向队列中加入一个任务
			notify(); // 唤醒一个正在getTask()方法中待任务的工作线程
		}
	}

	/** 从工作队列中取出一个任务,工作线程会调用此方法 */
	private Runnable getTask(int threadid)
			throws InterruptedException {
		if (workQueue.size() == 0 && isClosed)
			return null;
		log.debug("线程池" + name + " 工作线程" + threadid + "等待任务...");
		return (Runnable) workQueue.take(); // 反回队列中第一个元素,并从队列中删除
	}

	/** 关闭线程池 */
	public synchronized void closePool() {
		if (!isClosed) {
			waitFinish(); // 等待工作线程执行完毕
			isClosed = true;
			workQueue.clear(); // 清空工作队列
			interrupt(); // 中断线程池中的所有的工作线程,此方法继承自ThreadGroup类
		}
	}

	/** 等待工作线程把所有任务执行完毕 */
	public void waitFinish() {
		synchronized (this) {
			isClosed = true;
			notifyAll(); // 唤醒所有还在getTask()方法中等待任务的工作线程
		}
		Thread[] threads = new Thread[activeCount()]; // activeCount()
														// 返回该线程组中活动线程的估计值。
		int count = enumerate(threads); // enumerate()方法继承自ThreadGroup类，根据活动线程的估计值获得线程组中当前所有活动的工作线程
		for (int i = 0; i < count; i++) { // 等待所有工作线程结束
			try {
				threads[i].join(); // 等待工作线程结束
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * 内部类,工作线程,负责从工作队列中取出任务,并执行
	 */
	private class WorkThread extends Thread {
		private int id;

		public WorkThread(int id) {
			// 父类构造方法,将线程加入到当前ThreadPool线程组中
			super(ThreadPool.this, id + "");
			this.id = id;
		}

		public void run() {
			while (!isInterrupted()) { // isInterrupted()方法继承自Thread类，判断线程是否被中断
				Runnable task = null;
				try {
					task = getTask(id); // 取出任务
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
				// 如果getTask()返回null或者线程执行getTask()时被中断，则结束此线程
				if (task == null)
					return;

				try {
					log.debug("线程池" + name + " 工作线程" + id + "开始执行任务...");
					task.run(); // 运行任务
					log.debug("线程池" + name + " 工作线程" + id + "执行任务完毕...");
				} catch (Throwable t) {
					t.printStackTrace();
					
				}
			}
			new WorkThread(id).start();//某个线程异常退出后，立马启动一个
		}
	}
	
	public int getWorkerNum(){
		return workQueue.size();
	}

	public static void main(String[] args) throws InterruptedException {
		ThreadPool threadPool = new ThreadPool("xx",1,3); // 创建一个有个3工作线程的线程池
		Thread.sleep(500); // 休眠500毫秒,以便让线程池中的工作线程全部运行
		// 运行任务
		for (int i = 0; i <= 5; i++) {
			threadPool.execute(createTaskTest(i));
		}
		System.out.println("pause");
		
//		Thread.sleep(5000); // 休眠500毫秒,以便让线程池中的工作线程全部运行
//		for (int i = 500; i <= 1000; i++) {
//			threadPool.execute(createTaskTest(i));
//		}
//		threadPool.waitFinish(); // 等待所有任务执行完毕
//		threadPool.closePool(); // 关闭线程池

	}

	private static Runnable createTaskTest(final int taskID) {
		return new Runnable() {
			public void run() {
			    System.out.println("Task" + taskID + "开始");
				System.out.println("Hello world " + taskID);
				System.out.println("Task" + taskID + "结束");
			}
		};
	}

}
