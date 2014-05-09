package com.tigerknows.proxy.domain;

public class TargetURL {
	
	private String url;
	private long lastRequestTime;
	
	public TargetURL(String url, long time){
		this.url = url;
		this.lastRequestTime = time;
	}
	
	public String getUlr(){
		return this.url;
	}

	public long getLastRequestTime(){
		return this.lastRequestTime;
	}
	
	public void setLastRequestTime(long time){
		this.lastRequestTime = time;
	}
}
