import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.nio.charset.Charset;
import java.net.*;

public class ServerCache{

  //Check the cache for a site
  public boolean checkCache(String siteName){
    boolean result = false;
    try{
      File filePath = new File("cache/"+siteName+".html");
      result = filePath.exists();
    }catch(Exception e){
      System.out.println("Problem checking the cache");
    }
    return result;
  }

  //Cache the site on server
  public boolean cacheSite(String siteName, String destination){
    boolean result = false;
    String fileName = "cache/"+siteName+".html";
    PrintWriter writer = null;
    BufferedReader br = null;

    //Download the file and write it to a html file in the cache
    try{
      writer = new PrintWriter(fileName, "UTF-8");

      URL dest = new URL(destination);
      HttpURLConnection con = (HttpURLConnection)dest.openConnection();
      con.usingProxy();
      br =	new BufferedReader(new InputStreamReader(con.getInputStream()));
      String input;
      writer.println("<!-- This is a cached version of the website -->");

 	    while ((input = br.readLine()) != null){
 	       writer.print(input);
 	    }
      br.close();
      writer.close();

    }catch(Exception e){
      //If caching the file failed, remove the file that was created
      try{
        writer.close();
        Files.delete(Paths.get(fileName));
        br.close();
      }catch(Exception except){}
    }
    return result;
  }
}
