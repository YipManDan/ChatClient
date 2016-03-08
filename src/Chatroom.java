import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Daniel on 3/7/2016.
 */
public class Chatroom {
    ArrayList<UserId> users;
    ArrayList<Message> messages;
    ClientGUI cg;
    ChatGUI chatGUI;
    Boolean isOpen;

    //Constructor
    Chatroom(ArrayList<UserId> users, ArrayList<Message> messages, ClientGUI cg) {
        this.users = users;
        this.messages = messages;
        if(messages == null)
            this.messages = new ArrayList<>();
        this.cg = cg;
        isOpen = false;
        openGUI();
    }

    void openGUI(){
        isOpen = true;
        this.chatGUI = new ChatGUI(users, messages, cg, this);
        chatGUI.setLocationRelativeTo(cg);
        chatGUI.setVisible(true);
    }

    void closeGUI() {
        isOpen = false;
    }

    void newMessage(ChatMessage cMsg) {
        Message m = new Message(cMsg.getSender(), cMsg.getTimestamp(), cMsg.getMessage());
        messages.add(m);
        Collections.sort(messages);
        if(isOpen)
            chatGUI.append(cMsg.getMessage());
        else
            openGUI();
    }

    String getTitle(){
        return cg.getTitle();
    }

    void sendMessage(ArrayList<UserId> recipients, String message) {
        cg.sendMessage(recipients, message);
    }

    void fileTransferStart(long length, String filename, ArrayList<UserId> recipients) {
        cg.fileTransferStart(length, filename, recipients);
    }

    void sendNull(){
        cg.sendNull();
    }
    ObjectOutputStream getOOS(){
        return cg.getOOS();
    }

    void closeChat(ChatGUI chat){
        isOpen = false;
    }

    public ArrayList<UserId> getUsers() {
        return users;
    }
}
