package com.tigerknows.proxy.web.servlet;



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Set;

import javax.security.auth.callback.TextOutputCallback;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tigerknows.proxy.util.StringUtil;
import com.tigerknows.proxy.web.answer.Answer;
import com.tigerknows.proxy.web.service.Service;
import com.tigerknows.proxy.web.service.ServiceFactory;

public class ServiceServlet extends HttpServlet {

	private static Log log = LogFactory.getLog(ServiceServlet.class);
	private static final long serialVersionUID = 1234L;

	/**
	 * Servlet初始化，继承自父类。由容器进行调用。
	 */
	public void init() throws ServletException {
		log.info("proxy init: ServingServlet.init() :start");
		super.init();
		ServiceFactory.init();
		log.info("proxy init: ServingServlet.init() :  end");

	}

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws IOException {
		try {
			doService(req, res);
		} catch (Exception e) {
			log.error("servlet " + e.getStackTrace().length, e);
		}
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws IOException {
		try {
			doService(req, res);
		} catch (Exception e) {
			log.error("servlet " + e.getStackTrace().length, e);
		}
	}

	private void doService(HttpServletRequest req, HttpServletResponse res)
			throws Exception {
		long start = System.currentTimeMillis();
		@SuppressWarnings("unchecked")
		Map<String, String> params = (Map<String, String>) req
				.getAttribute(ServletConstants.ATTRIBUTE_KEY_PARAMS);
		if (!params.containsKey("at")){
			params.put("at", "p");
			params.put("v", "1");
		}
		try {
			// unique data version
			// 根据参数中请求类型获取对应的Service进行处理。
			Service svc = ServiceFactory.getService(params);
			Answer answer = svc.getAnswer(params);
			if (answer == null)
				throw new Exception("Exception : null answer");
//			if (null != params.get(ServletConstants.ATTRIBUTE_KEY_ENCRYPTED)) {
//				DataEncryptor encryptor;
//				if(ServletConstants.ENCRYPTED_VALUE.equals(params.get(ServletConstants.ATTRIBUTE_KEY_ENCRYPTED))
//						|| ServletConstants.COMPRESS_ENCRYPTED_VALUE.equals(params.get(ServletConstants.ATTRIBUTE_KEY_ENCRYPTED)))
//					 encryptor = new DataEncryptor(true);
//				else
//					encryptor = new DataEncryptor();
//				answer = new EncryptedAnswer(answer, encryptor);
//			}

			// to write to response
			writeAnswer(params.get(ServletConstants.BROWSER_CLIENT), answer, res,
					params.get(ServletConstants.STR_ENCODING));
			long time = System.currentTimeMillis() - start;
			log.info(generateLog(req, time, params, answer));
		} catch (Exception e) {
			String loginfo = generateLog(req, System.currentTimeMillis()
					- start, params, null);
			log.warn(loginfo, e);
		} finally {
			res.getOutputStream().close();
		}

	}

	public static String generateLog(HttpServletRequest request, long time,
			Map<String, String> params, Answer answer) {
		StringBuilder loginfo = new StringBuilder("[");
		loginfo.append(request.getRemoteAddr());
		String originalIP = request.getHeader("X-Forwarded-For");
		if (originalIP != null) {
			loginfo.append(", ").append(originalIP);
		}
		loginfo.append("] ").append(time).append("ms ")
				.append(params.get(ServletConstants.STRING_PARAM_FOR_LOG));
		if (params.get("modelName") != null)
			loginfo.append("{modelName=").append(params.get("modelName"))
					.append("}");
		if (answer != null) {
			String anInfo = answer.getLogInfo();
			if (anInfo != null)
				loginfo.append(" [").append(anInfo).append("]");
			if (answer.isEmpty())
				loginfo.append(" Empty!");
		} else {
			loginfo.append(" Exception!");
		}
		return loginfo.toString();
	}

	public static void writeAnswer(String browserClient, Answer answer,
			ServletResponse response, String encoding) throws IOException {
		if (encoding == null) {
			encoding = StringUtil.getDefaultCharset();
		}
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		
//		if (browserClient == null) {
//			answer.writeTo(bos, encoding);
//		} else {
//			answer.writeToBrowser(bos, encoding);
//		}
		
//		int contentLen = bos.size();
		byte[] result = answer.getStringResut().getBytes("UTF-8");
		int contentLen = result.length;
		if (response.getBufferSize() < contentLen)
			response.setBufferSize(contentLen);
		response.setContentLength(contentLen);
		response.setContentType("text/html; charset=utf-8");
		response.getOutputStream().write(answer.getStringResut().getBytes("UTF-8"));
//		bos.writeTo(response.getOutputStream());
//		bos.close();
	}

}
