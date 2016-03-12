/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;


/*
 * The Client with its GUI
 */
public class ClientGUI extends JFrame implements ActionListener {
    
    private static final long serialVersionUID = 1L;
    // will first hold "Username:", later on "Enter message"
    private JLabel label;
    // JTextField to hold the Username and later on the messages
    private JTextField tf;
    // to hold the server address and the port number
    private JTextField tfServer, tfPort;
    //GUI list to display logged in users (minus this user)
    private JList userList;
    private DefaultListModel listModel;
    //Buttons for logging in, chatting, logging out, and finding out active users
    private JButton login, chat, logout, whoIsIn, crash;
    // for the chat room
    private JTextArea ta;
    // if it is for connection
    private boolean connected;
    // the Client object
    private ChatClient client;
    // the default port number and the default Hostname
    private int defaultPort;
    private String defaultHost;

    //Arraylist of all active users
    ArrayList<UserId> allUsers;
    //ArrayList of all active chat windows
    //ArrayList<ChatGUI> allChats;
    ArrayList<Chatroom> allRooms;


    // Constructor connection receiving a socket number
    ClientGUI(String host, int port) {

        super("Chat Client");
        defaultPort = port;
        defaultHost = host;

        //allChats = new ArrayList<>();
        allRooms = new ArrayList<>();

        // The NorthPanel with:
        JPanel northPanel = new JPanel(new GridLayout(3,1));
        // the server name and the port number
        JPanel serverAndPort = new JPanel(new GridLayout(1,5, 1, 3));
        // the two JTextField with default value for server address and port number
        tfServer = new JTextField(host);
        tfPort = new JTextField("" + port);
        tfPort.setHorizontalAlignment(SwingConstants.RIGHT);

        serverAndPort.add(new JLabel("Server Address:  "));
        serverAndPort.add(tfServer);
        serverAndPort.add(new JLabel("Port Number:  "));
        serverAndPort.add(tfPort);
        serverAndPort.add(new JLabel(""));
        // adds the Server an port field to the GUI
        northPanel.add(serverAndPort);

        // the Label and the TextField
        label = new JLabel("Enter your username below", SwingConstants.CENTER);
        northPanel.add(label);
        tf = new JTextField("Anonymous");
        tf.setBackground(Color.WHITE);
        northPanel.add(tf);
        add(northPanel, BorderLayout.NORTH);


        //Create a new list model allowing for a dynamically modified list
        listModel = new DefaultListModel();
        userList = new JList(listModel);
        //Allows user to select multiple users from the list
        userList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listModel.addElement("User List");
        listModel.addElement("To update user list click the WHOISIN button");
        listModel.addElement("To select multiple users hold down ctrl");



        // The CenterPanel which is the chat room
        ta = new JTextArea("Welcome to the Chat room\n", 80, 80);
        JPanel centerPanel = new JPanel(new GridLayout(2,1));
        //centerPanel.add(activeUsers);
        centerPanel.add(new JScrollPane(userList));
        centerPanel.add(new JScrollPane(ta));
        ta.setEditable(false);
        add(centerPanel, BorderLayout.CENTER);

        // the 3 buttons
        login = new JButton("Login");
        login.addActionListener(this);
        chat = new JButton("Chat");
        chat.addActionListener(this);
        chat.setEnabled(false);
        logout = new JButton("Logout");
        logout.addActionListener(this);
        logout.setEnabled(false);		// you have to login before being able to logout
        whoIsIn = new JButton("Who is in");
        whoIsIn.addActionListener(this);
        whoIsIn.setEnabled(false);		// you have to login before being able to Who is in
        crash = new JButton("Crash");
        crash.addActionListener(this);

        JPanel southPanel = new JPanel();
        southPanel.add(login);
        southPanel.add(chat);
        southPanel.add(logout);
        southPanel.add(whoIsIn);
        //southPanel.add(crash);
        add(southPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(350, 500);
        setVisible(true);
        tf.requestFocus();

        allUsers = new ArrayList<>();

    }

    void saveHistory(UserHistory history){
        append("Saving history. Size: " + history.getChatrooms().size());

        /*
        for(Chatroom cr: allRooms) {
            history.newChatroom(cr.getUsers(), cr.messages);
        }
        */
        allRooms = new ArrayList<>();
        ArrayList<UserHistory.Chat> chats = history.getChatrooms();
        for(UserHistory.Chat chat: chats){
            allRooms.add(new Chatroom(chat.recipients, chat.messages, this));
           append(chat.recipients.get(0).getName());
        }


    }

    void sendMessage(ArrayList<UserId> users, String message) {
        client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, message, users, client.getSelf(), new Date()));
    }

    void sendNull(){
        client.sendMessage(new ChatMessage(10, "", client.getSelf(), new Date()));
    }


    // called by the Client to append text in the TextArea 
    void append(ChatMessage cMsg) {
        ta.append(cMsg.getMessage() + "\n");
        ta.setCaretPosition(ta.getText().length() - 1);
        //TODO: Only works with WHOISIN, server messages MAYBE manage list better?
        /*
        if(cMsg.getRecipients().size()==0){
            System.out.println("Message from server");
            return;
        }
        */
        if(cMsg.getSender().getId() == 0) {
            System.out.println("Message from server");
            return;
        }

        for(int i = 0; i< allRooms.size(); i++) {
            ArrayList<UserId> users = allRooms.get(i).getUsers();
            //if(users.equals(cMsg.getRecipients()));
            if(users.size() == cMsg.getRecipients().size()
                    && users.containsAll(cMsg.getRecipients())
                    && cMsg.getRecipients().containsAll(users)) {
                System.out.println("The ChatWindow is already open!!");
                allRooms.get(i).newMessage(cMsg);
                return;
            }
        }
        Chatroom cr = openWindow(cMsg.getRecipients());
        cr.newMessage(cMsg);

        /*
        ChatGUI frame = openWindow(cMsg.getRecipients());
        frame.append(cMsg.getMessage());
        */

    }
    void append(String s) {
        ta.append(s + "\n");
        ta.setCaretPosition(ta.getText().length() - 1);
    }
    //Update list of users
    void updateList(ArrayList<UserId> users) {
        System.out.println("Updating the list...");
        listModel.removeAllElements();
        allUsers = users;
        for(int i=0; i<allUsers.size(); i++){
            System.out.println("Adding to GUI List: " + allUsers.get(i).getName());
            listModel.addElement(allUsers.get(i).getName());
        }
        for(int i= 0; i < listModel.size(); i++)
            System.out.println(listModel.get(i));
        userList.repaint();
        userList.setVisible(true);
    }

    Chatroom openWindow(ArrayList<UserId> selectedUsers){
        /*
        ChatGUI frame = new ChatGUI(selectedUsers, null, this);
        frame.setVisible(true);
        allChats.add(frame);
        return frame;
        */
        for(int i = 0; i< allRooms.size(); i++) {
            ArrayList<UserId> users = allRooms.get(i).getUsers();
            //if(users.equals(cMsg.getRecipients()));
            if(users.size() == selectedUsers.size()
                    && users.containsAll(selectedUsers)
                    && selectedUsers.containsAll(users)) {
                System.out.println("The ChatWindow is already open!!");
                allRooms.get(i).openGUI();
                return allRooms.get(i);
            }
        }

        Chatroom room = new Chatroom(selectedUsers, null, this);
        allRooms.add(room);
        room.openGUI();
        return room;
    }

    /*
    //Remove ChatWindow from list when ChatGUI is closed
    void closeChat(ChatGUI room) {
        System.out.println("Removing ChatGUI from list");
        allChats.remove(room);

    }
    */

    void fileTransferStart(long length, String filename, ArrayList<UserId> recipients){
        client.sendMessage(new ChatMessage(ChatMessage.FILE, ChatMessage.FILESEND, length, filename, recipients, client.getSelf()));
    }

    //Send file deny message to server
    void denyFileTransfer(int transferId) {
        client.sendMessage(new ChatMessage(ChatMessage.FILE, ChatMessage.FILEDENY, transferId, 0, "", client.getSelf()));

    }

    //Send file accept message to server
    void acceptFileTransfer(int transferId) {
        client.sendMessage(new ChatMessage(ChatMessage.FILE, ChatMessage.FILEACCEPT, transferId, 0, "", client.getSelf() ));
    }

    Socket getSocket(){
        return client.getSocket();
    }

    ObjectOutputStream getOOS(){
        return client.getOOS();
    }

    UserId getSelf(){
        return client.getSelf();
    }


    void removeTransferRequest(FileReceiveGUI ended) {
        client.removeTransferRequest(ended);
    }

    // called by the GUI is the connection failed
    // we reset our buttons, label, textfield
    void connectionFailed() {
        login.setEnabled(true);
        chat.setEnabled(false);
        logout.setEnabled(false);
        whoIsIn.setEnabled(false);
        label.setText("Enter your username below");
        tf.setText("Anonymous");
        // reset port number and host name as a construction time
        tfPort.setText("" + defaultPort);
        tfServer.setText(defaultHost);
        // let the user change them
        tfServer.setEditable(false);
        tfPort.setEditable(false);
        // don't react to a <CR> after the username
        tf.removeActionListener(this);
        connected = false;
    }

    /*
    * Button or JTextField clicked
    */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        // if it is the Logout button
        if(o == chat) {
            int[] selected = userList.getSelectedIndices();
            ArrayList<UserId> selectedUsers = new ArrayList<>();
            for(int i=0; i < selected.length; i++)
                selectedUsers.add(allUsers.get(selected[i]));
            if(selectedUsers.size() == 0) {
                JOptionPane.showMessageDialog(new JFrame()
                        , "No users selected.\nPlease select one or more users."
                        , "Chat Room Error"
                        , JOptionPane.ERROR_MESSAGE);
            }
            else {
                openWindow(selectedUsers);
            }
            return;
        }
        if(o == logout) {
            UserHistory history = new UserHistory(client.getSelf().getName());
            for(int i = 0; i< allRooms.size(); i++) {
                ArrayList<UserId> users = allRooms.get(i).getUsers();
                history.newChatroom(users, allRooms.get(i).messages);
                System.out.println("Saving a chatroom");
            }
            System.out.println("Sending history from: " + history.getUsername() + " of size: " + history.getChatrooms().size());
            client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, "", client.getSelf(), new Date()));
            client.sendHistory(history);
            return;
        }
        // if it the who is in button
        if(o == whoIsIn) {
            client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, "", client.getSelf(), new Date()));
            return;
        }

        if(o == crash) {
            client.crash();
        }

        //a broadcast message
        if(connected) {
            client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, tf.getText(), client.getSelf(), new Date()));
            tf.setText("");
            return;
        }


        if(o == login) {
            // ok it is a connection request
            String username = tf.getText().trim();
            // empty username ignore it
            if(username.length() == 0)
                return;
            
            // empty serverAddress ignore it
            String server = tfServer.getText().trim();
            if(server.length() == 0)
                return;
            
            // empty or invalid port numer, ignore it
            String portNumber = tfPort.getText().trim();
            if(portNumber.length() == 0)
                return;
            
            int port = 0;
            
            try {
                port = Integer.parseInt(portNumber);
            } catch(Exception en) {
                return;   // nothing I can do if port number is not valid
            }

            // try creating a new Client with GUI
            client = new ChatClient(server, port, username, this);
            // test if we can start the Client
            if(!client.start()) 
                return;
            
            tf.setText("");
            label.setText("Enter your broadcast message below");
            connected = true;
            this.setTitle(username);

            // disable login button
            login.setEnabled(false);
            // enable the 3 buttons
            chat.setEnabled(true);
            logout.setEnabled(true);
            whoIsIn.setEnabled(true);
            // disable the Server and Port JTextField
            tfServer.setEditable(false);
            tfPort.setEditable(false);
            // Action listener for when the user enter a message
            tf.addActionListener(this);
        }

    }

    // to start the whole thing the server
    public static void main(String[] args) {
        new ClientGUI("localhost", 8080);
    }

}

