package com.tigerknows.proxy.web.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tigerknows.proxy.concurrency.ShareData;
import com.tigerknows.proxy.config.ConfigConstants;
import com.tigerknows.proxy.dao.DataSources;
import com.tigerknows.proxy.domain.Agent;
import com.tigerknows.proxy.domain.AgentSquads;
import com.tigerknows.proxy.domain.Url;
import com.tigerknows.proxy.exception.ExceptionUtil;
import com.tigerknows.proxy.model.AgentReaper;
import com.tigerknows.proxy.model.DBManager;
import com.tigerknows.proxy.model.UrlAgentManager;
import com.tigerknows.proxy.model.UrlManager;
import com.tigerknows.proxy.model.WarmUpManager;
import com.tigerknows.proxy.web.answer.Answer;
import com.tigerknows.proxy.web.answer.StringAnswer;

public class ProxyServiceV1 implements Service {

	private static Log log = LogFactory.getLog(ProxyServiceV1.class);

	private DBManager dBManager;

	private UrlAgentManager urlAgentManager;

	private UrlManager urlManager;
	
	private WarmUpManager warmUpManger;

	public static int getVersion() {
		return 1;
	}

	public static String getAt() {
		return "p";
	}

	public void init() {

	}

	public ProxyServiceV1() {
		log.info("Proxy service start to init");
		DataSource ds = DataSources.agent();
		dBManager = new DBManager(ds);
		dBManager.work();
		warmUpManger = new WarmUpManager(ds);
		warmUpManger.work();
		urlAgentManager = new UrlAgentManager();
		urlAgentManager.work();
		urlManager = new UrlManager();
		urlManager.work();
		log.info("Proxy service start to init done");
	}

	public void registerSelf() {
		ServiceFactory.addService(getAt() + "_" + getVersion(), this);
	}

	public Answer getAnswer(Map<String, String> map) throws Exception {
		
		String sUrl = map.get(ConfigConstants.String_Url);
		String protocol = map.get(ConfigConstants.String_Portocol);
		int num = 1;
		String sNum = map.get(ConfigConstants.String_Num);
		
		if (sNum != null && sNum.length() > 0){
			try{
				num = Integer.parseInt(sNum);
			}catch (Exception e){
				log.error(ExceptionUtil.getExceptionString(e));
			}
			if (num > ConfigConstants.MaxAgentsNumRequestOnce)
				num = ConfigConstants.MaxAgentsNumRequestOnce;//暂时限制最高
		}
		int counter = 0;
		Set<String>  results = new HashSet<String>();
		while(results.size() < num && counter ++ < num * 5){//默认最多尝试5倍请求数目的次数
			Agent agent = AgentReaper.reap(sUrl, protocol);
			if (agent != null)
				results.add(agent.toResultString());
			else{
					agent = UrlAgentManager.getAngetQuickly(sUrl, protocol);
					if (agent != null) {
						ConcurrentHashMap<String, AgentSquads> fruits = null;
						if (ConfigConstants.Protocol_socks5.equals(protocol))
							fruits = ShareData.sockFruits;
						else
							fruits = ShareData.httpFruits;
						AgentSquads as = fruits.get(sUrl);

						if (as == null){
							as = new AgentSquads();
							as.addAgent(agent);
							fruits.put(sUrl, as);
						}else{
							as.addAgent(agent);
						}
					}
					break;
			}
			
		}
		
		Url url = ShareData.urls.get(sUrl);
		if (url != null)
			url.setLastRequestTime(System.currentTimeMillis());
		else {
			url = new Url(sUrl, System.currentTimeMillis());
			ShareData.urls.put(sUrl, url);

		}
		StringBuffer result = new StringBuffer("");
		counter = 1;
		for (String s: results){
			result.append(s);
			if (counter++ < results.size())
				result.append("#");
		}
		Answer an = new StringAnswer(result.toString());
		return an;
	}

	public static void main(String args[]) {
		Map<String, String> mp = new HashMap<String, String>();
		mp.put("url", "http://www.dianping.com");
		mp.put("protocol", "http");
		ProxyServiceV1 ps1 = new ProxyServiceV1();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		while (true) {
			try {
				StringAnswer sa = (StringAnswer) ps1.getAnswer(mp);
				System.out.println(sa.getStringResut());

				sa = (StringAnswer) ps1.getAnswer(mp);
				System.out.println(sa.getStringResut());
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
