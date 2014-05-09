package com.tigerknows.proxy.model;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tigerknows.proxy.concurrency.ShareData;
import com.tigerknows.proxy.concurrency.ThreadPool;
import com.tigerknows.proxy.config.ConfigConstants;
import com.tigerknows.proxy.domain.Agent;
import com.tigerknows.proxy.domain.AgentSquads;

public class UrlAgentManager {

	private Log log = LogFactory.getLog(UrlAgentManager.class);

	private ThreadPool tPool;

	public UrlAgentManager() {
		this.tPool = new ThreadPool("UrlAgentManager", 2, 32);
	}

	public void work() {
		new WorkThread().start();
		try {
			Thread.sleep(1000);
			log.info("UrlAgentManager start to work");
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
					log.debug("UrlAgentManaget work");
					Agent agent = ShareData.agentQueue.take();
					ConcurrentHashMap<String, AgentSquads> fruits = null;
					if (ConfigConstants.Protocol_socks5.equals(agent
							.getProtocol()))
						fruits = ShareData.sockFruits;
					else
						fruits = ShareData.httpFruits;

					Set<String> urls = fruits.keySet();
					for (String url : urls) {
						log.debug("UrlAgentManaget work " + url);
						tPool.execute(createTask(agent, url, fruits));
					}
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			new WorkThread().start();// 某个线程异常中断后，立马启动一个
		}
	}

	private static Runnable createTask(final Agent agent, final String url,
			final ConcurrentHashMap<String, AgentSquads> fruits) {
		return new Runnable() {

			public void run() {
				if (agent.testAgent(url, 1) > 0) {
					AgentSquads as = fruits.get(url);
					if (as != null){
						if (as.size() > ConfigConstants.MaxAgentsNumRequestOnce * 3)
							as.delete();
						as.addAgent(agent);
					}
					else {
						as = new AgentSquads();
						as.addAgent(agent);
						fruits.put(url, as);
					}
				}
			}
		};
	}

	public static Agent getAngetQuickly(String url, String protocol) {
		
		Agent agent = null;
		ConcurrentHashMap<String, Agent> liveAgents = null;
		if (ConfigConstants.Protocol_socks5.equals(protocol))
			liveAgents = ShareData.sock5LiveAgents;
		else
			liveAgents = ShareData.httpLiveAgents;
		Object[] keys = liveAgents.keySet().toArray();
//		Set<String> toBeKickOut = new HashSet<String>();
		int i = 0;
		Random r = new Random();
//		System.err.println("keys.length " + keys.length);
//		for (String key : keys) {
		while(i++ < keys.length){
			int flag = r.nextInt(keys.length);
//			System.err.println("flag"  + flag);
			String key = (String)keys[flag];
			agent = liveAgents.get(key);
					
			if (agent.testAgent(url, 1) > 0)
				break;
			else{
//				toBeKickOut.add(agent.getId());
				DBManager.killAgent(agent);
			}
		}
		
//		for (String key : toBeKickOut)
//			liveAgents.remove(key);//有点缺陷
			
		return agent;
	}
}
