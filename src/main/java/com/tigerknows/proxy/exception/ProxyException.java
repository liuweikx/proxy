package com.tigerknows.proxy.exception;

public class ProxyException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String type;
	private String Message;

	public ProxyException(String type, String Message) {
		this.type = type;
		this.Message = Message;
	}

	public String getType(){
		return this.type;
	}
	
	public String getMessage(){
		return this.Message;
	}
}
