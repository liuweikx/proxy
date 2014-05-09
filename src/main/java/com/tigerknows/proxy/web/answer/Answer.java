package com.tigerknows.proxy.web.answer;

import java.io.IOException;
import java.io.OutputStream;


public interface Answer {
	/**
	 * 
	 * for mobile client
	 * 
	 * @param os
	 * @param charset
	 * @throws IOException
	 */
    void writeTo(OutputStream os, String charset) throws IOException;
    
    /**
     * 
     * for browser client
     * 
     * @param os
     * @param charset
     * @throws IOException
     */
    void writeToBrowser(OutputStream os, String charset) throws IOException;
    
    boolean isEmpty();
    
    String getLogInfo();
    
    String toJSONString();
    
    String getStringResut();
    
}

