import java.util.*;
import java.net.*;
import java.io.*;

public class ClientConnectionHandler extends Thread{
  private Socket clientSock;
  private ProxyServerSocket server;
  boolean readIn = true;

  public ClientConnectionHandler(Socket client, ProxyServerSocket s){
    this.clientSock = client;
    this.server = s;
  }

  @Override
  public void run(){
    try{
      //Read input from client
      BufferedReader in = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
      HttpRequest builder = new HttpRequest(clientSock, server);
      String line;

      while((line = in.readLine()) != "" ){
        builder.parseHeader(line);
      }

      in.close();
    }catch(Exception e){}
  }
}
