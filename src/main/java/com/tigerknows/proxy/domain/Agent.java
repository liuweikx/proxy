package com.tigerknows.proxy.domain;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;

import com.tigerknows.proxy.config.ConfigConstants;
import com.tigerknows.proxy.exception.ExceptionUtil;
import com.tigerknows.proxy.http.HttpConnectionManager;

public class Agent {

	private Log log = LogFactory.getLog(Agent.class);

	private String IP;

	private int port;

	private int state;

	private String protocol;

	private String city;

	private String country;

	private long lastLiveTime;

	private double speed = 0.0; // 按字节算

	private String username;

	private String password;

	public Agent(String IP, int port, int state, String protocol, String city,
			String country) {
		this.IP = IP;
		this.port = port;
		this.state = state;
		this.protocol = protocol;
		this.city = city;
		this.country = country;
		this.lastLiveTime = System.currentTimeMillis() / 1000;
	}

	public Agent(String IP, int port, int state, String protocol, String city,
			String country, String username, String password) {
		this.IP = IP;
		this.port = port;
		this.state = state;
		this.protocol = protocol;
		this.city = city;
		this.country = country;
		this.username = username;
		this.password = password;
		this.lastLiveTime = System.currentTimeMillis() / 1000;
	}

	public String getIP() {
		return IP;
	}

	public int getState() {
		return state;
	}

