package com.tigerknows.proxy.web.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tigerknows.proxy.util.ByteUtil;


public class ByteWriter {
	@SuppressWarnings("unused")
	private static Log log = (Log)LogFactory.getLog(ByteWriter.class);
	
	private OutputStream os;
	protected String charset;
	protected int refX, refY;
	
	public ByteWriter(String charset, OutputStream os){
		this.charset = charset;
		this.os = os;
	}

	public ByteWriter(String charset, OutputStream os, int x, int y){
		this.charset = charset;
		this.os = os;
		this.refX = x;
		this.refY = y;
	}
	
	public ByteWriter cloneWith( OutputStream otherOs ){
		return new ByteWriter(charset, otherOs, refX, refY);
	}
	
	public String getCharset() {
		return this.charset;
	}

	public void writeByte(byte b) throws IOException {
		os.write(b);
	}
	
	public void writeByte(int b) throws IOException {
		os.write(ByteUtil.intToByte(b));
	}
	
	public void writeBytes(byte[] ba) throws IOException {
		for(byte b :ba)
			writeByte(b);
	}
	
	@Deprecated
	public void writeBytes(int[] ba) throws IOException {
		for(int b :ba)
			writeByte(b);
	}
	
	@Deprecated
	public void writeByteArray(byte[] ba) throws IOException {
		writeShort(ba.length);
		writeBytes(ba);
	}
	
	public void writeShort(short s) throws IOException {
		os.write(ByteUtil.uintTo2Bytes(s));
	}
	
	public void writeShort(int s) throws IOException {
		os.write(ByteUtil.uintTo2Bytes(s));
	}
	
	public void writeShorts(short[] sa) throws IOException {
		for(short s :sa)
			writeShort(s);
	}

	public void writeShortArray(short[] sa, OutputStream os) throws IOException {
		writeShort(sa.length);
		writeShorts(sa);
	}
	
	public void writeShorts(int[] sa) throws IOException {
		for(int s :sa)
			writeShort(s);
	}
	public void writeShortArray(int[] sa) throws IOException {
		writeShort(sa.length);
		writeShorts(sa);
	}
	
	public void writeInt(int i) throws IOException {
		os.write(ByteUtil.intTo4Bytes(i));
	}

	public void writeVarUint(long value) throws IOException {
		if (value < 0)
			throw new NumberFormatException("writeVarUint(" + value + ")");
		else if (value == 0) {
			writeByte(0);
		} else {
			List<Byte> list = new ArrayList<Byte>();

			list.add((byte) (value & 0x7f));
			value = value >> 7;

			while (value > 0) {
				list.add((byte) ((value & 0x7f | 0x80) & 0xff));
				value = value >> 7;
			}
			for (int i = list.size() - 1; i >= 0; i--) {
				writeByte(list.get(i));
			}
		}
	}

	public void writeVarInt(long value) throws IOException {
		long var = value;
		if(var == 0) {
			this.writeBytes(new byte[]{0, 0});
		}else if(var == -1) {
			this.writeBytes(new byte[]{(byte)0x7f, (byte)0xff});
		}else {
			List<byte[]> list = new ArrayList<byte[]>();
			int offset = 14;
			while(var != 0 && var != -1) {
				long a = var & 0x7fffL;
				list.add(new byte[]{(byte)(a >> 8), (byte)a});
				var = var >> offset;
				if(offset == 14)
					offset = 15;
			}
			for(int i = 1; i < list.size(); i++)
				list.get(i)[0] =  (byte)(list.get(i)[0] | 128);
			if(value < 0)
				list.get(0)[0]  = (byte)(list.get(0)[0] | 0x40);
			else
				list.get(0)[0]  = (byte)(list.get(0)[0] & 0xbf);
			for(int i = list.size() - 1; i >= 0; i--) 
				this.writeBytes(list.get(i));
		}
		
	}

	
	public void writeVarString(String s) throws IOException {
		if(s == null)
			s = "";
		byte[] data = s.getBytes(charset);
		this.writeVarUint(data.length);
		os.write(data);
	}
	
//	public void writeXObject(XObject xobject) throws IOException {
//		xobject.writeXObject(this);
//	}
	
