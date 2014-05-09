package com.tigerknows.proxy.exception;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtil {
	
		public static String getExceptionString(Exception e){
			String result;
			StringWriter sw = new StringWriter();
			PrintWriter pw = new   PrintWriter(sw);   
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			result = sw.toString();
			pw.close();
			try {
				sw.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return result;
		}
}
