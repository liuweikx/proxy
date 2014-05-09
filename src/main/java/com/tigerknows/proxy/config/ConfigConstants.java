package com.tigerknows.proxy.config;

public class ConfigConstants {

	public final static int  Timeout = 180;
	
	public final static int DB_QUNENE_MAXSIZE = 10000; //工作队列等待执行任务最大数目
	
	public final static int Speed_Hs = 200000; //高速和中速的分界线
	
	public final static int Speed_Ns = 100000;//中速和低俗的分界线
	
	public final static int Proba_Top = 100;//100概率上限

	public final static int Buffsize = 1024 * 500;

	public final static int Proba_Hs = 50; // 50%以上的概率取到高速
	
	public final static int Proba_Ns = 20; // 30%（50 - 20 = 30）以上的概率取到中速
	
	public final static String String_Url = "url";
	
	public final static String String_Portocol = "protocol";
	
	public final static String String_Num = "num";
	
	public final static String Agent_High = "high";
	
	public final static String Agent_Normal = "normal";
	
	public final static String Agent_Low = "low";
	
	public final static String Protocol_Http = "http";
	
	public final static String Protocol_socks5 = "sock5";
	
	public final static String URL_TEST_SPEED = "http://tieba.baidu.com/";
	
	public final static String URL_DianPing = "http://www.dianping.com";
	
	public final static String URL_SOCK5_FAKE = "URL_SOCK5_FAKE";
	
//	public final static String URL_TEST_SPEED = "http://www.baidu.com/";
	
	public final static int Agent_State_Live = 1;
	
	public final static int Agent_State_Dead = 0;

	public final static int SleepTime = 60 * 1000; //线程睡眠时间，从数据库读取间隔
	
	public final static int MaxAgentsNumRequestOnce = 300; //每次最大请求代理数
	
	
}

