import java.io.*;
import java.net.*;
import java.util.*;

public class ProxyServerSocket extends Thread{
  private ServerSocket serverSock;
  private int portNumber;
  private boolean running;
  public ArrayList<String> blocklist;

  public ProxyServerSocket(int port){
    this.portNumber = port;
  }

  //Check if a domain has been blocked
  public boolean checkBlockList(String domain){
    return blocklist.contains(domain);
  }

  //Block a domain from being accessed through the server
  public boolean blockDomain(String domain){
    boolean result = false;
    if(!checkBlockList(domain)){
      blocklist.add(domain);
      result = true;
    }
    return result;
  }

  //Unblock the domain from the server
  public boolean unblockDomain(String domain){
    boolean result = false;
    if(checkBlockList(domain)){
      blocklist.remove(domain);
      result = true;
    }
    return result;
  }

  //Unblock all the domains on the list
  public void dumpBlockList(){
    blocklist = new ArrayList<String>();
  }

  //Return all the domains on the server
  public String getBlockList(){
    String result = "";
    for(int i=1;i<blocklist.size();i++){
      result = result+blocklist.get(i)+"\n";
    }
    return result;
  }

  //Start the proxy server
  public void startProxyServer(){
    try{
      serverSock = new ServerSocket(portNumber);
      blocklist = new ArrayList<String>();
      blocklist.add("init");
      this.start();
    }catch(Exception e){}
  }

  //Stop the proxy server
  public void stopServer(){
    running = false;
    this.interrupt();
  }

  //Start the proxy server thread
  @Override
  public void run(){
    running = true;
    while(running){
      try{
        Socket clientSock = serverSock.accept();
        ClientConnectionHandler clientHandler = new ClientConnectionHandler(clientSock, this);
        clientHandler.start();
      }catch(Exception e){}
    }
  }
}
