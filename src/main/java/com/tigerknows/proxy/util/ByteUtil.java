package com.tigerknows.proxy.util;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ByteUtil {
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(ByteUtil.class);

    /**
     * 将int转换为byte，int取值范围为[0, 255]，转换所得byte应视作无符号数（Java不支持无符号数，因此所得byte可能取负值）
     * 
     * @throws 如果int取值不属于[0,255]，则抛出NumberFormatException
     * @param i
     *            要转换的int
     * @return 转换后的byte
     */
    public static byte intToByte(int i) {
        if (i > 255 || i < 0)
            throw new NumberFormatException("不能把" + i + "转换为1 byte，int取值必须属于[0, 255]。");
        return (byte) i;
    }

    /**
     * 将byte转换为int，转换后所得int取值范围为[0, 255]，应把byte视作无符号数（Java不支持无符号数，因此byte可能取负值）
     * 
     * @param b
     *            要转换的byte
     * @return 转换后的int
     */
    public static int byteToInt(byte b) {
        if (b < 0)
            return (int) b + 256;
        return b;
    }

    /**
     * 将int转换为两个byte，int为有符号整数，其由低到高第16个bit为符号位
     * 
     * @throws
     * 如果int大于Short.MAX_VALUE（32767）或小于Short.MIN_VALUE（-32768）则抛出NumberFormatException
     * @param i
     *            要转换的int
     * @return 转换后的byte[]，byte[0]为高8 bit组成的字节，byte[1]为低8 bit组成的字节
     */
    public static byte[] uintTo2Bytes(int i) {
        if (i > 65535 || i < 0)
            throw new NumberFormatException("不能把" + i + "转换为2 byte，否则会丢失高位信息。");
        int s = i;

        byte hi = (byte) ((s >>> 8) & 0xff);
        byte lo = (byte) (s & 0xff);
        return new byte[] { hi, lo };
    }

    /**
     * 将两个byte转换为int，int为有符号整数，其由低到高第16个bit为符号位，取值范围为[Short.MIN_VALUE, Short.MAX_VALUE]
     * 
     * @param b1
     *            高8 bit组成的字节
     * @param b2
     *            低8 bit组成的字节
     * @return 转换后的int
     */
    public static int byteToInt(byte b1, byte b2) {
        return (b1 << 8) | (b2 & 0xff);
    }

    /**
     * 将int转换为4个byte，int为有符号整数
     * 
     * @param i
     *            要转换的int
     * @return 转换后的byte[]，byte[0]为最高8 bit组成的字节，byte[3]为最低8 bit组成的字节
     */
    public static byte[] intTo4Bytes(int i) {
        byte[] array = new byte[4];
        array[3] = (byte) (i & 0xff);
        array[2] = (byte) ((i >>> 8) & 0xff);
        array[1] = (byte) ((i >>> 16) & 0xff);
        array[0] = (byte) ((i >>> 24) & 0xff);
//        if(log.isDebugEnabled())
//        	log.debug(byte2HexString(array));
        return array;
    }

    /**
     * 将4个byte转换为int，int为有符号整数
     * 
     * @param b1
     *            最高8 bit组成的字节
     * @param b2
     *            次高8 bit组成的字节
     * @param b3
     *            次低8 bit组成的字节
     * @param b4
     *            最低8 bit组成的字节
     * @return 转换后的int
     */
    public static int byteToInt(byte b1, byte b2, byte b3, byte b4) {
        return (b1 << 24) | ((b2 << 16) & 0xff0000) | ((b3 << 8) & 0xff00) | (b4 & 0xff);
    }

    /**
     * 将相对坐标40 bit编码还原为经纬度坐标
     * 
     * @param refX
     *            参考点经度
     * @param refY
     *            参考点纬度
     * @param ba
     *            byte数组，其中包含有相对坐标40 bit编码，共5个字节
     * @param offset
     *            ba[offset]到ba[offset + 4]为编码数据
     * @return 还原所得经纬度，double[0]为经度，double[1]为纬度，保留5位小数
     */
    public static double[] getCoordinatesFromByteCode(int refX, int refY, byte[] ba,
            int offset) {
        byte b0 = ba[offset];
        byte b1 = ba[offset + 1];
        byte b2 = ba[offset + 2];
        byte b3 = ba[offset + 3];
        byte b4 = ba[offset + 4];

        int dx = ((b0 << 12) & 0xff000) | ((b1 << 4) & 0xff0) | ((b2 >> 4) & 0xf);
        int dy = (((b2 & 0xf) << 16) & 0xf0000) | ((b3 << 8) & 0xff00) | (b4 & 0xff);

        return new double[] { (double) dx / 100000 + refX, (double) dy / 100000 + refY };
    }

    /**
     * 将绝对经纬度转换为相对于参考点经纬度的40 bit编码<br>
     * 
     * 编码规则为(x - refX)保留5为小数四舍五入后与refX的差，小数点右移5位取整，<br>
     * 所得整数以20 bit表示（高位优先），y做相应处理，把所得40 bit以5个字节表示。
     * 
     * @param refX
     *            参考点经度
     * @param refY
     *            参考点纬度
     * @param x
     *            绝对精度
     * @param y
     *            绝对纬度
     * 
     * @throws IllegalArgumentException
     *             (x - refX)保留5为小数四舍五入后与refX的差取值范围为[0, 10)，<br>
     *             否则抛出IllegalArgumentException<br>
     *             (y - refY)保留5为小数四舍五入后与refY的差取值范围为[0, 10)，<br>
     *             否则抛出IllegalArgumentException
     * @return 40 bit编码后数据，共5个字节
     */
    public static byte[] getByteCodeFromCoordinates(int refX, int refY, double x, double y) {
    	
//    	log.debug("refx: " + refX + "  refy: " + refY);
//    	log.trace(refX);
    	
    	if(refX < 0)
    		refX = refX & 0xff;
    	
        int dx = (int) Math.round((x - refX) * 100000);
        int dy = (int) Math.round((y - refY) * 100000);

        if (dx < 0 || dx >= 1000000)
            throw new IllegalArgumentException("参数x: " + x + "保留5位小数舍入后与 参数refX: " + refX
                    + "的个位之差大于9或小于0！");
        if (dy < 0 || dy >= 1000000)
            throw new IllegalArgumentException("参数y: " + y + "保留5位小数舍入后与 参数refY: " + refY
                    + "的个位之差大于9或小于0！");

        byte b0 = (byte) (dx >>> 12);
        byte b1 = (byte) ((dx >>> 4) & 0xff);
        byte b2 = (byte) (((dx & 0xf) << 4) | (dy >>> 16));
        byte b3 = (byte) ((dy >>> 8) & 0xff);
        byte b4 = (byte) (dy & 0xff);
        byte[] arr =  new byte[] { b0, b1, b2, b3, b4 };

//        if(log.isDebugEnabled())
//        	log.debug(byte2HexString(arr));
        return arr;
    }
    
    public static byte[] getByteCodeFromCoordinates(int refX, int refY, int x, int y) {
    	
//    	log.debug("refx: " + refX + "  refy: " + refY);
//    	log.trace(refX);

        int dx = x - refX*100000;
        int dy = y - refY*100000;

        if (dx < 0 || dx >= 1000000)
            throw new IllegalArgumentException("参数x: " + x + "保留5位小数舍入后与 参数refX: " + refX
                    + "的个位之差大于9或小于0！");
        if (dy < 0 || dy >= 1000000)
            throw new IllegalArgumentException("参数y: " + y + "保留5位小数舍入后与 参数refY: " + refY
                    + "的个位之差大于9或小于0！");

        byte b0 = (byte) (dx >>> 12);
        byte b1 = (byte) ((dx >>> 4) & 0xff);
        byte b2 = (byte) (((dx & 0xf) << 4) | (dy >>> 16));
        byte b3 = (byte) ((dy >>> 8) & 0xff);
        byte b4 = (byte) (dy & 0xff);
        byte[] arr =  new byte[] { b0, b1, b2, b3, b4 };

//        if(log.isDebugEnabled())
//        	log.debug(byte2HexString(arr));
        return arr;
    }

    public static void writeString(OutputStream os, String charset, String str)
            throws IOException {
        if (str != null) {
            byte[] ba = str.getBytes(charset);
            os.write(uintTo2Bytes(ba.length));
            os.write(ba);
        } else {
            byte[] ba = "".getBytes(charset);
            os.write(uintTo2Bytes(ba.length));
            os.write(ba);
        }
    }
    
    public static String byte2HexString(byte[] b) {
    	StringBuilder sb = new StringBuilder();
		for (int i = 0; i < b.length; i++) { 
			String hex = byte2HexString(b[i]);
			sb.append(hex);
			sb.append(" ");
		} 
		return sb.toString();
    }
    
    public static String byte2HexString(byte b) {
    	String hex = Integer.toHexString(b & 0xFF); 
		if (hex.length() == 1)  
			hex = '0' + hex;
		return hex;
    }

	public static byte[] float2Bytes(float value) {
		int i = Float.floatToIntBits(value);
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (i & 0xff);
		bytes[1] = (byte) ((i >>> 8) & 0xff);
		bytes[2] = (byte) ((i >>> 16) & 0xff);
		bytes[3] = (byte) ((i >>> 24) & 0xff);
		return bytes;
	}

	public static float bytes2Float(byte[] b) {
		int i = (b[3] << 24) | ((b[2] << 16) & 0xff0000) | ((b[1] << 8) & 0xff00) | (b[0] & 0xff);
		return Float.intBitsToFloat(i);
	}
	
}
