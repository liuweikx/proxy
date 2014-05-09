package com.tigerknows.proxy.model;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tigerknows.proxy.concurrency.ShareData;
import com.tigerknows.proxy.config.ConfigConstants;
import com.tigerknows.proxy.domain.Url;

public class UrlManager {
	
	private Log log = LogFactory.getLog(UrlManager.class);
	public void work() {
		new WorkThread().start();
		try {
			Thread.sleep(1000);
			log.info("UrlManager start to work");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private class WorkThread extends Thread {

		public void run() {
			while (!isInterrupted()) {
				long secNow = System.currentTimeMillis() / 1000;
				Set<String> keys = ShareData.urls.keySet();
				for (String key : keys) {
					Url url = ShareData.urls.get(key);
					if (url == null)
						continue;
					if (secNow - url.getLastRequestTime() > ConfigConstants.Timeout) {
						ShareData.urls.remove(key);
						ShareData.httpFruits.remove(key);
						ShareData.sockFruits.remove(key);
						log.debug("remove info of " + url);
					}
				}
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			new WorkThread().start();// 某个线程异常退出后，立马启动一个
		}
	}

}
