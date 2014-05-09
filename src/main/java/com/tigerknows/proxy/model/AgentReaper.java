package com.tigerknows.proxy.model;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tigerknows.proxy.concurrency.ShareData;
import com.tigerknows.proxy.config.ConfigConstants;
import com.tigerknows.proxy.domain.Agent;
import com.tigerknows.proxy.domain.AgentSquads;

public class AgentReaper {
	
		private static Log log = LogFactory.getLog(AgentReaper.class);
		
		public static Agent reap(String sUrl, String protocol){
			ConcurrentHashMap<String, AgentSquads> fruits = null;
			if (ConfigConstants.Protocol_socks5.equals(protocol))
				fruits = ShareData.sockFruits;
			else
				fruits = ShareData.httpFruits;

			Agent agent = null;
			AgentSquads as = null;
			
			if (sUrl == null){
				if (ConfigConstants.Protocol_socks5.equals(protocol))
					sUrl = ConfigConstants.URL_SOCK5_FAKE;
				else
					sUrl = ConfigConstants.URL_TEST_SPEED;
			}

			as = fruits.get(sUrl);

			if (as != null) {
				agent = as.getQuickAgent();
			}
			return agent;
		}
}
