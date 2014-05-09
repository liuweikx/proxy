package com.tigerknows.proxy.tool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.sql.DataSource;

import com.tigerknows.proxy.config.ConfigConstants;
import com.tigerknows.proxy.dao.AgentDao;
import com.tigerknows.proxy.dao.DataSources;
import com.tigerknows.proxy.domain.Agent;

public class ParseAgents2DB {

	public static void main(String args[]) {

		if (args == null || args.length < 2 || args[0].trim().length() <= 0 || args[1].trim().length() <= 0) {
			System.out.println("need Parameter of action or protocol");
			return;
		}

		DataSource ds = DataSources.agent();
		AgentDao agentDao = new AgentDao(ds);

		// 如果只是计数决定是否需要更新代理源，现在只管http
		String action = args[0];
		String protocol = args[1];
		if ("count".equals(action)) {

			int num = agentDao.countTable("agents",
					" where protocol = '" + protocol +"' and state = 1");
			if (ConfigConstants.Protocol_Http.equals(protocol) && num < 2000)
				System.out.println("http");
			return;
		}

		try {
			 BufferedReader reader = new BufferedReader(new InputStreamReader(
			 System.in));
//			BufferedReader reader = new BufferedReader(new FileReader(
//					"/home/hepan/agent"));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.trim().length() == 0 || line.trim().charAt(0) == '#')
					continue;
				String[] items = line.split(":");
				Agent agent = null;
				if (items.length == 2)
					agent = new Agent(items[0].trim(),
							Integer.parseInt(items[1].trim()),
							ConfigConstants.Agent_State_Live, protocol, "", "");
				else if (items.length == 4)
					agent = new Agent(items[0].trim(),
							Integer.parseInt(items[1].trim()),
							ConfigConstants.Agent_State_Live, protocol, "", "",
							items[2].trim(), items[3].trim());
				if (agent != null) {
					agentDao.deleteAgent(agent);
					agentDao.insertAgent(agent);
				}
			}
			agentDao.deleteDeadAgent();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