	public int getPort() {
		return port;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getCity() {
		return city;
	}

	public String getCountry() {
		return country;
	}

	public String getUserName() {
		return username;
	}

	public String getPassWord() {
		return password;
	}

	public long getLastLiveTime() {
		return lastLiveTime;
	}

	public void setLastLiveTime(long time) {
		this.lastLiveTime = time;
	}

	public double getSpeed() {
		return this.speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public String getId() {
		return this.getIP() + ":" + this.getPort();
	}

	public double testAgent2(String sUrl, int n) {
		for (int i = 0; i < n; i++) {
			Proxy proxy = null;
			int bufferSize = 500 * 1024;
			if (ConfigConstants.Protocol_socks5.equals(this.protocol)) {
				if (ConfigConstants.URL_SOCK5_FAKE.equals(sUrl))
					return 20.0;
				// 处理sock5的请求，用socket的请求来做
				Sock5Factory s5f = new Sock5Factory(this);
				try {
					Socket s = s5f.createSocket(sUrl, 80);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// 诡异问题，先如此处理
				return 20.0;
				// try {
				// InputStream is = s.getInputStream();
				// OutputStream out = s.getOutputStream();
				// StringBuffer sb = new StringBuffer("GET HTTP/1.1\r\n");
				// sb.append("User-Agent: Java/1.6.0_20\r\n");
				// sb.append("Host: " + sUrl + ":80\r\n");
				//
//				sb.append("Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2\r\n");
				// sb.append("Connection: Close\r\n");
				// sb.append("\r\n");
				// long start = System.currentTimeMillis();
				// out.write(sb.toString().getBytes());
				// int readed = 0;
				// int temp = 0;
				// byte[] buffer = new byte[bufferSize];
				// while (readed < bufferSize && (temp = is.read(buffer)) !=
				// -1) {
				// readed += temp;
				// }
				// long end = System.currentTimeMillis();
				//
				// this.speed = (this.speed * n + (int) (readed * 1000 * 1.0
				// / (end - start)))
				// / (n + 1);
				// out.close();
				// is.close();
				// s.close();
				//
				// } catch (UnknownHostException e) {
				// e.printStackTrace();
				// } catch (IOException e) {
				// e.printStackTrace();
				// }

			} else {// 处理http协议的请求
				SocketAddress addr = new InetSocketAddress(this.getIP(),
						this.getPort());
				proxy = new Proxy(Proxy.Type.HTTP, addr);
				URL url = null;
				HttpURLConnection httpUrlConnection = null;
				InputStream is = null;
				try {
					if (sUrl == null)
						url = new URL(ConfigConstants.URL_TEST_SPEED);
					else
						url = new URL(sUrl);
					long start = System.currentTimeMillis();
					URLConnection conn = url.openConnection(proxy);
					httpUrlConnection = (HttpURLConnection) conn;
					httpUrlConnection.setConnectTimeout(2000);// 2秒超时
					httpUrlConnection.setReadTimeout(2000);
					// httpUrlConnection.setDoInput(true);
					httpUrlConnection.connect();
					is = httpUrlConnection.getInputStream();
					int readed = 0;
					int temp = 0;
					byte[] buffer = new byte[bufferSize];
					while (readed < bufferSize
							&& (temp = is.read(buffer)) != -1) {
						readed += temp;
						//
					}
					long end = System.currentTimeMillis();

					this.speed = (this.speed * n + (int) (readed * 1000 * 1.0 / (end - start)))
							/ (n + 1);
					// is.close();
					// httpUrlConnection.disconnect();
				} catch (Exception e) {
					// e.printStackTrace();
					log.debug(ExceptionUtil.getExceptionString(e));
				} finally {
					if (is != null)
						try {
							is.close();
						} catch (IOException e1) {
							log.debug(ExceptionUtil.getExceptionString(e1));
						}
					if (httpUrlConnection != null)
						httpUrlConnection.disconnect();
				}
			}
		}

		return this.speed;
	}

	public double testAgent(String sUrl, int n) {
		for (int i = 0; i < n; i++) {
			int readed = 0;
			if (ConfigConstants.Protocol_socks5.equals(this.protocol)) {
				Proxy proxy = null;
				if (ConfigConstants.URL_SOCK5_FAKE.equals(sUrl))
					return 20.0;
				// 处理sock5的请求，用socket的请求来做
				Sock5Factory s5f = new Sock5Factory(this);
				try {
					Socket s = s5f.createSocket(sUrl, 80);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// 诡异问题，先如此处理
				return 20.0;
				// try {
				// InputStream is = s.getInputStream();
				// OutputStream out = s.getOutputStream();
				// StringBuffer sb = new StringBuffer("GET HTTP/1.1\r\n");
				// sb.append("User-Agent: Java/1.6.0_20\r\n");
				// sb.append("Host: " + sUrl + ":80\r\n");
				// sb.append("Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2\r\n");
				// sb.append("Connection: Close\r\n");
				// sb.append("\r\n");
				// long start = System.currentTimeMillis();
				// out.write(sb.toString().getBytes());
				// int readed = 0;
				// int temp = 0;
				// byte[] buffer = new byte[bufferSize];
				// while (readed < bufferSize && (temp = is.read(buffer)) !=
				// -1) {
				// readed += temp;
				// }
				// long end = System.currentTimeMillis();
				//
				// this.speed = (this.speed * n + (int) (readed * 1000 * 1.0
				// / (end - start)))
				// / (n + 1);
				// out.close();
				// is.close();
				// s.close();
				//
				// } catch (UnknownHostException e) {
				// e.printStackTrace();
				// } catch (IOException e) {
				// e.printStackTrace();
				// }

			} else {
				HttpClient httpclient = HttpConnectionManager.getHttpClient();
				HttpGet httpget = new HttpGet(sUrl);
				HttpHost httpProxy = new HttpHost(this.getIP(), this.getPort());
				httpclient.getParams().setParameter(
						ConnRoutePNames.DEFAULT_PROXY, httpProxy);
				HttpResponse response;
				InputStream is = null;
				try {
					long start = System.currentTimeMillis();
					response = httpclient.execute(httpget);
					long end = System.currentTimeMillis();
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						HttpEntity entity = response.getEntity();
						is = entity.getContent();
						byte[] buffer = new byte[ConfigConstants.Buffsize];
						int flag = 0;
						while (readed < ConfigConstants.Buffsize && (flag = is.read(buffer, readed, readed + 20480)) != -1)
							readed += flag;
//						log.debug("readed" + readed);
						this.speed = (this.speed * (i + 1) + (int) (readed * 1000 * 1.0 / (end - start)))
								/ (i + 2);
					}
				} catch (ClientProtocolException e) {
					log.debug(ExceptionUtil.getExceptionString(e) + " readed " + readed );
				} catch (IOException e) {
					log.debug(ExceptionUtil.getExceptionString(e) + " readed " + readed );
				}finally{
					if (is != null)
						try {
							is.close();
						} catch (IOException e) {
							log.debug(ExceptionUtil.getExceptionString(e));
						}
					httpget.releaseConnection();
				}
			}
		}

		return this.speed;
	}

	public Agent clone() {
		return new Agent(this.getIP(), this.getPort(), this.getState(),
				this.getProtocol(), this.getCity(), this.getCountry(),
				this.getUserName(), this.getPassWord());
	}

	public String toResultString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getIP()).append(":").append(this.getPort());
		if (this.username != null && this.username.length() > 0)
			sb.append(":").append(this.username);
		if (this.password != null && this.password.length() > 0)
			sb.append(":").append(this.password);
		return sb.toString();
	}
}
