package com.tigerknows.proxy.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import com.tigerknows.proxy.domain.Agent;

public class AgentDao {
	
	private JdbcTemplate jdbc;
	
	private static final String SQL_QUERY_AGENGTS = "select * from agents where state != 0";
	
	private static final String SQL_UPDATE_AGENGTS_DEAD = "update agents set state = 0 where ip=";
	
	private static final String SQL_DELETE_AGENGTS = "delete from agents where ip=";
	
	private static final String SQL_DELETE_DEADAGENGTS = "delete from agents where state=0";
	
	private static final String SQL_INSERT_AGENTS_HEAD = "insert into agents (ip, port, state, protocol, city, country, username, password) values(";
	
	public AgentDao(DataSource ds){
		this.jdbc = new JdbcTemplate(ds);
	}
	
	public int countTable(String tablename, String contion) {
		int count = this.jdbc.queryForInt("select count(*) from " + tablename + " " + contion);
		return count;
	}
	
	@SuppressWarnings("unchecked")
	public List<Agent> getOriAgents(){
		return this.jdbc.query(SQL_QUERY_AGENGTS, new AgentMapper());
	}
	
	public void killAgent(Agent agent){
		StringBuffer sb = new StringBuffer(SQL_UPDATE_AGENGTS_DEAD).append("'").append(agent.getIP())
				.append("'").append(" and port = ").append(agent.getPort());
		this.jdbc.update(sb.toString());
	}
	
	public void deleteAgent(Agent agent){
		StringBuffer sb = new StringBuffer(SQL_DELETE_AGENGTS).append("'").append(agent.getIP())
				.append("'").append(" and port = ").append(agent.getPort());
		this.jdbc.update(sb.toString());
	}
	
	public void deleteDeadAgent(){
		this.jdbc.execute(SQL_DELETE_DEADAGENGTS);
	}
	
	public void insertAgent(Agent agent){
		StringBuffer sb = new StringBuffer(SQL_INSERT_AGENTS_HEAD).append("'").append(agent.getIP()).append("',").append(agent.getPort())
				.append(",").append(agent.getState()).append(",'").append(agent.getProtocol()).append("'");
		if (agent.getCity() != null){
			sb.append(",'").append(agent.getCity()).append("'");
		}else{
			sb.append(",''");
		}
		if (agent.getCountry() != null && agent.getCountry().length() > 0){
			sb.append(",'").append(agent.getCountry()).append("'");
		}else{
			sb.append(",''");
		}
		
		if (agent.getUserName() != null && agent.getUserName().length() > 0){
			sb.append(",'").append(agent.getUserName()).append("'");
		}else{
			sb.append(",''");
		}
		if (agent.getPassWord() != null && agent.getPassWord().length() > 0){
			sb.append(",'").append(agent.getPassWord()).append("'");
		}else{
			sb.append(",''");
		}
		sb.append(")");
		this.jdbc.execute(sb.toString());
	}
	
	private static class AgentMapper implements ParameterizedRowMapper<Agent> {
		public Agent mapRow(ResultSet rs, int rowNum) throws SQLException {
			String IP = rs.getString("IP");
			int port = rs.getInt("port");
			int state = rs.getInt("state");
			String protocol = rs.getString("protocol");
			String city = rs.getString("city");
			String country = rs.getString("country");
			String username = rs.getString("username");
			String password = rs.getString("password");
			return new Agent(IP, port, state, protocol, city, country,username,password);
		}
	}
}
