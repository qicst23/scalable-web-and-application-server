Scalable Web and Application Server
===================================

1. Created an Apache Tomcat like web and application server that could run Java servlets and render dynamic web pages.
2. Tested on ApacheBench and successfully handled 50,000 requests with 1000 requests concurrently for images, style sheets, HTML pages, cookies and sessions.

## _Skills_
    
    Java, thread pool, servlet, HTTP 1.0/1.1, cookies, sessions

## _Course_
    
    Course: CIS555, Internet & Web Systems, Spring 2013
    
## _Deployment_
    
The source files are located in `src/edu/upenn/cis455/webserver` folder. To compile the program, do

    ant build

To start your server, in a terminal window run:
    
    java -cp [jar files path rendered by ant build]
             [HttpServer class path] [port to listen for connection to ]
             [root directory of the static web pages]
             [the web.xml location for URL-class mapping]

For example: 

    java –cp target/WEB-INF/lib/hw1.jar:target/WEB-INF/lib/servlet-api.jar
            edu.upenn.cis455.webserver.HttpServer 8080
            /home/cis455/workspace/HW1
            /home/cis455/workspace/HW1/conf/web.xml

The server should be running now. Try openning firefox and typing:
    
    http://localhost:8080/demo

It should take you to a page that says “Hello!”

Once you have this simple demo servlet working, you can try testing the other servlets that use cookies and sessions. 
For example, to test if you have your cookies working. try:    

    http://localhost:8080/cookie1

This will take you to a page where you can then click through the links to the cookie2 and cookie3
servlets.

To see how the server or thread pool works, go to [Dispatcher.java](src/edu/upenn/cis455/webserver/Dispatcher.java) then you will understand.
    
    
    
    


