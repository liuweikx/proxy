package com.tigerknows.proxy.web.answer;

import java.io.IOException;
import java.io.OutputStream;

import com.tigerknows.proxy.web.io.ByteWriter;


public class StringAnswer implements Answer {

	
	private String result;
	
	public StringAnswer(String result){
		this.result = result;
	}
	
	
	public void writeTo(OutputStream os, String charset) throws IOException {
		writeBinary(new ByteWriter(charset, os));
	}
	
	protected void writeBinary(ByteWriter writer) throws IOException
	{		
		writer.writeString(this.result);
	}
	


	public void writeToBrowser(OutputStream os, String charset)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getLogInfo() {
		// TODO Auto-generated method stub
		return this.result;
	}
	
	public String getStringResut(){
		return this.result;
	}

	
	public static void main(String[] argv)
	{
		StringAnswer la = new StringAnswer("");
		try {
			la.writeTo(System.out, "UTF8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return;
		
	}
	public String toJSONString() {
		// TODO Auto-generated method stub
		return null;
	}
}
