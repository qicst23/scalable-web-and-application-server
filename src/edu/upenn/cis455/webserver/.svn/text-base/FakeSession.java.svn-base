package edu.upenn.cis455.webserver;
import java.util.Enumeration;
import java.util.Properties;
import java.security.SecureRandom;
import java.math.BigInteger;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 * @author Todd J. Green
 */
@SuppressWarnings("deprecation")
class FakeSession implements HttpSession {

	private Properties m_props = new Properties();
	private boolean m_valid = true;

	private boolean isNew;
	private long creationTime;
	private FakeContext context;
	private String sessionId;
	private long lastAccessedTime;
	private int maxInactiveInterval;;


	public FakeSession(FakeContext fc){

		context = fc;
		// generate secure session id (classical method) 
		SecureRandom random = new SecureRandom();
		sessionId = new BigInteger(130, random).toString(32);
		
		// set time
		creationTime = System.currentTimeMillis();
		lastAccessedTime = creationTime;
		if(context.getAttribute("session-timeout") != null){
			
			String timeoutStr = context.getAttribute("session-timeout").toString();
			setMaxInactiveInterval(Integer.parseInt(timeoutStr));
		}
		
		// set boolean values
		isNew = true;
		m_valid = true;
	}


	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getCreationTime()
	 */
	public long getCreationTime() {
		return creationTime;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getId()
	 */
	public String getId() {
		if(!isValid())
			throw new IllegalStateException();
		return sessionId;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getLastAccessedTime()
	 */
	public long getLastAccessedTime() {
		if(!isValid())
			throw new IllegalStateException();
		return lastAccessedTime;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getServletContext()
	 */
	public ServletContext getServletContext() {
		return context;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#setMaxInactiveInterval(int)
	 */
	public void setMaxInactiveInterval(int arg0) {
		maxInactiveInterval = arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getMaxInactiveInterval()
	 */
	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getSessionContext()
	 */
	public HttpSessionContext getSessionContext() {
		// Deprecated
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String arg0) {
		if(!isValid())
			throw new IllegalStateException();
		return m_props.get(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getValue(java.lang.String)
	 */
	public Object getValue(String arg0) {
		// Deprecated
		return m_props.get(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getAttributeNames()
	 */
	@SuppressWarnings("rawtypes")
	public Enumeration getAttributeNames() {
		return m_props.keys();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getValueNames()
	 */
	public String[] getValueNames() {
		// Deprecated
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String name, Object value) {
		
		if(!isValid())
			throw new IllegalStateException();
		if(value == null)
			removeAttribute(name);
		else {
			System.out.println("Hey!" + "Set Attr!");
			m_props.put(name, value);
			
		}
			
	}
	public void setLastAccessedTime(long time){
		lastAccessedTime = time;
		isNew = false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#putValue(java.lang.String, java.lang.Object)
	 */
	public void putValue(String arg0, Object arg1) {
		// Deprecated
		//		m_props.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String name) {
		if(!isValid())
			throw new IllegalStateException();
		if(m_props.isEmpty())return;   //### how to check no object bound
		else
			m_props.remove(name);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#removeValue(java.lang.String)
	 */
	public void removeValue(String arg0) {
		// Deprecated
		//		m_props.remove(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#invalidate()
	 */
	public void invalidate() {
		if(!isValid())
			throw new IllegalStateException();
		m_valid = false;
		Dispatcher.removeSession(this);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#isNew()
	 */
	public boolean isNew() {
		if(!isValid())
			throw new IllegalStateException();
		return isNew;
	}

	boolean isValid() {
		return m_valid;
	}


}
