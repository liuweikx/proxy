package com.tigerknows.proxy.web.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ServiceFactory {

	private static Log log = LogFactory.getLog(ServiceFactory.class);

	private static Map<String, Service> services = new HashMap<String, Service>();

	public void setServices(Map<String, Service> servicesMap) {
		services = servicesMap;
	}
	
	public static void init(){
		services.put("p_1", new ProxyServiceV1());
	}

	public static void addService(String key, Service serv) {
		if (services.containsKey(key))
			throw new IllegalArgumentException();
		services.put(key, serv);
	}

	public static Service getService(Map args) {
		String at = (String) args.get(CommonRequestParams.API_TYPE);
		String version = (String) args.get(CommonRequestParams.VERSION);
		if (at == null || version == null)
			throw new IllegalArgumentException("Exception : missing at or v");
		Service serv = services.get(at + "_" + version);
		if (serv == null) {
			throw new IllegalArgumentException("无对应的service：" + at + "_"
					+ version);
		}
		return serv;
	}
	

}
