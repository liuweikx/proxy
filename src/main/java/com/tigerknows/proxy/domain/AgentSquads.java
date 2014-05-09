package com.tigerknows.proxy.domain;

import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tigerknows.proxy.config.ConfigConstants;
import com.tigerknows.proxy.model.WarmUpManager;

public class AgentSquads {

	private static Log log = LogFactory.getLog(AgentSquads.class);

	private LinkedBlockingQueue<Agent> highSpeed;

	private LinkedBlockingQueue<Agent> normalSpeed;

	private LinkedBlockingQueue<Agent> lowSpeed;

	private Random r;

	public AgentSquads() {

		this.highSpeed = new LinkedBlockingQueue<Agent>();

		this.normalSpeed = new LinkedBlockingQueue<Agent>();

		this.lowSpeed = new LinkedBlockingQueue<Agent>();

		this.r = new Random(ConfigConstants.Proba_Top);
	}

	private LinkedBlockingQueue<Agent> getSquads(String speedType) {

		if (ConfigConstants.Agent_Low.equals(speedType))
			return lowSpeed;
		else if (ConfigConstants.Agent_Normal.equals(speedType))
			return normalSpeed;
		else
			return highSpeed;
	}

	public synchronized Agent getAgent(String speedType) {// 既用线程安全的队列又用synchronized，是为了在while中take的时候不会堵塞

		log.debug(" before high :" + this.highSpeed.size() + " nor :"
				+ this.normalSpeed.size() + " slow :" + this.lowSpeed.size());
		LinkedBlockingQueue<Agent> squads = getSquads(speedType);
		Agent result = null;
		long secnow = System.currentTimeMillis() / 1000;

		while ((result = squads.poll()) != null) {
			if (result.getLastLiveTime() == 0
					|| secnow - result.getLastLiveTime() < ConfigConstants.Timeout * 12)
				break;
		}
		log.debug(" after high :" + this.highSpeed.size() + " nor :"
				+ this.normalSpeed.size() + " slow :" + this.lowSpeed.size());

		return result;
	}

	private Agent getOtherSpeedAgent(String speedType) {
		Agent agent = null;
		if (ConfigConstants.Agent_Low.equals(speedType)) {
			agent = getAgent(ConfigConstants.Agent_High);
			if (agent == null)
				agent = getAgent(ConfigConstants.Agent_Normal);
			return agent;
		}

		if (ConfigConstants.Agent_Normal.equals(speedType)) {
			agent = getAgent(ConfigConstants.Agent_High);
			if (agent == null)
				agent = getAgent(ConfigConstants.Agent_Low);
			return agent;
		}

		if (ConfigConstants.Agent_High.equals(speedType)) {
			agent = getAgent(ConfigConstants.Agent_Normal);
			if (agent == null)
				agent = getAgent(ConfigConstants.Agent_Low);
			return agent;
		}

		return agent;
	}

	public Agent getAgentRandomly() {
		int randomNum = r.nextInt();
		String speedType;
		if (randomNum >= ConfigConstants.Proba_Hs)
			speedType = ConfigConstants.Agent_High;
		else if (randomNum < ConfigConstants.Proba_Hs
				&& randomNum >= ConfigConstants.Proba_Ns)
			speedType = ConfigConstants.Agent_Normal;
		else
			speedType = ConfigConstants.Agent_Low;
		Agent agent = getAgent(speedType);
		// 如果随机到的队列为空，则按速度等级取一个
		if (agent == null)
			agent = getOtherSpeedAgent(speedType);
		return agent;
	}

	public synchronized void clearAgents(){
		if (this.size() > ConfigConstants.MaxAgentsNumRequestOnce * 10){
			int times = this.size() - ConfigConstants.MaxAgentsNumRequestOnce * 10;
			for (int i = 0; i < times; i++ ){
				this.delete();
				log.debug("delete " + i);
			}
		}
	}
	
	//返回最快的代理
	public Agent getQuickAgent() {
		Agent agent = getAgent(ConfigConstants.Agent_High);
		if (agent == null){
			agent = getAgent(ConfigConstants.Agent_Normal);
			if (agent == null)
				agent = getAgent(ConfigConstants.Agent_Low);
		}
		return agent;
	}

	public int size() {
		return this.highSpeed.size() + this.normalSpeed.size()
				+ this.lowSpeed.size();
	}

	public void delete() {
		try {
			if (lowSpeed.size() > 0)
				lowSpeed.take();
			else if (normalSpeed.size() > 0)
				normalSpeed.take();
			else if (highSpeed.size() > 0)
				highSpeed.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void addAgent(Agent agent) {
		String speedType = null;
		if (agent.getSpeed() >= ConfigConstants.Speed_Hs)
			speedType = ConfigConstants.Agent_High;
		else if (agent.getSpeed() < ConfigConstants.Speed_Hs
				&& agent.getSpeed() <= ConfigConstants.Speed_Ns)
			speedType = ConfigConstants.Agent_Normal;
		else
			speedType = ConfigConstants.Agent_Low;
		getSquads(speedType).add(agent);
	}
}
