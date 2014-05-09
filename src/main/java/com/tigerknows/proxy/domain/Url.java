package com.tigerknows.proxy.domain;

public class Url {
	private String url;
	private long lastRequestTime;
	
	public Url(String url, long time){
		this.url = url;
		this.lastRequestTime = time;
	}
	
	public String getUrl(){
		return this.url;
	}
	
	public long getLastRequestTime(){
		return this.lastRequestTime;
	}
	
	public void setLastRequestTime(long time){
		this.lastRequestTime = time;
	}
}
