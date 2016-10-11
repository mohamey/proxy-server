import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.cert.Certificate;

public class HttpRequest{
  private Socket clientSock;
  String method, destination, protocol, host, userAgent, acceptConditions, acceptLanguage, acceptEncoding, cookie, connectionType, contentLength, contentType, cacheControl, proxyConnection, refererDetails, modified;
  ProxyServerSocket server;
  boolean secure;
  boolean denied;
  DataOutputStream out;
  ServerCache cache;

  //HTTPRequest Constructer with client socket
  public HttpRequest(Socket socket, ProxyServerSocket s){
    try{
      this.clientSock = socket;
      out = new DataOutputStream(clientSock.getOutputStream());
      cache = new ServerCache();
      this.server = s;
    }catch(Exception e){}
  }

  //Check if the destination URL being accessed is the index file of the server
  private boolean isRootFile(){
    boolean result = false;
    int destIndex = this.destination.indexOf("/")+2;
    String modifiedDest = destination.substring(destIndex, destination.length()-1);
    return (host.equals(modifiedDest));
  }

  //Check if we have the minimum requirements to send a HTTP request
  private boolean checkMinRequirements(){
    return ((this.method!=null) && (this.destination!=null) && (this.host != null) &&
            (this.userAgent != null));
  }

  //Print details of this objets traffic
  private void printRequestDetails(){
    String clientAddress = clientSock.getInetAddress().toString();
    int remotePort = clientSock.getPort();
    System.out.println(this.method+" request from "+clientAddress + ":"+remotePort+" to: "+this.destination);

  }

  //Set the headers for the HTTP Request
  private void setHeaders(HttpURLConnection con){
    String last = "";
    try{
      con.setRequestProperty("Host", host);
      con.setRequestProperty("User-Agent", userAgent);

      if(acceptLanguage != null){
        con.setRequestProperty("Accept-Language", acceptLanguage);
      }
      if(acceptEncoding != null){
        con.setRequestProperty("Accept-Encoding", acceptEncoding);
      }
      if(this.acceptConditions != null){
        con.setRequestProperty("Accept", acceptConditions);
      }
      if(this.cookie != null){
        con.setRequestProperty("Cookie", cookie);
      }
      if(this.connectionType != null){
        con.setRequestProperty("Connection", connectionType);
      }
      if(this.contentLength != null){
        con.setRequestProperty("Content-Length", contentLength);
      }
      if(this.contentType != null){
        con.setRequestProperty("Content-Type", contentType);
      }
      if(this.cacheControl != null){
        con.setRequestProperty("Cache-Control", cacheControl);
      }
      if(this.refererDetails != null){
        con.setRequestProperty("Referer", refererDetails);
      }
      if(this.proxyConnection != null){
        con.setRequestProperty("Proxy-Connection", proxyConnection);
      }
    }catch(Exception e){}
  }

  //Send a HTTPS request
  private void sendSecureRequest(URL dest){
    try{
      if(server.checkBlockList(this.host)){
        serveDenied();
      }else{
        HttpsURLConnection con = (HttpsURLConnection) dest.openConnection();
        con.usingProxy();
        int responseCode = con.getResponseCode();
        respondToClient(con);
      }
    }catch(Exception e){
      //If an error occurs, attempt to close the client socket
      try{
        clientSock.close();
      }catch(Exception temp){}
    }
  }

  //Send a HTTP Request
  private void sendUnsecureRequest(URL dest){
    try{
      if(server.checkBlockList(this.host)){
        serveDenied();
      }else{
        HttpURLConnection con = (HttpURLConnection) dest.openConnection();
        con.setRequestMethod(this.method);
        setHeaders(con);
        con.usingProxy();
        //System.out.println("Sending Secure Request");
        int responseCode = con.getResponseCode();
        //System.out.println("Server response was "+responseCode);
        respondToClient(con);
      }
    }catch(Exception e){
      try{
        clientSock.close();
      }catch(Exception temp){}
    }
  }

