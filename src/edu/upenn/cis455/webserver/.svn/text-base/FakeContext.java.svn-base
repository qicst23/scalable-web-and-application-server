package edu.upenn.cis455.webserver;
import javax.servlet.*;
import java.util.*;

/**
 * @author Nick Taylor
 */
class FakeContext implements ServletContext {
	private HashMap<String,Object> attributes;
	private HashMap<String,String> initParams;
	private ServiceHandler serviceHandler;
	
	public FakeContext(ServiceHandler handler) {
		attributes = new HashMap<String,Object>();
		initParams = new HashMap<String,String>();
		serviceHandler = handler;
	}
	
	public Object getAttribute(String name) {
		return attributes.get(name);
	}
	
	public Enumeration getAttributeNames() {
		Set<String> keys = attributes.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}
	
	public ServletContext getContext(String name) {
		// must begin with "/"
		if (name.equals("/"))
			return this;
		return null;
	}
	
	public String getInitParameter(String name) {
		return initParams.get(name);
	}
	
	public Enumeration getInitParameterNames() {
		Set<String> keys = initParams.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}
	
	public int getMajorVersion() {
		return 2;
	}
	
	public String getMimeType(String file) {
		return null;
	}
	
	public int getMinorVersion() {
		return 4;
	}
	
	public RequestDispatcher getNamedDispatcher(String name) {
		// NOT REQUIRED in hw1ms2
		return null;
	}
	
	public String getRealPath(String path) {
		if(path != null) 
			return Dispatcher.getRootDir() + path; 
		return null;
	}
	
	public RequestDispatcher getRequestDispatcher(String name) {
		// NOT REQUIRED in hw1ms2
		return null;
	}
	
	public java.net.URL getResource(String path) {
		return null;
	}
	
	public java.io.InputStream getResourceAsStream(String path) {
		return null;
	}
	
	public java.util.Set getResourcePaths(String path) {
		return null;
	}
	
	public String getServerInfo() {
		return "CIS555 Java Servlet Server by Yayang";
	}
	
	public Servlet getServlet(String name) {
		return null;
	}
	
	public String getServletContextName() {
		return serviceHandler.m_appName;
	}
	
	public Enumeration getServletNames() {
		return null;
	}
	
	public Enumeration getServlets() {
		return null;
	}
	
	public void log(Exception exception, String msg) {
		log(msg, (Throwable) exception);
	}
	
	public void log(String msg) {
		System.err.println(msg);
	}
	
	public void log(String message, Throwable throwable) {
		System.err.println(message);
		throwable.printStackTrace(System.err);
	}
	
	public void removeAttribute(String name) {
		attributes.remove(name);
	}
	
	public void setAttribute(String name, Object object) {
		attributes.put(name, object);
	}
	
	void setInitParam(String name, String value) {
		initParams.put(name, value);
	}
}
