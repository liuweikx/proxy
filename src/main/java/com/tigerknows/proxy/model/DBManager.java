package com.tigerknows.proxy.model;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tigerknows.proxy.concurrency.ShareData;
import com.tigerknows.proxy.concurrency.ThreadPool;
import com.tigerknows.proxy.config.ConfigConstants;
import com.tigerknows.proxy.dao.AgentDao;
import com.tigerknows.proxy.domain.Agent;
import com.tigerknows.proxy.domain.AgentSquads;
import com.tigerknows.proxy.exception.ExceptionUtil;

public class DBManager {

	private static Log log = LogFactory.getLog(DBManager.class);

	private AtomicInteger counter;

	private ThreadPool tPool;

	private static AgentDao agentDao;

	public DBManager(DataSource ds) {

		this.counter = new AtomicInteger(0);

		this.tPool = new ThreadPool("DBManager", 1, 64);

		agentDao = new AgentDao(ds);

	}

	public void work() {
		new WorkThread().start();
		try {
			Thread.sleep(1000);
			log.info("DBManager start to work");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private class WorkThread extends Thread {

		public void run() {
			while (!isInterrupted()) {
				try {
					if (tPool.getWorkerNum() > ConfigConstants.DB_QUNENE_MAXSIZE * 3) {
						Thread.sleep(60000);
						continue;
					}
					List<Agent> agents = agentDao.getOriAgents();
					log.info("get agents :" + agents.size());
					counter.set(0);
					for (Agent agent : agents){
						tPool.execute(createTask(agent, agentDao, counter));
						tPool.execute(createSpecialTask(agent,ConfigConstants.URL_DianPing, ConfigConstants.Protocol_Http));
					}
					// Thread.sleep(ConfigConstants.sleepTime);
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					log.error(ExceptionUtil.getExceptionString(e));
				}
			}
			new WorkThread().start();// 某个线程异常退出后，立马启动一个
		}
	}
	
	public static void killAgent(Agent agent){
		agentDao.killAgent(agent);
	}

	private static Runnable createTask(final Agent agent,
			final AgentDao agentDao, final AtomicInteger counter) {
		return new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				if (agent.testAgent(ConfigConstants.URL_TEST_SPEED, 1) > 0) {
					// 加入队列
					try {
						ShareData.agentQueue.put(agent.clone());
						if (ConfigConstants.Protocol_socks5.equals(agent.getProtocol()))
							ShareData.sock5LiveAgents.put(agent.getId(), agent.clone());
						else
							ShareData.httpLiveAgents.put(agent.getId(), agent.clone());
						counter.getAndIncrement();
						log.info(" live agents : " + counter.get() + " "
								+ agent.getIP());
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					agentDao.killAgent(agent);
					log.info(" kill agents : " + agent.getIP());
				}
			}
		};
	}
	
	private static Runnable createSpecialTask(final Agent agent, final String url, final String protocol) {
		return new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				if (agent.testAgent(url, 1) > 0) {
					ConcurrentHashMap<String, AgentSquads> fruits = null;
					if (ConfigConstants.Protocol_socks5.equals(protocol))
						fruits = ShareData.sockFruits;
					else
						fruits = ShareData.httpFruits;
					AgentSquads as = fruits.get(url);
					if (as == null){
						as = new AgentSquads();
						as.addAgent(agent);
						fruits.put(url, as);
					}else{
						as.addAgent(agent);
					}
				}
			}
		};
	}
}
