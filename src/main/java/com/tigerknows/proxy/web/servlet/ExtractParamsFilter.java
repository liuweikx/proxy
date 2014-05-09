/**
 * 
 */
package com.tigerknows.proxy.web.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExtractParamsFilter implements Filter {
	private static Log log = LogFactory.getLog(ExtractParamsFilter.class);
	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {

	}

//	private byte[] decryptParams(DataEncryptor encryptor, ServletRequest request, ServletResponse response) 
//			throws IOException, ServletException {
//		InputStream in = request.getInputStream();
//		int len = request.getContentLength();
//		if(len == 0)
//			return null;
//		byte[] data = new byte[len];
//		if ((len != in.read(data,0,len))) {
//			return null;
//		}
//		else {
//			encryptor.decrypt(data);
//			return data;
//		}
//	}
	
	private Map<String, String> getRealParams(byte[] data, ServletRequest request) throws IOException {
		if(data == null)
			return null;
		BufferedReader reader = new BufferedReader(new StringReader(new String(data, "utf8")));
		String line = null;
		//解析请求参数		
		line = reader.readLine();
		String[] sa = line.split("&");
		Map<String, String> params = new HashMap<String, String>();
		Map<String, String> inverted = new HashMap<String, String>();
		for (int i = 0; i < sa.length; ++ i) {
			String s = sa[i];
			int sepPos = s.indexOf('=');
			if (-1 != sepPos) { // 正常情况，本行有 '=' 
				String key = s.substring(0, sepPos);
				String value = s.substring(sepPos + 1);
				params.put(key, value);
				inverted.put(value, key);
			} 
			else if(i > 0) { // 本行没有'='，去之前找'='
				String preS = sa[i - 1];
				int preSepPos = preS.indexOf('=');
				
				String preLinevalue;
				if (-1 != preSepPos) // 前一行有'='，其后半段到key的反向映射已经记下来了
					preLinevalue = preS.substring(preSepPos + 1);		
				else // 前一行也没有'='，但其到key的反向映射也是已经记下来了的
					preLinevalue = preS;

				String preKey = inverted.get(preLinevalue);
				String preValue = (String)request.getAttribute(preKey);
				String value = preValue + '_' + s;
				params.put(preKey, value);
				inverted.put(s, preKey);
			}
		}
		return params;
	}
	
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		Map<String, String> map = null;
		String serviceTypeHeaderValue = ((HttpServletRequest)request).getHeader(ServletConstants.REQ_KEY_HEADER_V13);
		if (serviceTypeHeaderValue != null) {
//			byte[] data;
//			if(serviceTypeHeaderValue.equals(ServletConstants.ENCRYPTED_VALUE) || serviceTypeHeaderValue.equals(ServletConstants.COMPRESS_ENCRYPTED_VALUE))
//				data = decryptParams(new DataEncryptor(true), request, response);
//			else
//				data = decryptParams(new DataEncryptor(), request, response);
//			if(data == null) {
//				return;
//			}
//			if(ServletConstants.COMPRESS_ENCRYPTED_VALUE.equals(serviceTypeHeaderValue))
//				data = ZLibUtils.decompress(data);
//			map = getRealParams(data, request);
//			if(map != null)
//				map.put(ServletConstants.ATTRIBUTE_KEY_ENCRYPTED, serviceTypeHeaderValue);
		} 
		else {
			map = new HashMap<String, String>();
			request.setCharacterEncoding("utf8");
			Enumeration e = request.getParameterNames();
			while(e.hasMoreElements()) {
				String name = (String)e.nextElement();
				map.put(name, request.getParameter(name));
			}
//			log.info("no " + ServletConstants.REQ_KEY_HEADER_V13 + " header");
			//return;
		}
		
		if(map == null)
			return;
		map.put(ServletConstants.STRING_PARAM_FOR_LOG, map.toString());
		request.setAttribute(ServletConstants.ATTRIBUTE_KEY_PARAMS, map);
		chain.doFilter(request, response);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig arg0) throws ServletException {

	}

}
