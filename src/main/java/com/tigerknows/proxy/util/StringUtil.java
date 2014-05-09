package com.tigerknows.proxy.util;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;


public class StringUtil {

    private static final String CHARSET = "UTF-8";


    private static NumberFormat longitudeFormat;

    private static NumberFormat latitudeFormat;

    private static NumberFormat priorityFormat;

    static {
        longitudeFormat = NumberFormat.getInstance();
        longitudeFormat.setMaximumIntegerDigits(3);
        longitudeFormat.setMinimumIntegerDigits(3);
        longitudeFormat.setMinimumFractionDigits(5);
        longitudeFormat.setMaximumFractionDigits(5);

        latitudeFormat = NumberFormat.getInstance();
        latitudeFormat.setMaximumIntegerDigits(2);
        latitudeFormat.setMinimumIntegerDigits(2);
        latitudeFormat.setMinimumFractionDigits(5);
        latitudeFormat.setMaximumFractionDigits(5);

        priorityFormat = NumberFormat.getInstance();
        priorityFormat.setMaximumIntegerDigits(2);
        priorityFormat.setMinimumIntegerDigits(2);
    }

    public static String getDefaultCharset() {
        return CHARSET;
    }

    public static String joinString(long[] array, String str) {
    	if(array == null)
    		return "null";
        StringBuilder sb = new StringBuilder();
        for (long l : array)
            sb.append(l + str);
        if(sb.length() > 0)
        	sb.delete(sb.length() - 1, sb.length());
        return sb.toString();
    }

    public static <T> String joinString(List<T> list, String str) {
    	if(list == null)
    		return "null";
        StringBuilder sb = new StringBuilder();
        for (T l : list)
            sb.append(l + str);
        if(sb.length() > 0)
        	sb.delete(sb.length() - 1, sb.length());
        return sb.toString();
    }

    public static <T> String joinString(T[] list, String str) {
    	if(list == null)
    		return "null";
        StringBuilder sb = new StringBuilder();
        int i;
        for (i = 0; i < list.length - 1; i++)
            sb.append(list[i] + str);
        sb.append(list[i]);
        return sb.toString();
    }

    /**
     * 切去float多余精度
     * 
     * @param value
     *            原始浮点数值
     * @param len
     *            保留小数点后的位数
     * @return
     */
    public static String cutFloat(float value, int len) {
        String str = Float.toString(value);
        int index = str.indexOf(".");
        if (index <= 0)
            return str;
        index += len + 1;
        index = index <= str.length() ? index : str.length();
        return str.substring(0, index);
    }
    
    /**
     * 保留len位小数
     * @param value
     * @param len
     * @return
     */
    public static String cutDouble(double value, int len) {
    	return cutFloat((float)value, len);
    }
    /**
     * 保留len位小数
     * @param value
     * @param len
     * @return
     */
    @Deprecated
    public static String cutDouble(double value) {
		return cutFloat((float)value, 5);
    }

    /**
     * @deprecated
     * @param name
     * @return
     */
    public static String trimBusName(String name) {
        int index = name.indexOf("(");
        String fix = "路";
        if (index < 0)
            return name;
        return name.substring(0, index);
    }

    public static String getCleanedBusLineName(String name) {
        //String str = name.replaceAll("[(（][^()（）]*[)）]$", "");
    	//String str = name.replaceAll("\\([^()]*(((?'Open'\\()[^()]*)+((?'-Open'\\))[^()]*)+)*(?(Open)(?!))\\)$", "");
    	int length = name.length();
    	String str = name;
    	if(name.charAt(length-1)==')' || name.charAt(length-1)=='）'){
    		int kuohao = 1;
    		char c;
    		for(int i = length-2; i>0; i--){
    			c = name.charAt(i);
    			if(c == ')' || c == '）')
    				kuohao++;
    			else if(c == '(' || c ==  '（')
    				kuohao--;
    			if(kuohao == 0){
    				str = name.substring(0, i);
    				break;
    			}
    		}
    	}

        if (str.matches(".*[\\d０１２３４５６７８９]$")) {
            str = str + "路";
        }
        return str.length() == 0 ?name:str;
    }

//    public static String getCleanedKeyword(String keyword) {
//        String key = com.tigerknows.common.util.CharactorUtil.sbc2dbc(keyword.trim());
//        String str = key.replaceAll("[(（)）\\\\]", " ");
//
//        str = str.trim();
//        str = str.replaceAll("[\n()\\[\\]<>{}~#.,?!\"@/:_;+&%*='|^$£€¥¤§`\\\\-]+$", "");
//        str = str.replaceAll("^[\n()\\[\\]<>{}~#.,?!\"@/:_;+&%*='|^$£€¥¤§`\\\\-]+", "");
//        str = str.trim();
//
//        if (str.length() > MAX_KEYWORD_LEN)
//            return str.substring(0, MAX_KEYWORD_LEN);
//        else
//            return str;
//    }

    public static String formatX(double x) {
        return longitudeFormat.format(x);
    }

    public static String formatY(double y) {
        return latitudeFormat.format(y);
    }

    public static String formatPriority(short priority) {
        return priorityFormat.format(priority);
    }
    
    public static long strTime2longMilli(String strTime, String format){
    	Calendar c = Calendar.getInstance();
		try {
			c.setTime(new SimpleDateFormat(format).parse(strTime));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return c.getTimeInMillis();
    }
    
}
