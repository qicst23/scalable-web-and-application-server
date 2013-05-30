package edu.upenn.cis455.webserver;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;
import java.text.ParseException;
import java.util.Date;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;



/**
 * This class processes threads in task queue one by one.
 * @author Yayang Tian
 */
public class Worker extends Thread{
	// status
	private TaskQueue taskQueue = null;
	private Socket clientSocket;
	boolean stopped = false;
	private String status = "Waiting";
	// I/Os
	String localPath;
	boolean sendBody = true;
	BufferedReader in;
	PrintWriter out;
	OutputStream outputStream;
	File file;
	static boolean dumpDetail = true;
	//request
	String reqInit = "";
	String method = "";
	String version = "HTTP/1.1";
	HashMap<String, String> reqHead = new HashMap<String, String>();
	// response
	String respInit ="";
	StringBuffer reqBody = new StringBuffer();
	StringBuffer respHead = new StringBuffer();
	StringBuffer respBody = new StringBuffer();

	// Servlet
	HttpServlet servlet;
	String reqUri;
	String servletPath;
	String pathInfo;
	String queryString;
	String postString;

	static Logger logger = Logger.getLogger(Dispatcher.class);
	FileAppender fileAppender;

	public Worker(TaskQueue queue){
		taskQueue = queue;
	}

	public void run(){

		while(!stopped){
			try{
				/* Connect to clientSocket */
				status = "Waiting";
				clientSocket = taskQueue.remove();
				clientSocket.setSoTimeout(40000);
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				outputStream = clientSocket.getOutputStream();
				out = new PrintWriter(outputStream);

				/* Get and store useful request info */
				readRequest();

				/* Check request validty and respond */
				parseAndReply();

			} catch(InterruptedException e){
				try {
					(Dispatcher.getServerSocket()).close();
					if(clientSocket != null)
						clientSocket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
					dump("Unable to close the socket!");
				}
			} catch (IOException e) {
				e.printStackTrace();
				dump("Maybe Time out because of long idle time");
				Dispatcher.closeServer();
				//				throw new InterruptedException();

			}
		}
	}


	/**
	 * This function parse incoming task
	 */
	public void readRequest() throws IOException, InterruptedException{

		/* Parse inititial line */
		reqInit = in.readLine();
		dump("Init Line", reqInit);
		if(reqInit == "" || reqInit == null){
			sendResponse(400, false);  				// 400 Bad Request

		}else{
			StringTokenizer st = new StringTokenizer(reqInit);
			if(st.hasMoreElements())
				method = st.nextToken();
			if(st.hasMoreElements()){
				localPath =st.nextToken();
				if (localPath.toLowerCase().startsWith("http://")) {
					localPath = localPath.substring(5);
					dump("localPath", localPath);
				}
			}
			if(st.hasMoreElements())
				version = st.nextToken();
		}

		/* Parse header line */
		String thisLine = in.readLine();
		//		System.out.println("~~~~The content of the line");
		while((thisLine != null && !thisLine.equals(""))){
			if(thisLine.toLowerCase().startsWith("connection: close")){
				throw new InterruptedException();
			}

			//			System.out.println(thisLine);
			// header name is case-insensitive (yet value not)
			if(thisLine.toLowerCase().startsWith("host:")){
				reqHead.put("host", thisLine);
			}
			if(thisLine.toLowerCase().startsWith("content-type:")){
				reqHead.put("content-type", thisLine);
			}
			if(thisLine.toLowerCase().startsWith("content-length:")){
				reqHead.put("content-length", thisLine);
			}
			if(thisLine.toLowerCase().startsWith("if-modified-since:") ||
					thisLine.startsWith("if-unmodified-since:"))
				reqHead.put("dateReq", thisLine);
			if(thisLine.toLowerCase().startsWith("cookie:"))
				reqHead.put("cookie", thisLine);
			thisLine = in.readLine();
		}

		// ### Dealing with POST
		if(method.equals("POST") && reqHead.containsKey("content-type")){
			System.out.println("Hey!" + "here");
			reqBody = new StringBuffer();
			String str = reqHead.get("content-length");
			int length = Integer.parseInt(str.split(":")[1].trim());
			int readChar = 0;
			while (true) {
				reqBody.append((char)in.read());
				readChar ++;
				if(readChar >= length)break;
			}
		}

		/* Things to print in Console */
		status = taskQueue.rootDir + localPath;
	}


