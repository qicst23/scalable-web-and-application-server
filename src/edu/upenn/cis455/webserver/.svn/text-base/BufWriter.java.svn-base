package edu.upenn.cis455.webserver;

import java.io.OutputStream;
import java.io.PrintWriter;

public class BufWriter extends PrintWriter{
	
	
	public BufWriter(OutputStream outputStream){
		super(outputStream);
		bodyBuf = new StringBuffer();
		headBuf = new StringBuffer();
	}
	
	public void println(String str){
//		System.out.println("Appended" + str + "in Body");
		bodyBuf.append(str);
	}
	
	
	public void flush(){
		if(headBuf.length() != 0){
			
			int bodyLength = bodyBuf.toString().getBytes().length;
			headBuf.append("Content-Length: " + bodyLength + "\r\n");
			
			System.out.println("*** headBuf ***\n" + headBuf.toString());
			System.out.println("*** bodyBuf ***\n" + bodyBuf.toString());
			super.print(headBuf.toString() + "\r\n");
			super.print(bodyBuf.toString());
			super.flush();
			System.out.println("\nServer has sent and commited the response.");
		}
	}
	
	public void setRespHead(StringBuffer buf){
		headBuf = buf;
	}
	
	public void addHead(String str){
//		System.out.println("Appended" + str + "in head");
		headBuf.append(str).append("\r\n");
		
	}
	private StringBuffer bodyBuf;
	private StringBuffer headBuf;
}
