package edu.upenn.cis455.webserver;
import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Todd J. Green
 */
class FakeRequest implements HttpServletRequest {

	private Properties m_params = new Properties();
	private Properties m_props = new Properties();
	private FakeSession m_session = null;
	private String m_method;

	//added
	Socket clientSocket;
	HashMap<String, String> reqHead = new HashMap<String, String>();
	private Vector<Cookie> m_cookies = new Vector<Cookie>();
	private FakeContext m_context;
	String encoding;
	String version;
	boolean starMatched;
	private InputStream inputStream;
	private Locale locale;

	String contextPath = "";
	String servletPath;
	String pathInfo;
	String queryString;


	FakeRequest(Socket socket, HashMap<String, String>head, 
			FakeContext context, FakeSession session) {
		clientSocket = socket;
		m_session = session;
		m_context = context;
		reqHead = head;

		for(String key: reqHead.keySet()){
			System.out.println("TRY ONE~!");
			// parse cookie first 
			if(key.equals("cookie")){

				String headValue = getHeader(key).split(": ")[1];
				String[] cookies = headValue.split(";");
				for(String ck : cookies){
					String k = ck.split("=")[0].trim();
					String v = ck.split("=")[1].trim();
					// If it's a session in cookoe
					if(k.equals("JSESSIONID")){
						session = Dispatcher.getSession(v);
					}
					// If it's a normal cookie, just 	 it
					else{
						Cookie cookie = new Cookie(k, v);
						m_cookies.add(cookie);
					}
				}
			}
		}


	}


	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getAuthType()
	 */
	public String getAuthType() {
		return BASIC_AUTH;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getCookies()
	 */
	public Cookie[] getCookies() {
		int size = m_cookies.size();
		if(size == 0)
			return null;
		return m_cookies.toArray(new Cookie[size]);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
	 */
	public long getDateHeader(String arg0) {
		String cookieKey = arg0.toLowerCase();
		if(!reqHead.containsKey(cookieKey))
			return -1;

		String datePattern = "EEE, dd MMM yyyy HH:mm:ss zzz";
		SimpleDateFormat parser = new SimpleDateFormat(datePattern);
		try{
			// In cookies, the first one is date
			String dateStr =  getHeader(arg0);
			Date date = parser.parse(dateStr);
			return date.getTime();
		}catch(ParseException e){
			throw new IllegalArgumentException();
		}

	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
	 */
	public String getHeader(String arg0) {
		String key = arg0.toLowerCase();
		if(!reqHead.containsKey(key))
			return null;
		return reqHead.get(key);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
	 */
	@SuppressWarnings("rawtypes")
	public Enumeration getHeaders(String arg0) {
		if(reqHead.isEmpty() || reqHead == null)// ###does not allow???
			return null;

		String key = arg0.toLowerCase();
		if(reqHead.containsKey(key)){
			String[] values = reqHead.get(key).split(", ");
			Vector<String> headers = new Vector<String>();
			for(int h = 0; h < values.length; h ++)
				headers.addElement(values[h]);
			return headers.elements();
		}
		else{ //return empty set
			Set<String> empty = new HashSet<String>();
			return Collections.enumeration(empty);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
	 */
	@SuppressWarnings("rawtypes")
	public Enumeration getHeaderNames() {
		if(reqHead.isEmpty() || reqHead == null)
			return null;
		return Collections.enumeration(reqHead.keySet());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
	 */
	public int getIntHeader(String arg0) {
		String key = arg0.toLowerCase();
		if(!reqHead.containsKey(key)){
			return -1;
		}
		try{
			int parsedInt = Integer.parseInt(reqHead.get(key));
			return parsedInt;
		}catch(NumberFormatException e){
			throw new NumberFormatException();
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getMethod()
	 */
	public String getMethod() {
		return m_method;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getPathInfo()
	 */
	public String getPathInfo() {
		if(pathInfo == "")
			return null;
		return pathInfo;
	}

	public void setPathInfo(String path){
		pathInfo = path;

	}
	public void setServletPath(String path){
		servletPath = path;
	}


	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
	 */
	public String getPathTranslated() {
		// NOT REQUIRED in hw1ms2
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getContextPath()
	 */
	public String getContextPath() {
		// only one web application per servlet container
		return "";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getQueryString()
	 */
	public String getQueryString() {
		return queryString;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
	 */
	public String getRemoteUser() {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
	 */
	public boolean isUserInRole(String arg0) {
		// NOT REQUIRED in hw1ms2
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
	 */
	public Principal getUserPrincipal() {
		// NOT REQUIRED in hw1ms2
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
	 */
	public String getRequestedSessionId() {
		return m_session.getId();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURI()
	 */
	public String getRequestURI() {
		return servletPath + pathInfo;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURL()
	 */
	public StringBuffer getRequestURL() {
		/* 
		 * http   :// localhost.com : 80   /555app      /servlet/servlet1  /xyz      ? a=b
		 * scheme     serverName      port contextPath  servletPath        pathInfo    queryString
		 * 
		 * URI = /555app/servlet/servlet1/xyz
		 * URL = http://localhost.com:80/555app/servlet/servlet1/xyz  (NO QUERY STRING)
		 */
		StringBuffer URL = new StringBuffer();
		URL.append(getScheme()).append("://");
		URL.append(getServerName()).append(":").append(getServerPort());
		URL.append(getContextPath()).append(getServletPath()).append(getPathInfo());
		return URL;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getServletPath()
	 */
	public String getServletPath() {
		if(starMatched)return "";
		return servletPath;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
	 */
	public HttpSession getSession(boolean create) {
		System.out.println("################in #######");
		// NOW counts for ONE ACCESS to sessions
		if (create) {
			if (! hasSession()) {
				System.out.println("@@@@@@No session");
				m_session = new FakeSession(m_context);
				Dispatcher.addSession(m_session);
				System.out.println("$$$$$$"  + Dispatcher.sessionList.size());
				System.out.println(" SS:     " + m_session.getId());
				reqHead.put("cookie", "JSESSIONID=" + m_session.getId());
			}
		} else {
			System.out.println("@@@@@@@@@@@Session3 TEst");
			if (! hasSession()) {

				m_session = null;
				Dispatcher.removeSession(m_session);
			}
			else{
				//				m_session.invalidate();
				//				m_session = null; 
				// update the last access time of session 
				long today = System.currentTimeMillis();
				long due =  m_session.getLastAccessedTime()  +
						m_session.getMaxInactiveInterval()*1000;
				System.out.println("\n$$$$$$$$$due?" + (due - today)/1000 + " seconds\n" );
				if(1 ==1 ){
					
					// If session too old, get a NEW one 
					m_session.invalidate();
//					m_session = new FakeSession((FakeContext)m_context);
//					Dispatcher.addSession(m_session);
//					reqHead.put("cookie", "JSESSIONID=" + m_session.getId());
					return null;
					// If session too old, get a NEW one 
				} else {
					m_session.setLastAccessedTime(today);
				}	
			}
		}
		return m_session;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession()
	 */
	public HttpSession getSession() {
		return getSession(true);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
	 */
	public boolean isRequestedSessionIdValid() {
		if(Dispatcher.isSessionValid(getRequestedSessionId()))
			return m_session.isValid();
		else return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
	 */
	public boolean isRequestedSessionIdFromCookie() {
		return true;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
	 */
	public boolean isRequestedSessionIdFromURL() {
		// NOT REQUIRED in hw1ms2
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
	 */
	public boolean isRequestedSessionIdFromUrl() {
		if(reqHead.containsKey("session"))
			return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#	Attribute(java.lang.String)
	 */
	public Object getAttribute(String arg0) {
		return m_props.get(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAttributeNames()
	 */
	public Enumeration getAttributeNames() {
		return m_props.keys();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		if(reqHead.containsKey("Character-Encoding"))
			return reqHead.get("Character-Encoding");
		return "ISO-8859-1";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {
		encoding = arg0;

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getContentLength()
	 */
	public int getContentLength() {
		if(reqHead.containsKey("Content-Length")){
			return Integer.parseInt(reqHead.get("Content-Length"));
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getContentType()
	 */
	public String getContentType() {
		if(reqHead.containsKey("Content-Type")){
			return reqHead.get("Content-Type");
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getInputStream()
	 */
	public ServletInputStream getInputStream() throws IOException {
		// NOT REQUIRED in hw1ms2
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
	 */
	public String getParameter(String arg0) {
		return m_params.getProperty(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterNames()
	 */
	public Enumeration getParameterNames() {
		return m_params.keys();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
	 */
	public String[] getParameterValues(String arg0) {
		String[] values = null;
		if(m_params.containsKey(arg0)){
			values = new String[1];
			values[0] = m_params.getProperty(arg0);
		}	
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterMap()
	 */
	public Map getParameterMap() {
		if(m_params == null || m_params.isEmpty())
			return null;
		return m_params;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getProtocol()
	 */
	public String getProtocol() {
		return version;
	}
	public void setProtocol(String protocol){
		version = protocol;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getScheme()
	 */
	public String getScheme() {
		return "http";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServerName()
	 */
	public String getServerName() {
		//return clientSocket.getLocalAddress().getHostName();
		if(!reqHead.containsKey("Host"))return null;
		String hostStr = reqHead.get("Host");
		return hostStr.substring(0, hostStr.indexOf(":"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServerPort()
	 */
	public int getServerPort() {
		return clientSocket.getPort();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getReader()
	 */
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(inputStream));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemoteAddr()
	 */
	public String getRemoteAddr() {
		if(clientSocket.isConnected())
			return clientSocket.getRemoteSocketAddress().toString();
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemoteHost()
	 */
	public String getRemoteHost() {
		if(clientSocket.isConnected())
			return reqHead.get("User-Agent");
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String arg0, Object arg1) {
		m_props.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String arg0) {
		m_props.remove(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocale()
	 */
	public Locale getLocale() {
		if (locale != null)
			return locale;
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocales()
	 */
	public Enumeration getLocales() {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#isSecure()
	 */
	public boolean isSecure() {
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
	 */
	public RequestDispatcher getRequestDispatcher(String arg0) {
		// NOT REQUIRED in hw1ms2
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
	 */
	public String getRealPath(String arg0) {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemotePort()
	 */
	public int getRemotePort() {
		if(clientSocket != null)
			return clientSocket.getPort();
		return -1;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalName()
	 */
	public String getLocalName() {
		if(clientSocket != null)
			return clientSocket.getInetAddress().getHostName();
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalAddr()
	 */
	public String getLocalAddr() {
		return clientSocket.getLocalAddress().toString();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalPort()
	 */
	public int getLocalPort() {
		return getServerPort();
	}

	void setMethod(String method) {
		m_method = method;
	}

	void setParameter(String key, String value) {
		m_params.setProperty(key, value);
	}

	void clearParameters() {
		m_params.clear();
	}

	boolean hasSession() {
		return ((m_session != null) && m_session.isValid());
	}


}