	public void writeFloat(float value) throws IOException {
		int bits = Float.floatToIntBits(value);
		byte[] bytes = new byte[4];
		for(int i = 0; i < 4; i++) {
			bytes[3 - i] = (byte)(bits & 0xff);
			bits = bits >> 8;
		}
		this.writeBytes(bytes);
	}
	
	@Deprecated
	public void writeInts(int[] ia) throws IOException {
		for(int i :ia)
			writeInt(i);
	}
	
	@Deprecated
	public void writeIntArray(int[] ia) throws IOException {
		writeShort(ia.length);
		writeInts(ia);
	}
	
	@Deprecated
	public void writeLong(long l) throws IOException {

		byte[] buf = new byte[8];

		for (int i = 0; i < 8; i++) {
			buf[7 - i] = (byte) ((l >> (i * 8)) & 0xFF);
		}
		os.write(buf);
	}
	@Deprecated
	private void writeLongs(long[] la) throws IOException {
		for(long l :la)
			writeLong(l);
	}
	@Deprecated
	public void writeLongArray(long[] la) throws IOException {
			writeShort(la.length);
			writeLongs(la);
	}
	public void writeString(String s) throws IOException {
		ByteUtil.writeString(os, charset, s);
	}
	
	public void writeStringBytes(String s) throws IOException {
		if (s != null) {
			os.write( s.getBytes(charset) );
		}
	}
	
	public void writeCoordinatesCode(int refX, int refY, double x, double y) throws IOException {
		os.write(ByteUtil.getByteCodeFromCoordinates(refX, refY, x, y));
	}
	
	public void writeCoordinatesCode(double x, double y) throws IOException {
		writeCoordinatesCode(refX, refY, x, y);
	}
	
//	
//	@Deprecated
//	public <T> void writeArray(T[] array, ByteCoder<T> coder) throws IOException {
//		writeShort(array.length);
//		for (int i = 0; i < array.length; i++)
//			coder.code(array[i], this);
//	}
//	
//	@Deprecated
//	public <T> void writeArray(List<T> list, ByteCoder<T> coder) throws IOException {		
//		writeShort(list.size());
//		for (int i = 0; i < list.size(); i++){ 
//			coder.code(list.get(i), this);
//		}
//	}
//
//	@Deprecated
//	public <T> void writeArrayWithElementLength(List<T> list, ByteCoder<T> coder) throws IOException {		
//		writeShort(list.size());
//		for (int i = 0; i < list.size(); i++) 
//			writeWithLength(list.get(i), coder);
//	}
//
//	public <T> void writeWithLength(T o, ByteCoder<Long> lengthCoder, ByteCoder<T> coder) throws IOException {
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		ByteWriter newWriter = new ByteWriter(charset, bos, refX, refY);
//		coder.code(o, newWriter);
//		byte[] arr = bos.toByteArray();
//		lengthCoder.code((long)arr.length, this);
//		os.write(arr);
//	}
//	
//	@Deprecated
//	public <T> void writeWithLength(T o, ByteCoder<T> coder) throws IOException {
//		this.writeWithLength(o, new ByteCoder<Long>() {
//			public void code(Long o, ByteWriter writer) throws IOException {
//				writer.writeShort((int)(long)o);
//			}}
//		, coder);
//	}
	
	@SuppressWarnings("deprecation")
	public void writeDate(Date date) throws IOException {
		this.writeShort((short)(date.getYear() + 1900));
		this.writeByte((byte)(date.getMonth() + 1));
		this.writeByte((byte)date.getDate());
	}
	
	@Deprecated
	public int getOffset() {
		return ((ByteArrayOutputStream)this.os).size();
	}
	
	public void writeVarTinyInt(int value) throws IOException{
		List<Byte> list = new ArrayList<Byte>();
		if(value < 0){
			list.add((byte) (value & 0x3f | 0x40));
			value = value >> 6;
			while ((value ^ 0xffffffff) != 0) {
				list.add((byte) ((value & 0x7f | 0x80) & 0xff));
				value = value >> 7;		
			}
		}
		else{
			list.add((byte) (value & 0x3f));
			value = value >> 6;
			while (value != 0) {
				list.add((byte) ((value & 0x7f | 0x80) & 0xff));
				value = value >> 7;		
			}
		}
		for (int i = list.size() - 1; i >= 0; i--) {
			writeByte(list.get(i));
		}
	}
}
