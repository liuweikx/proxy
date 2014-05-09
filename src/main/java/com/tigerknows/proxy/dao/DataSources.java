package com.tigerknows.proxy.dao;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.dbcp.BasicDataSource;

public class DataSources {
//	private static Log log = LogFactory.getLog(DataSources.class);
	private static final String configFileName = "/opt/AgentData/jdbc.properties";

	private static DataSource agent;

	/**
	 * 单独拿出来，因为要不停服务就要更换MySql的，其它地方(RTInfoWrapperProvider)需要该方法
	 */

	static {
		PropertiesConfiguration config = new PropertiesConfiguration();
		try {
			config.load(configFileName);
		} catch (ConfigurationException e) {
			config = null;
		}


		String agentHost = getFromProperties(config, "agent.host", "192.168.11.174:3306/hack");
		String agentUsername = getFromProperties(config, "agent.username", "mysql");
		String agentPassword = getFromProperties(config, "agent.password", "titps4gg");

		
//		log.trace("agent:host=" + agentHost + ", username=" + agentUsername + ", password=" + agentPassword);
		agent = mysql(agentHost, agentUsername,agentPassword);
	}

	private static String getFromProperties(PropertiesConfiguration config, String key, String defValue) {
		if (config == null)
			return defValue;
		String value = config.getString(key);
		if (value == null)
			value = defValue;
		return value;
	}

	private DataSources() {
	}

	

	public static DataSource agent() {
		return agent;
	}
	

	public static DataSource mysql(String host, String username, String password) {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://" + host + "?characterEncoding=UTF-8"
				+ "&autoReconnect=true&autoReconnectForPools=true" + "&zeroDateTimeBehavior=convertToNull");
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		return dataSource;
	}

	public static void main(String[] args) {
		DataSource source = agent();
		try {
			source.getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
