package edu.upenn.cis455.webserver;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * This class is the user interface 
 * @author Yayang Tian
 */
class HttpServer {
	
	static Dispatcher dispatcher = null;
	/**
	 * Start server, deploy threads, and communicate with clients
	 * 
	 * @param args[0] port
	 * @param args[1] rootDir
	 * @param args[2] xml location
	 * @see Distpacher#Dispatcher() 
	 */
	public static void main(String args[])
	{	
		/* your code here */
		if(args.length != 3){
			System.out.println("Yayang Tian\nyaytian");
			System.out.println(args.length);
		}else{
			int port = Integer.parseInt(args[0]);
			String rootDir = args[1];
			String webxml = args[2];
			try {

				/* create center station: thread pool implementation is encapsulated */ 
				dispatcher = new Dispatcher(port, rootDir, webxml);
				
				/* create handlers for processing services*/ 
				dispatcher.setupService();
				
				/* interact with clients requests */
				dispatcher.listenClient();

			} catch (IOException e) {
				System.out.println(e.getMessage());
				if(e.getMessage().equals("Address already in use")){
					System.out.println("Please change the port");
				}else{
					Dispatcher.closeServer();
					System.out.println("Server has been closed!");
				}
			} catch (SAXException e) {
				System.out.println(e.getMessage());
			} catch (ParserConfigurationException e) {
				System.out.println(e.getMessage());
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}
}
