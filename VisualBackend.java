import java.util.*;
import java.io.*;

public class VisualBackend{
  ProxyServerSocket server = null;

  //Parse input from the management console
  String parseInput(String input){
    String result;
    String command = input.toLowerCase();
    String [] expressions = command.split(" ");

    switch(expressions[0]){
      case("start"):
        //start the server
        try{
          int portNumber = Integer.parseInt(expressions[1]);
          server = new ProxyServerSocket(portNumber);
          server.startProxyServer();
          result = "Proxy server successfully started at port "+portNumber;
        }catch(Exception e){
          result = "Failed to start the proxy server at port "+expressions[1];
        }
        break;
      case("terminate"):
        //Terminate the server
        try{
          server.stopServer();
          server = null;
          result = "Successfully terminated the server";
        }catch(Exception e){
          result = "Unable to terminate the server";
        }
        break;
      case("block"):
        //Block a domain specified in expressions[1]
        try{
          if(server.blockDomain(expressions[1])){
            result = "Successfully blocked domain "+expressions[1];
          }else{
            result = "Was unable to block the domain "+expressions[1];
          }
        }catch(Exception e){
            result = "An error occured while trying to block the domain";
        }
        break;
      case("unblock"):
        //Unblock the domain represented by expressions[1]
        try{
          if(expressions[1].equals("all")){
            server.dumpBlockList();
            result = "Successfully unblocked all domains";
          }
          else if(server.unblockDomain(expressions[1])){
            result = "Successfully unblocked domain "+expressions[1];
          }else{
            result = "Was unable to block the domain "+expressions[1];
          }
        }catch(Exception e){
          result = "An error occured unblocking the domain";
        }
        break;
      case("show"):
      //Show the blacklist of domains.
      //Case is used here since I plan to extend functionality
        try{
          switch(expressions[1]){
            case("blocklist"):
              result = server.getBlockList();
              if(result.equals("")) result = "No Domains blocked";
              break;
            default:
              result = "Unable to show parameter \""+expressions[1]+"\"";
          }
        }catch(Exception e){
          result = "An error occured showing "+expressions[1];
        }
        break;
      default:
        result = "Sorry, unknown command \""+expressions[0]+"\"";
    }
    result = result+"\n";
    return result;
  }
}