	/**
	 * This function check validity of request and prepare to respond
	 */
	public void parseAndReply() throws IOException, InterruptedException{

		/* HW1MS1 */
		if(method.equals("GET") || method.equals("HEAD") || method.equals("POST")){

			/* Request Type2: Control (Dump Thread) */
			if(localPath.equals("/control")){
				dump("Dumping!");
				respBody.setLength(0);
				respBody.append( "<html><body><br>");
				respBody.append( "Control Panel | Yayang Tian | yaytian<br><hr>");
				for(Worker w : Dispatcher.getWorkers()){
					respBody.append(w.workerInfo()).append("<br>");
				}
				respBody.append("<html><body>");
				respBody.append("<a href = \"shutdown\"> Shutdown </a><br>");
				respBody.append("<a href = \"ErrorLog.html\"> error log </a><br>");
				respBody.append("</body></html>");
				sendResponse(200, true);
			}

			/* Request Type3: Shutdown */
			else if(localPath.equals("/shutdown")){
				dump("Shutdown!");
				status = "Waiting";
				Dispatcher.closeServer();
				throw new InterruptedException();
			}

			/* Request Type3: Invoke Servlet or Get Files */
			else{

				// Check HTTP 1.1: MUST use "100 Continue" & contain "host" header
				if(version.equals("HTTP/1.1")){
					sendStatus(100);      				// 100 Continue
					if(!reqHead.containsKey("host")){
						sendResponse(400, false);  		// 400 Bad Request
					}
				}else if(!version.equals("HTTP/1.0")){
					sendResponse(501, false);			// 501 Not Implemented
				}

				/*******************
				 * HW1MS2: Servlet *
				 *******************/

				// ParseRequestUri
				if(localPath.endsWith("/"))
					localPath = localPath.substring(0, (localPath.length() - 1));

				String reqUri = localPath;

				if(method.equals("POST")){
					String temp = reqBody.toString().replaceAll("\\s","");
					if(temp.contains("=")){
						postString = temp;
					}

					dump("reqUri", temp);

				}


				// trim query string first
				if(reqUri.contains("?")){
					String[] parts = localPath.split("\\?");
					if(parts.length == 2){
						reqUri = parts[0];
						queryString = parts[1];
					}
					dump("reqUri", reqUri);
					dump("queryStr", queryString);


				}
				// parse requestUri into servletPath and pathInfo
				parseRequestUri(reqUri);

				if(servlet != null){

					//					sendStatus(200);
					//sendHead(false);
					dump("Servlet Running");
					invokeServlet();


				}


				/*******************
				 * Back to HW1MS1  *
				 *******************/

				file = new File(taskQueue.rootDir + localPath);
				// (1) if it's a directory, list them
				if(file.isDirectory()){
					String filelocalPath = "";
					respBody.setLength(0);
					if(localPath.equals("/"))   		 // if root is "/", delete "/"
						localPath = "";
					respBody.append("<html><body>");
					respBody.append("Folder " + localPath + "  contains: <br>");

					for(int i = 0; i < file.list().length; i++){
						filelocalPath = "http://" + clientSocket.getLocalAddress().getHostName() +
								":" + Dispatcher.getServerSocket().getLocalPort() +
								(localPath + "/" + file.list()[i]);
						respBody.append("<a href = \"");
						respBody.append(filelocalPath);
						respBody.append("\">");
						respBody.append(file.list()[i]);
						respBody.append("</a><br>");
					}
					respBody.append("</body></html>");
					sendResponse(200, true);
				}

				// (2) If it's a file, check validity
				if(file.isFile()){
					dump("fullPath", taskQueue.rootDir + localPath);
					if(!(file.getCanonicalPath() + "/").contains(taskQueue.rootDir)){
						dump("Security Defend! Outside root directory!");
						sendResponse(403, false);				// 403 Forbidden

					}else if(!file.exists() || !file.canRead()){
						dump("File not found!");dump(1);
						sendResponse(404, false);				// 404 Not Found

					}
					else if(reqHead.containsKey("dateReq")){
						// @HTTP 1.1: MUST check modified after otherwise is valid
						dump(2);
						String dateReq = reqHead.get("dateReq").replaceAll(".+: ", "");
						Date date = null;
						SimpleDateFormat parseDate = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
						try {
							date = parseDate.parse(dateReq);
						} catch (ParseException e1) {
							parseDate = new SimpleDateFormat("EEEEE, dd-MMM-yy HH:mm:ss zzz");
							try {
								date = parseDate.parse(dateReq);
							} catch (ParseException e2) {
								parseDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
								try {
									date = parseDate.parse(dateReq);
								} catch (ParseException e3) {
									System.out.println("Malformed data format");
								}
							}
						}
						if(date == null){
							sendResponse(400, false);
						}else{
							if(dateReq.startsWith("If-Modified-Since: ")){
								//@HTTP 1.1: response format
								if(!(date.getTime() < file.lastModified())){
									sendResponse(304, false);
								}
							}else{
								//@HTTP 1.1: response format
								if(!(date.getTime() > file.lastModified())){
									sendResponse(412, false);
								}
							}
						}
					}
					// all valid. send files.
					if(method.equals("GET"))sendResponse(200, true);
					else if(method.equals("HEAD"))sendResponse(200, false);

				}//end isFile
			}//end getFile
		}//end if GET/HEAD

		/* Only implement GET & HEAD */
		else if(method.equals("DELETE") || method.equals("OPTIONS") ||
				method.equals("TRACE")){
			sendResponse(501, false);							// 501 Not Implemented
		}
		/* Not recognized method */
		else{
			sendResponse(400, false);
		}
	}


