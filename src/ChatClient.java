/*
 * COEN 317 Project
 */

import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class ChatClient {
    
    private ObjectInputStream sInput; //read from socket
    private ObjectOutputStream sOutput; //write to socket
    private Socket socket;

    private ClientGUI cg;

    private UserId self;


    // the server, the port and the username
    private String server, username;
    private int port;

    /*
     *  Constructor called by console mode
     *  server: the server address
     *  port: the port number
     *  username: the username
     */
    ChatClient(String server, int port, String username) {
            // which calls the common constructor with the GUI set to null
            this(server, port, username, null);
    }

    /*
     * Constructor call when used from a GUI
     * in console mode the ClienGUI parameter is null
     */
    
    ChatClient(String server, int port, String username, ClientGUI cg) {
            this.server = server;
            this.port = port;
            this.username = username;
            // save if we are in GUI mode or not
            this.cg = cg;
    }

    /*
     * To start the dialog
     */
    public boolean start() {
        // try to connect to the server
        try {
            socket = new Socket(server, port);
        } 
        // if it failed not much I can so
        catch(Exception ec) {
            display("Error connectiong to server:" + ec);
            return false;
        }

        String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
        display(msg);

        /* Creating both Data Stream */
        try {
            sInput  = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException eIO) {
            display("Exception creating new Input/output Streams: " + eIO);
            return false;
        }

        // creates the Thread to listen from the server 
        new ListenFromServer().start();
        // Send our username to the server this is the only message that we
        // will send as a String. All other messages will be ChatMessage objects
        try {
            sOutput.writeObject(username);
        } catch (IOException eIO) {
            display("Exception doing login : " + eIO);
            disconnect();
            return false;
        }
        
        // success we inform the caller that it worked
        return true;
    }

    /*
     * To send a message to the console or the GUI
     */
    private void display(ChatMessage cMsg) {
        if(cg == null)
            System.out.println(cMsg.getMessage()); // println in console mode
        else
            cg.append(cMsg); // append to the ClientGUI JTextArea (or whatever)
    }
    private void display(String s) {
        if(cg == null)
            System.out.println(s); // println in console mode
        else
            cg.append(s); // append to the ClientGUI JTextArea (or whatever)

    }
	
    /*
     * To send a message to the server
     */
    void sendMessage(ChatMessage msg) {
        System.out.println("Sending: " + msg.getMessage());
        if(msg.getSender() != null)
            System.out.println("Sender info: " + msg.getSender().getName() + " " + msg.getSender().getId());
        try {
            sOutput.writeObject(msg);
        } catch(IOException e) {
            display("Exception writing to server: " + e);
        }
    }

    void setSelf(UserId self) {
        this.self = self;
    }

    UserId getSelf(){
        return self;
    }


    /*
     * When something goes wrong
     * Close the Input/Output streams and disconnect not much to do in the catch clause
     */
    private void disconnect() {
        try { 
                if(sInput != null) sInput.close();
        } catch(Exception e) {} // not much else I can do

        try {
            if(sOutput != null) sOutput.close();
        } catch(Exception e) {} // not much else I can do

        try{
            if(socket != null) socket.close();
        } catch(Exception e) {} // not much else I can do

        // inform the GUI
        //if(cg != null)
        //	cg.connectionFailed();

    }

    /*
     * To start the Client in console mode use one of the following command
     * > java Client
     * > java Client username
     * > java Client username portNumber
     * > java Client username portNumber serverAddress
     * at the console prompt
     * If the portNumber is not specified 1500 is used
     * If the serverAddress is not specified "localHost" is used
     * If the username is not specified "Anonymous" is used
     * > java Client 
     * is equivalent to
     * > java Client Anonymous 1500 localhost 
     * are eqquivalent
     * 
     * In console mode, if an error occurs the program simply stops
     * when a GUI id used, the GUI is informed of the disconnection
     */
        
    public static void main(String[] args) {
        // default values
        int portNumber = 1500;
        String serverAddress = "localhost";
        String userName = "Anonymous";

        // depending of the number of arguments provided we fall through
        switch(args.length) {
            // > javac Client username portNumber serverAddr
            case 3:
                serverAddress = args[2];
            // > javac Client username portNumber
            case 2:
                try {
                        portNumber = Integer.parseInt(args[1]);
                } catch(Exception e) {
                        System.out.println("Invalid port number.");
                        System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
                        return;
                }

            // > javac Client username
            case 1: 
                userName = args[0];
            // > java Client
            case 0:
                break;
            // invalid number of arguments
            default:
                System.out.println("Usage is: > java Client [username] [portNumber] {serverAddress]");
            return;
        }

        // create the Client object
        ChatClient client = new ChatClient(serverAddress, portNumber, userName);
        // test if we can start the connection to the Server
        // if it failed nothing we can do
        if(!client.start())
            return;

        // wait for messages from user
        Scanner scan = new Scanner(System.in);

        // loop forever for message from the user
        while(true) {
            System.out.print("> ");
            // read message from user
            String msg = scan.nextLine();
            // logout if message is LOGOUT
            if(msg.equalsIgnoreCase("LOGOUT")) {
                client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, "", client.getSelf()));
                // break to do the disconnect
                break;
            } else if(msg.equalsIgnoreCase("WHOISIN")) {
                // message WhoIsIn
                client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, "", client.getSelf()));
            } else {
                // default to ordinary message
                client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg, client.getSelf()));
            }
        }
        
        client.disconnect();
    }


    /*
     * a class that waits for the message from the server and append them to the JTextArea
     * if we have a GUI or simply System.out.println() it in console mode
     */
    class ListenFromServer extends Thread {
        int flag = 0;
        ArrayList<UserId> users = new ArrayList<>();
        @Override
        public void run() {
            while(true) {
                try {
                    ChatMessage cMsg = (ChatMessage) sInput.readObject();
                    // if console mode print the message and add back the prompt
                    if(cMsg.getType() == ChatMessage.MESSAGE) {
                        System.out.println("Received a MESSAGE");
                        if(flag == 1) {
                            flag = 0;
                            for(int i = 0; i < users.size(); i++)
                                System.out.println(users.get(i).getName());
                            cg.updateList(users);
                        }
                        display(cMsg);
                    }
                    else if(cMsg.getType() == ChatMessage.WHOISIN)
                    {
                        if(flag == 0) {
                            System.out.println("Clear userlist in Client");
                            users.clear();
                        }
                        flag = 1;
                        System.out.println("Received a WHOISIN");
                        if(!cMsg.isYou) {
                            System.out.println("Added to userlist");
                            users.add(new UserId(cMsg.getUserID(), cMsg.getMessage()));
                        }
                        else {
                            setSelf(new UserId(cMsg.getUserID(), cMsg.getMessage()));
                        }
                        for(int i = 0; i < users.size(); i++)
                            System.out.println(users.get(i).getName());
                    }
                    else if(cMsg.getType() == ChatMessage.FILE) {
                        FileReceiveGUI fileWindow = new FileReceiveGUI(cg, cMsg);
                    }
                    /*
                    if(cg == null) {
                            System.out.println(msg);
                            System.out.print("> ");
                    } else {
                            cg.append(msg);
                    }
                    */
                }
                catch(IOException e) {
                    display("Server has close the connection: " + e);
                    if(cg != null) 
                            cg.connectionFailed();
                    break;
                } catch(ClassNotFoundException e) {
                }
            }
        }
    }
}