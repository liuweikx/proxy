package com.tigerknows.proxy.concurrency;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.tigerknows.proxy.domain.Agent;
import com.tigerknows.proxy.domain.AgentSquads;
import com.tigerknows.proxy.domain.Url;

public class ShareData {

	public static LinkedBlockingQueue<Agent> agentQueue = new LinkedBlockingQueue<Agent>(); //被URL-AgentManager观察，有值则生成任务
	
	public static ConcurrentHashMap<String, Agent> httpLiveAgents = new ConcurrentHashMap<String, Agent>(); //用于保存个周期内预期可用的agent，所谓预期可用是指周期内确认过一次agent可用
	
	public static ConcurrentHashMap<String, Agent> sock5LiveAgents = new ConcurrentHashMap<String, Agent>(); //用于保存个周期内预期可用的agent，所谓预期可用是指周期内确认过一次agent可用
	
	public static ConcurrentHashMap<String, AgentSquads> httpFruits = new ConcurrentHashMap<String, AgentSquads>(); //对应每个url的有效agent
	
	public static ConcurrentHashMap<String, AgentSquads> sockFruits = new ConcurrentHashMap<String, AgentSquads>(); //对应每个url的有效agent
	
	public static ConcurrentHashMap<String, Url> urls = new ConcurrentHashMap<String, Url>();  //保存访问过的url
}