	/**
	 * This parse request Uri into context path, servlet path and path info
	 */
	private void parseRequestUri(String reqUri){

		// Get matched servlet from url Mappings
		HashMap<String, String> urlMap = Dispatcher.getUrlMappings();

		for(String urlPattern : (urlMap.keySet())){
			// Exact Matching
			if(reqUri.equals(urlPattern)){
				servletPath = reqUri;
				pathInfo = null;
				dump("Exact Matching");
				dump("url", urlPattern);
				String servletName = urlMap.get(urlPattern);
				dump("servletName", servletName);
				servlet = Dispatcher.getServlet(servletName);
			}

			// Path Matching
			if(urlPattern.contains("*")){
				String prefixWithSlash = Pattern.compile("\\*").split(urlPattern)[0];
				String prefix = prefixWithSlash.substring(0, prefixWithSlash.length() - 1);
				dump("prefix",prefix);
				if(reqUri.startsWith(prefix)){
					servletPath = prefix;
					pathInfo = reqUri.substring(servletPath.length(), reqUri.length());
					if(pathInfo == "")pathInfo = null;
					dump("Path Matching");
					dump("prefix", prefix);
					String servletName = urlMap.get(urlPattern);
					dump("servletName", servletName);
					servlet = Dispatcher.getServlet(servletName);
				}
			}
		}
		dump("servletPath", servletPath);
		dump("pathInfo", pathInfo);


	}


	/**
	 * This invokes servlet object
	 */
	private void invokeServlet() throws IOException{

		FakeSession session = null;
		/* check head */
		if(reqHead.containsKey("cookie")){

			String cookieString = reqHead.get("cookie").split(":")[1].trim();
			//			dump("cookieString", cookieString);
			String[] cookies = cookieString.split(";");
			for(String cookie : cookies){
				dump("cookie",	cookie);
				String key = cookie.split("=")[0].trim();
				String value = cookie.split("=")[1].trim();
				dump("key", key);
				dump("value", value);
				if(key.equalsIgnoreCase("JSESSIONID")){
					session = Dispatcher.getSession(value);
				}
			}
		}

		/* invalidate old session */
		if(session != null && session.isValid()){
			long now = System.currentTimeMillis();
			long gap = 0;
			if(!session.isNew())
				gap = now - session.getLastAccessedTime();
			else
				gap = now - session.getCreationTime();
			if (gap > 1000 * session.getMaxInactiveInterval()){
				//				session.invalidate();
			}
		}

		/*[HERE] Construct a HttpServletRequest  */
		FakeContext fc = (FakeContext)servlet.getServletContext();
		FakeRequest request = new FakeRequest(clientSocket,
				reqHead, fc,  session);
		request.setMethod(method);
		request.setProtocol(version);
		request.setServletPath(servletPath);
		request.setPathInfo(pathInfo);





		//Set params for query string
		if(queryString != null && !queryString.isEmpty()){
			String[] queries = queryString.split("\\&");
			for(String query : queries){
				String k = query.split("=")[0];
				String v = query.split("=")[1];
				request.setParameter(k, v);
			}
		}

		//Set params for post string
		if(postString != null && !postString.isEmpty()){
			String[] queries = queryString.split("\\&");
			for(String query : queries){
				String k = query.split("=")[0];
				String v = query.split("=")[1];
				request.setParameter(k, v);
			}
		}

		/*  [HERE] Construct Response */
		FakeResponse response  = new FakeResponse(request, outputStream);
		String serverName = servlet.getServletContext().getServerInfo();
		response.setHeader("Server", serverName);

		try {

			//////////Service Starts//////////////
			servlet.service(request, response);
			///////////////////////////////////////

			if(!response.isCommitted()){
				response.flushBuffer();
			}
			//response.bufWriter.close();

		} catch (ServletException e) {
logger.error("servlet error");
			e.printStackTrace();
			sendResponse(500, false);
		}

		in.close();
		out.flush();
		out.close();
		return;
	}

