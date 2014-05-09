package com.tigerknows.proxy.model;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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

public class WarmUpManager {

	private static Log log = LogFactory.getLog(WarmUpManager.class);

	private ThreadPool tPool;

	private static AgentDao agentDao;

	public WarmUpManager(DataSource ds) {

		this.tPool = new ThreadPool("WarmManager", 3, 128);

		agentDao = new AgentDao(ds);

	}

	public void work() {
		new WorkThread().start();
		try {
			Thread.sleep(5000);
			log.info("WarmUpManager start to work");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private class WorkThread extends Thread {

		public void run() {
			while (!isInterrupted()) {
				try {
					if (tPool.getWorkerNum() > ConfigConstants.DB_QUNENE_MAXSIZE) {
						log.debug("WarmUpManager sleep " + tPool.getWorkerNum());
						Thread.sleep(10000);
						continue;
					}
					
					ConcurrentHashMap<String, AgentSquads> fruits = ShareData.httpFruits;
					AgentSquads as = fruits.get(ConfigConstants.URL_DianPing);
					if (as != null)
						as.clearAgents();
					List<Agent> agents = agentDao.getOriAgents();
					for (Agent agent : agents){
						log.debug("warm up task" + tPool.getWorkerNum());
						tPool.execute(createTask(agent,ConfigConstants.URL_DianPing, ConfigConstants.Protocol_Http));
					}
					// Thread.sleep(ConfigConstants.sleepTime);
//					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					log.error(ExceptionUtil.getExceptionString(e));
				}
			}
			new WorkThread().start();// 某个线程异常退出后，立马启动一个
		}
	}
	
	
	
	private static Runnable createTask(final Agent agent, final String url, final String protocol) {
		return new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				log.debug("warm up in");
				if (agent.testAgent(url, 1) > 0) {
					log.debug("warm up yes");
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
//						if (as.size() > ConfigConstants.MaxAgentsNumRequestOnce * 3)
//							as.delete();
						as.addAgent(agent);
					}
				}
			}
		};
	}
}