  //Serve the blocked website page
  private void serveDenied(){
    try{
      File file = new File("denied.html");
      FileInputStream in = new FileInputStream(file);
      byte[] buffer = new byte[(int)file.length()];
      in.read(buffer);
      out.write(buffer);
      out.flush();
      in.close();
      out.close();
      clientSock.close();
      denied = false;
    }catch(Exception e){}
  }

  //Send remote server response to client
  private void respondToClient(HttpURLConnection con){
    boolean cached = false;
    try{
      //If the destination is a website index file and has not been cached, cache it them
      //serve the webpage
      if(isRootFile() && (!cache.checkCache(this.host))){
        cached = cache.cacheSite(this.host, this.destination);
        System.out.println("Wrote file to cache");
        if(cached){cached = serveCachedFile();}
      }
      //If destination is website index file and in cache, serve it
      else if(isRootFile() && cache.checkCache(this.host)){
        cached = serveCachedFile();
      }
      //Just load destination and send to client
      if(!cached){
        byte[] buffer = new byte[4096];
        int n = -1;
        DataInputStream in = new DataInputStream(con.getInputStream());
        while((n = in.read(buffer))!=-1){
          out.write(buffer, 0, n);
        }
        out.flush();
        out.close();
        in.close();
        printRequestDetails();
        clientSock.close();
      }
    }catch(Exception e){}
  }

  //Serve the cached version of the website
  private boolean serveCachedFile(){
    boolean served = false;
    try{
      String path = "cache/"+this.host+".html";
      File file = new File(path);
      FileInputStream in = new FileInputStream(file);
      byte[] buffer = new byte[(int)file.length()];
      in.read(buffer);
      out.write(buffer);
      out.flush();
      in.close();
      out.close();
      clientSock.close();
      served = true;
    }catch(Exception e){
      served = false;
    }
    return served;
  }

  //Process this object in preparation for a HTTP request
  private void processRequest(){
    try{
      URL dest = new URL(destination);
      if(this.secure){
        sendSecureRequest(dest);
      }else{
        sendUnsecureRequest(dest);
      }
    }catch(Exception e){}
  }

  //Parse the header received from the client
  void parseHeader(String segment){
    String [] segments = segment.split(" ");
    switch(segments[0]){
      case "GET":
        this.method = "GET";
        this.secure = false;
        this.destination = segments[1];
        this.protocol = segments[2];
        break;
      case "POST":
        this.method = "POST";
        this.secure = false;
        this.destination = segments[1];
        this.protocol = segments[2];
        break;
      case "CONNECT":
        this.method = "CONNECT";
        this.secure = true;
        String [] httpsSegments = segments[1].split(":");
        //System.out.println("SECURE DESTINATION IS: "+httpsSegments[0]);
        this.destination = "https://"+httpsSegments[0];
        this.protocol = segments[2];
      case "Host:":
        this.host = segments[1];
        break;
      case "User-Agent:":
        this.userAgent = segment.substring(12);
        break;
      case "Accept:":
        this.acceptConditions = segment.substring(8);
        break;
      case "Accept-Language:":
        this.acceptLanguage = segment.substring(17);
        break;
      case "Accept-Encoding:":
        this.acceptEncoding = segment.substring(17);
        break;
      case "Cookie:":
        this.cookie = segment.substring(8);
        break;
      case "Connection:":
        this.connectionType = segments[1];
        break;
      case "Content-Length:":
        this.contentLength = segments[1];
        break;
      case "Content-Type:":
        this.contentType = segments[1];
        break;
      case "Cache-Control:":
        this.cacheControl = segments[1];
        break;
      case "Proxy-Connection:":
        this.proxyConnection = segments[1];
        break;
      case "Referer:":
        this.refererDetails = segments[1];
        break;
      case "Last-Modified:":
        this.modified = segment.substring(15);
        break;
    }
    try{
      if(checkMinRequirements()){
        processRequest();
      }
    }catch(Exception e){}

  }
}