	/**
	 * This function reply to client in the final step
	 */
	public void sendResponse(int code, boolean withBody) throws IOException{

		sendStatus(code);

		sendHead(withBody);

		if(withBody){
			/* text */
			if(isText(localPath)){
				respBody.setLength(0);
				BufferedReader textReader = new BufferedReader(new FileReader(file));
				while(textReader.ready()){
					respBody.append((char)textReader.read());
				}
				out.write(respBody.toString());
				textReader.close();
			}
			/* picture */
			if(isPicture(localPath)){
				FileInputStream inStream = new FileInputStream(file);
				//DataOutputStream outStream = new DataOutputStream(clientSocket.getOutputStream());
				PrintStream outStream = new PrintStream(clientSocket.getOutputStream());
				byte[] buffer = new byte[1024];
				int bt;
				while((bt = inStream.read(buffer)) != -1){
					outStream.write(buffer, 0 , bt);
				}
				outStream.write("\r\n".getBytes());
				inStream.close();
				outStream.flush();
				outStream.close();
			}
			/* directory or /control */
			else{
				dump("respBody", respBody);
				out.write(respBody.toString());
			}
		}

		out.flush();
		//out.close();
		//if(clientSocket != null) clientSocket.close();
	}

	/**
	 * This function reply to client the Response#initial
	 */
	public void sendStatus(int code){
		String status;
		switch(code){
		case 100: status = version + " 100 Continue\r\n\r\n";
		break;
		case 200: status =  version + " 200 Continue\r\n";
		break;
		case 304: status = version + " 304 Not Modified\r\n";
		break;
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
		out.write(status);
	}


	public void sendHead(boolean withBody) throws IOException{
		respHead.setLength(0);
		respHead.append("Data: " + getGMTDate(new Date())).append("\r\n");

		if(withBody || method.equals("HEAD")){
			String type = getType(localPath);
			long length;
			dump("fdfdf", localPath);
			if(localPath.equals("/control") || (file!=null && file.isDirectory())){
				length = respBody.length();
			}else{
				//
				length = file.length();
				//			Date lastModified;
				//			lastModified = new Date(file.lastModified());
				//			respHead.append("Last-Modified: ").append(getGMTDate(lastModified)).append("\r\n");
			}
			respHead.append("Content-type: ").append(type).append("\r\n");
			respHead.append("Content-length: ").append(length).append("\r\n");
		}

		respHead.append("Host: YayangServer\r\n");
		respHead.append("Connection: close\r\n\r\n");// if HTTP 1.1 not persistent control
		dump("respHead", respHead);
		out.write(respHead.toString());
	}




	/**
	 * These are auxiliary functions
	 */
	private String getType(String localpath) {
		String type = "text/html";
		if (localpath.endsWith(".jpg") || localpath.endsWith(".jpeg"))
			type = "image/jpg";
		else if (localpath.endsWith(".gif"))
			type = "image/gif";
		else if (localpath.endsWith(".png"))
			type = "image/png";
		else if (localpath.endsWith(".txt"))
			type = "text/plain";
		else if (localpath.endsWith(".html") || localpath.endsWith(".htm"))
			type = "text/html";
		return type;
	}

	public String getGMTDate(Date date){
		// @HTTP 1.1: MUST include today'date time stamp in header
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyy HH:mm:ss zzz");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(date);

	}

	public synchronized void stopThread(){
		stopped = true;
		this.interrupt();
	}

	public String workerInfo(){
		return this.getName() + '\t' + status + '\n';
	}

	public boolean isPicture(String path){
		return  path.endsWith("jpeg") ||
				path.endsWith("jpg") ||
				path.endsWith("png") ||
				path.endsWith("gif");
	}
	public boolean isText(String path){
		return  path.endsWith("txt") ||
				path.endsWith("html");
	}

	private static void dump(Object comment, Object content){
		if(dumpDetail == true){
			System.out.print("[ "+ String.valueOf(comment) + " ]" + "\t");
			System.out.println(String.valueOf(content));
		}
	}
	private static void dump(Object item){
		System.out.println("\n/*** "+ String.valueOf(item) + "***/");
	}

}
