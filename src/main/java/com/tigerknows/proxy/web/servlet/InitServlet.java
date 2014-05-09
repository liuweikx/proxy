package com.tigerknows.proxy.web.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tigerknows.proxy.web.service.ProxyServiceV1;


public class InitServlet extends HttpServlet {

	private static Log log = LogFactory.getLog(InitServlet.class);
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws IOException {
		doService(req, res);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws IOException {
		doService(req, res);
	}

	private void doService(HttpServletRequest req, HttpServletResponse res) {
		
	}

}
