package com.tigerknows.proxy.http;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

public class HttpConnectionManager {

	private static HttpParams httpParams;
	private static PoolingClientConnectionManager cm;

	/**
	 * 最大连接数
	 */
	public final static int MAX_TOTAL_CONNECTIONS = 6000;
	/**
	 * 每个路由最大连接数
	 */
	public final static int MAX_ROUTE_CONNECTIONS = 2000;
	/**
	 * 连接超时时间
	 */
	public final static int CONNECT_TIMEOUT = 5000;
	/**
	 * 读取超时时间
	 */
	public final static int READ_TIMEOUT = 5000;

	static {
		httpParams = new BasicHttpParams();
		// 连接请求超时
		httpParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
				CONNECT_TIMEOUT);
		// 数据读取超时
		httpParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, READ_TIMEOUT);
		// 编码
		httpParams.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET,
				"UTF-8");
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory
				.getSocketFactory()));
		schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory
				.getSocketFactory()));
		cm = new PoolingClientConnectionManager(schemeRegistry);
		// Increase max total connection to 200
		cm.setMaxTotal(MAX_TOTAL_CONNECTIONS);
		// Increase default max connection per route to 20
		cm.setDefaultMaxPerRoute(MAX_ROUTE_CONNECTIONS);
	}

	public static DefaultHttpClient getHttpClient() {
		DefaultHttpClient httpClient = new DefaultHttpClient(cm);
		httpClient.setParams(httpParams);
		return httpClient;
	}

	public static void shutdown() {
		cm.shutdown();
	}

	public static PoolingClientConnectionManager getConnectionManager() {
		return cm;
	}

}