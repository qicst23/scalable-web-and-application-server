package edu.upenn.cis455.webserver;

import java.util.HashMap;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class ServiceHandler extends DefaultHandler {

	int m_state = 0;
	String m_servletName;
	String m_paramName;
	int m_sessionTimeout = 0;   //added
	String m_appName;           //added

	HashMap<String, String> m_servlets = new HashMap<String, String>();
	HashMap<String, String> m_contextParams = new HashMap<String, String>();
	HashMap<String, HashMap<String, String>> m_servletParams = new HashMap<String, HashMap<String, String>>();
	HashMap<String, String> m_urlMappings = new HashMap<String, String>();  //added.  URL -> servletName

	public void startDocument(){ 	  //added
//		System.out.println("Document started!");
	}

	public void endDocument(){        //added
//		System.out.println("Document ended!");
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		//		dump("qName", qName);
		if (qName.compareTo("servlet-name") == 0) {
			m_state = 1;
		} else if (qName.compareTo("servlet-class") == 0) {
			m_state = 2;
		} else if (qName.compareTo("url-pattern") == 0) {
			m_state = 3;
		} else if (qName.compareTo("context-param") == 0) {
			m_state = 4;
		} else if (qName.compareTo("init-param") == 0) {
			m_state = 5;
		} else if (qName.compareTo("param-name") == 0) {
			m_state = (m_state == 4) ? 10 : 20;
		} else if (qName.compareTo("param-value") == 0) {
			m_state = (m_state == 10) ? 11 : 21;
		} else if (qName.compareTo("session-timeout") == 0) {
			m_state = 22;
		} else if (qName.compareTo("display-name") == 0) {
			m_state = 23;
		}
	}
	public void characters(char[] ch, int start, int length) {
		String value = new String(ch, start, length);
		if (m_state == 1) {
			m_servletName = value;
			m_state = 0;
		} else if (m_state == 2) {
			m_servlets.put(m_servletName, value);
			m_state = 0;
		} else if (m_state == 3) {
			m_urlMappings.put(value, m_servletName);
			m_state = 0;
		} else if (m_state == 10 || m_state == 20) {
			m_paramName = value;
		} else if (m_state == 11) {
			if (m_paramName == null) {
				System.err.println("Context parameter value '" + value + "' no name");
				System.exit(-1);
			}
			m_contextParams.put(m_paramName, value);
			m_paramName = null;
			m_state = 0;
		} else if (m_state == 21) {
			if (m_paramName == null) {
				System.err.println("Servlet parameter value '" + value + "' no name");
				System.exit(-1);
			}
			HashMap<String,String> p = m_servletParams.get(m_servletName);
			if (p == null) {
				p = new HashMap<String,String>();
				m_servletParams.put(m_servletName, p);
			}
			p.put(m_paramName, value);
			m_paramName = null;
			m_state = 0;
		} else if (m_state == 22){
			m_sessionTimeout = new Integer(value);
			m_state = 0;
		} else if (m_state == 23){
			m_appName = value;
			m_state = 0;
		}
	}


}