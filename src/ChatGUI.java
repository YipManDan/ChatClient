/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/*
 * The Client with its GUI
 */
public class ChatGUI extends JFrame implements ActionListener, WindowListener{

    private static final long serialVersionUID = 1L;
    //Text field for user to enter message
    private JTextField tf;
    // for the chat room
    private JTextArea ta;

    private SimpleDateFormat sdf;

    // the Client object (The calling class)
    ClientGUI cg;

    private ArrayList<UserId> users;

    // Constructor connection receiving a socket number
    ChatGUI(ArrayList<UserId> users, ClientGUI cg) {

        super("Chat Box");

        this.users = users;
        this.cg = cg;

        //Runtime.getRuntime().addShutdownHook(new ClosingSequence(cg, this));

        sdf = new SimpleDateFormat("HH:mm:ss");

        String title = new String(cg.getTitle() + ">");
        for(int i = 0; i < users.size(); i++) {
            UserId user = users.get(i);
            title = title + user.getName() + " ";
        }
        this.setTitle(title);

        // The NorthPanel with:
        JPanel northPanel = new JPanel(new GridLayout(3,1));

        // the Label and the TextField
        northPanel.add(new JLabel("Enter your message below:", SwingConstants.CENTER));
        tf = new JTextField("");
        tf.setBackground(Color.WHITE);
        northPanel.add(tf);
        add(northPanel, BorderLayout.NORTH);

        // The CenterPanel which is the chat room
        ta = new JTextArea("Welcome to the Chat room\n", 80, 80);
        JPanel centerPanel = new JPanel(new GridLayout(1,1));
        centerPanel.add(new JScrollPane(ta));
        ta.setEditable(false);
        add(centerPanel, BorderLayout.CENTER);

        // the 3 buttons

        JPanel southPanel = new JPanel();
        add(southPanel, BorderLayout.SOUTH);

        //setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 600);
        setVisible(true);
        tf.addActionListener(this);
        tf.requestFocus();

        addWindowListener(this);

    }

    // called by the Client to append text in the TextArea 
    void append(String str) {
        ta.append(str + "\n");
        ta.setCaretPosition(ta.getText().length() - 1);
    }

    ArrayList<UserId> getUsers() {
        return users;
    }

    //JtextField is modified
    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        String time = sdf.format(new Date());

        // just have to send the message
        cg.sendMessage(users, tf.getText());
        append(time + ": " + "You: " + tf.getText());
        tf.setText("");
        return;
    }
    @Override
    public void windowClosing(WindowEvent e) {
        System.out.println("Closing Sequence");
        cg.closeChat(this);
    }

    public void windowClosed(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e){}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    /*
    //Closing sequence which will remove ChatGUI from Client's list
    static class ClosingSequence extends Thread {
        ClientGUI cg;
        ChatGUI room;
        ClosingSequence(ClientGUI cg, ChatGUI room) {
            this.cg = cg;
            this.room = room;

        }
        public void run() {
            //TODO: Remove
            System.out.println("Closing Sequence");
            cg.closeChat(room);
        }
    }
    */
}

