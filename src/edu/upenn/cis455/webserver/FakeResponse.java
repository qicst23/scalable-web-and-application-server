package edu.upenn.cis455.webserver;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * @author tjgreen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FakeResponse implements HttpServletResponse {


	/* From request */
	private FakeRequest request;
	private FakeSession session;

	/* For Response  */

	//head
	HashMap<String, StringBuffer> respHeadMap = new HashMap<String, StringBuffer>();
	StringBuffer respHeadBuf = new StringBuffer();

	//cookie
	Vector<Cookie> respCookies = new Vector<Cookie>();

	//head details
	String contentType;
	int contentLength;
	String version;
	int statusNo;
	String statusMsg;
	String encoding;
	int bufferSize;
	Locale locale;

	// I/Os
	OutputStream outputStream;  
	BufWriter bufWriter;

	// flag
	boolean committed = false;
	boolean writerUsed = false;




	public FakeResponse(FakeRequest fakeRequest, OutputStream out) throws IOException{
		request = fakeRequest;
		outputStream = out;
		version = request.getProtocol();
		session = (FakeSession) request.getSession();
		//		bufWriter = new BufWriter(new DataOutputStream(outputStream));
		//		respCookies = cookies;
		addDateHeader("Date", (new Date()).getTime());
		addHeader("Server", "CIS555 Java Servlet Server");// ###Change per context
		addHeader("Connection", "close");
	}


	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
	 */
	public void addCookie(Cookie arg0) {
		if(respCookies != null)
			respCookies.add(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
	 */
	public boolean containsHeader(String arg0) {
		return respHeadMap.containsKey(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
	 */
	public String encodeURL(String arg0) {
		return arg0;  //### do it if I have time. (encode cookie in url)
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
	 */
	public String encodeRedirectURL(String arg0) {
		return arg0;  //### do it if I have time. (encode cookie in url)
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
	 */
	public String encodeUrl(String arg0) {
		return arg0; // ###do it if I have time
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
	 */
	public String encodeRedirectUrl(String arg0) {
		return arg0;    //### do it if I have time. (encode cookie in url)
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
	 */
	public void sendError(int sc, String msg) throws IOException {
		if(isCommitted())
			throw new IllegalStateException();
		setStatus(sc);
		bufWriter.println("<html><body>");
		bufWriter.println("<h1>" + version + "Status Code: " + sc + 
				"; Error Msg: " + msg + "</h1>");
		bufWriter.println("</body></html>");
		flushBuffer();
	}


	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendError(int)
	 */
	public void sendError(int sc) throws IOException {
		if(isCommitted())
			throw new IllegalStateException();
		setStatus(sc);
		bufWriter.println("Status Code: " + sc);
		flushBuffer();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
	 */
	public void sendRedirect(String arg0) throws IOException {
		System.out.println("[DEBUG] redirect to " + arg0 + " requested");
		System.out.println("[DEBUG] stack trace: ");
		Exception e = new Exception();
		StackTraceElement[] frames = e.getStackTrace();
		for (int i = 0; i < frames.length; i++) {
			System.out.print("[DEBUG]   ");
			System.out.println(frames[i].toString());
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
	 */
	public void setDateHeader(String name, long date) {
		SimpleDateFormat parser = new SimpleDateFormat("EEE, dd MM HH:mm:ss zzz yyyy");
		if(containsHeader(name))
			respHeadMap.remove(name);
		respHeadMap.put(name, new StringBuffer(parser.format(date)));

	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
	 */
	public void addDateHeader(String name, long date) {
		SimpleDateFormat parser = new SimpleDateFormat("EEE, dd MM HH:mm:ss zzz yyyy");
		if(containsHeader(name))
			// allows response headers to handle multiple values
			respHeadMap.get(name).append(", " + parser.format(date));
		respHeadMap.put(name, new StringBuffer(parser.format(date)));


	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
	 */
	public void setHeader(String name, String value) {
		if(containsHeader(name))
			respHeadMap.remove(name);
		respHeadMap.put(name, new StringBuffer(value));

	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
	 */
	public void addHeader(String name, String value) {
		if(containsHeader(name))
			// allows response headers to handle multiple values
			respHeadMap.get(name).append(value);
		respHeadMap.put(name, new StringBuffer(value));

	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
	 */
	public void setIntHeader(String name, int value) {
		if(containsHeader(name))
			respHeadMap.remove(name);
		respHeadMap.put(name, new StringBuffer().append(value));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
	 */
	public void addIntHeader(String name, int value) {
		if(containsHeader(name))
			// allows response headers to handle multiple values
			respHeadMap.get(name).append(value);
		respHeadMap.put(name, new StringBuffer().append(value));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int)
	 */
	public void setStatus(int arg0) {
		statusNo = arg0;
		switch(statusNo){
		case 200: statusMsg = "OK";
		break;
		case 301: statusMsg = "Move Permanently";
		break;
		case 302: statusMsg = "Move Temporarily";
		break;
		}//#### need to add some
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int, java.lang.String)
	 */
	public void setStatus(int arg0, String arg1) {
		// Deprecated

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		return "ISO-8859-1";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getContentType()
	 */
	public String getContentType() {
		if(contentType == null)
			return "text/html";
		return contentType;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getOutputStream()
	 */
	public ServletOutputStream getOutputStream() throws IOException {
		// NOT REQUIRED in hw1ms2
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getWriter()
	 */
	public PrintWriter getWriter() throws IOException {
		//return bufWriter;
		//		if(writerUsed){
		//			System.out.println("used PrintWriter" );
		//			return this.printWriter;
		//		}
		//		// set committed to true whenever being called
		//		committed = true;
		//		writerUsed = true;
		//		// Wrap up to print bytes
		//		//OutputStreamWriter osWriter = new OutputStreamWriter(outputStream, getCharacterEncoding());
		//		System.out.println("In PrintWriter" );
		//		// Wrap up to print text files

		//		pw.write("ab");
		//		pw.flush();

		//		return new PrintWriter(osWriter);

		// Zhang Min's method
		//		pw = new PrintWriter(outputStream);
		//		return pw;
		bufWriter = new BufWriter(new DataOutputStream(outputStream));
		return bufWriter;

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String arg0) {
		//if getWriter has been called we COULD NOT use this methods
		if(!isCommitted()){
			encoding = arg0;
			setHeader("Character-Encoding", arg0);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setContentLength(int)
	 */
	public void setContentLength(int arg0) {
		if(!isCommitted()){
			contentLength = arg0;
			setIntHeader("Content-Length", arg0);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
	 */
	public void setContentType(String arg0) {
		if(!isCommitted()){
			contentType = arg0;
			setHeader("Content-Type", arg0);
		}

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setBufferSize(int)
	 */
	public void setBufferSize(int arg0) {
		// MUST be called before getWriter(). 
		// Large buffer leaves time for status & head settings
		if(!isCommitted())
			bufferSize = arg0;
		throw new IllegalStateException();

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getBufferSize()
	 */
	public int getBufferSize() {
		return bufferSize;
	}

	/* (non-Javadoc)   ########check this ##########
	 * @see javax.servlet.ServletResponse#flushBuffer()
	 */
	public void flushBuffer() throws IOException {
		if(isCommitted())
			return;
		// send all the content in buffer
		statusNo = 200;
		sendServletStatus(statusNo);

		for(String key : respHeadMap.keySet()){
			respHeadBuf.append(key + ": " + respHeadMap.get(key).toString()).append("\r\n");
		}
		/* add header */


		// Check sessions
		//		ArrayList<FakeSession>sessionList = Dispatcher.getSessions();
		//		if(session != null){
		//			Cookie ck = new Cookie("Session", session.getId());
		//			ck.setMaxAge(session.getMaxInactiveInterval());
		//			addCookie(ck);
		//
		//		}
		System.out.println("in FakeResponse flushBuffer");
		//
		// Construct Cookie
		for(Cookie ck : respCookies){
			long expire;
			// If not specified, the cookie will expire when the user's session ends.
			if(ck.getMaxAge() > 0  && session != null)
				expire = session.getCreationTime() + 1000 * ck.getMaxAge();
			expire = new Date().getTime() + 1000 * ck.getMaxAge();

			respHeadBuf.append("Set-Cookie: " + ck.getName() + "=" + ck.getValue() +
					"; expires=" + getCookieGMT(new Date(expire)) + "\r\n") ;
			System.out.println("Hey!!!!!" + respHeadBuf.toString());
		}

		// Add session in cookie
			if(session.isValid()){
				respHeadBuf.append("Set-Cookie: " + "JSESSIONID=" + session.getId()).append("\r\n");
		}


		//		bufWriter.addHead("a");
		// deliever the head response to the special writer
		bufWriter.setRespHead(respHeadBuf);
		bufWriter.flush();
		committed = true;
	}




	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#resetBuffer()
	 */
	public void resetBuffer() {
		if(!isCommitted()){
			bufWriter = new BufWriter(new DataOutputStream(outputStream));
		}
		throw new IllegalStateException();

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#isCommitted()
	 */
	public boolean isCommitted() {
		//"A commited response has already had its status code and headers written."
		return committed;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#reset()
	 */
	public void reset() {
		if(!isCommitted()){
			// clear all the contents 
			OutputStreamWriter osWriter;
			try {
				osWriter = new OutputStreamWriter(outputStream, getCharacterEncoding());
				// also clear all the head the status
				setStatus(200);
				respHeadMap = new HashMap<String, StringBuffer>();
				respHeadBuf = new StringBuffer();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale arg0) {
		if(!isCommitted()){
			locale = arg0;
			setHeader("Locale", locale.toString());
		}

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getLocale()
	 */
	public Locale getLocale() {
		return locale;
	}


	// Some additional auxiliary functions!

	/**
	 * This function reply to client the Servlet Response#initial
	 */
	public void sendServletStatus(int code){
		String status;
		switch(code){
		case 100: status = version + " 100 Continue\r\n\r\n";
		break;
		case 200: status =  version + " 200 Continue\r\n";
		break;
		case 301: status = version  + "301 Move Permanently\r\n";
		break;  // Mainly for MS2
		case 302: status = version + " 302 Move Temporarily\r\n";
		break;  //  Mainly for MS2
		case 304: status = version + " 304 Not Modified\r\n";
		break;
		case 500: status = version + "500 Internal Server Error\r\n";
		break; // Mainly for MS2
		case 501: status = version + " 501 Not Implemented\r\n";
		break;
		case 403: status = version + " 403 Forbidden\r\n";
		break;
		case 404: status = version + " 404 Not Found\r\n";
		break;
		case 412: status = version + " 412 Precondition Failed\r\n";
		break;
		default: status = version + " 400 Bad Request\r\n";
		break;
		}
		bufWriter.write(status);
	}



	/**
	 * This function setSeverInfo from context
	 */

	/**
	 * This parse into GMT date
	 */
	public String getDateGMT(Date date){
		// @HTTP 1.1: MUST include today'date time stamp in header 
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyy HH:mm:ss zzz");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(date);

	}

	public String getCookieGMT(Date date){
		DateFormat dateFormat = new SimpleDateFormat("EEE, d-MMM-yyyy HH:mm:ss z");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(date);
	}



}
