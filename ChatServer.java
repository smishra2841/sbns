import java.io.*;
import java.net.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import static java.lang.System.out;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.LinkedHashMap;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.codec.binary.Base64;

public class ChatServer {

    Vector<String> users = new Vector<String>();
    Vector<HandleClient> clients = new Vector<HandleClient>();
    
    Map<String, HandleClient> map = new HashMap<String, HandleClient>();
    public void process() throws Exception {
        ServerSocket server = new ServerSocket(18524);
        InetAddress IP=InetAddress.getLocalHost();
        out.println("Server IP := "+IP.getHostAddress());


        out.println("Server Started...");
        out.println("Check Database....");
        out.println("Data Updated....");
        
        while (true) {
            Socket client = server.accept();
            //add incoming client to connected clients vector.
            HandleClient c = new HandleClient(client);
            clients.add(c);
            
            map.put(c.getUserName(),c);
            
        }  // end of while
    }

        public static void main(String[] args) throws Exception {
        new ChatServer().process();
    } // end of main

    public void broadcast(String user, String message) {
        // send message to all connected users
        for (HandleClient c : clients) {
            c.sendMessage(user, message);
        }
    }

    /*
     * Inner class, responsible of handling incoming clients.
     * Each connected client will set as it's own thread.
     */
    class HandleClient extends Thread {

        String name = "";//client name/username
        BufferedReader input;//get input from client
        PrintWriter output;//send output to client

        public HandleClient(Socket client) throws Exception {
            // get input and output streams
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            output = new PrintWriter(client.getOutputStream(), true);
            // read name
            name = input.readLine();
            users.add(name); // add to users vector
            //broadcast(name, " Has connected!");
            System.out.println(name + " Has connected!");
            start();
        }

        public void sendMessage(String uname, String msg) {
            //output.println(uname + ": " + msg);
            output.println(msg);
        }

        public void getOnlineUsers() {
            for (HandleClient c : clients) {
                for (int i = 0; i < users.size(); i++) {
                    broadcast("", users.get(i));
                    System.out.println("Users Online: " + users.get(i)+ "number of  i " + users.size());
                }
            }
        }

        public String getUserName() {
            return name;
        }

public void run() {
        String line;
        try {
            while (true) {
                line = input.readLine();
                System.out.println(getUserName() +": " + line);
                if (line.equals("!end")) {
                     // Blah
                        users.remove(name);
                        clients.remove(map.get(name));
                        
                        System.out.println(name + " : " + map.get(name));
                        map.remove(name);
                        System.out.println("Endded");
                        return;
                    } else if(StringUtils.contains(line,"!N_Picked")){

                    String userID = StringUtils.substringBetween(line,",",",");

                    if(users.contains(userID)){
                   	HandleClient cSend = (HandleClient) map.get(StringUtils.substringBetween(line,",",","));
                  	cSend.sendMessage(getUserName(),line);
                    }

                    System.out.println("Server: ID: " + StringUtils.substringBetween(line,",",",") + " is picked up , parents notified");



                     } else if(StringUtils.contains(line,"!N_Dropped")){
                    String userID = StringUtils.substringBetween(line,",",",");

                    if(users.contains(userID)){
                   	HandleClient cSend = (HandleClient) map.get(StringUtils.substringBetween(line,",",","));
                  	cSend.sendMessage(getUserName(),line);
                    }

                    System.out.println("Server: ID: " + StringUtils.substringBetween(line,",",",") + " is dropped off , parents notified");

                     } else if(StringUtils.contains(line,"!N_Close")){

                    String userID = StringUtils.substringBetween(line,",",",");

                    if(users.contains(userID)){
                   	HandleClient cSend = (HandleClient) map.get(StringUtils.substringBetween(line,",",","));
                  	cSend.sendMessage(getUserName(),line);
                    }

                    System.out.println("Server: ID: " + StringUtils.substringBetween(line,",",",") + " is close to kid location , parents notified");

                    }  else if (StringUtils.contains(line,"!N_SMS")){

                    String userID = StringUtils.substringBetween(line,",",",");
                    String kName = StringUtils.substringBetween(StringUtils.substringAfter(line,","),",","{");
                    String phoneNumber = StringUtils.substringBetween(line,"{","}");
                    String msg = StringUtils.substringAfter(line,":");
            
                    
                    System.out.println("SMS sent to parent ID: " + userID + " (" + kName + ") " + "PhoneNumber:" + phoneNumber + " Msg:" + msg);
                    sendSMS(phoneNumber,msg);
                    
                   }   else if (line.equals("!noPickUp")){

                    if(users.contains("bus")){
                	HandleClient cSend = (HandleClient) map.get("bus");
                  	cSend.sendMessage(getUserName(), "!noPickUp: ID: {" + getUserName() + "}" + " Requested: no pick up");
                	HandleClient SendToParent = (HandleClient) map.get(getUserName());
                	SendToParent.sendMessage(getUserName(), "!noPickUp sent");
                    System.out.println("!noPickUp sent to bus driver");
                  }
                }else{
            	broadcast(name,line);
                }
                //broadcast(name, line); // method  of outer class - send messages to all
            } // end of while
        } // try
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    } // end of run()

    } // end of inner class

    public static void sendSMS(String pNum, String msG){
              try {
            String phoneNumber = pNum;
            String appKey = "9963b516-3604-47b5-a916-e63c450f2e32";
            String appSecret = "YtRmmnulaUSGnvz+yGZyCA==";
            String message = msG;

            URL url = new URL("https://messagingapi.sinch.com/v1/sms/" + phoneNumber);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            String userCredentials = "application\\" + appKey + ":" + appSecret;
            byte[] encoded = Base64.encodeBase64(userCredentials.getBytes());
            String basicAuth = "Basic " + new String(encoded);
            connection.setRequestProperty("Authorization", basicAuth);

            String postData = "{\"Message\":\"" + message + "\"}";
            OutputStream os = connection.getOutputStream();
            os.write(postData.getBytes());

            StringBuilder response = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while ( (line = br.readLine()) != null)
                response.append(line);

            br.close();
            os.close();

            System.out.println(response.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

        }
} // end of Server


