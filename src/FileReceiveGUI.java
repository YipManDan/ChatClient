import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

/**
 * Created by Daniel on 2/29/2016.
 */
public class FileReceiveGUI extends JFrame implements ActionListener, WindowListener{

    //JButtons to accept or deny file transfer request
    JButton accept, deny;
    //JLabel to display information about file request
    JLabel fileInfo;

    int transferId;
    ClientGUI cg;
    ChatMessage cMsg;

    final JFileChooser fc = new JFileChooser();

    //Constructor
    FileReceiveGUI(ClientGUI cg, ChatMessage cMsg){

        super("New File Transfer Request");

        this.cg = cg;
        this.cMsg = cMsg;
        this.transferId = cMsg.getTransferId();

        JPanel north, south;

        fileInfo = new JLabel("<html>You've received a file transfer request from user: <br>"
                + cMsg.getSender().getId()
                + " "
                + cMsg.getSender().getName()
                + ". <br>Filename: "
                + cMsg.getMessage()
                + " Size: "
                + cMsg.getFileSize()
                + "Bytes</html>", SwingConstants.CENTER);

        north = new JPanel();
        north.add(fileInfo);

        south = new JPanel();
        accept = new JButton("Accept");
        accept.addActionListener(this);
        deny = new JButton("Deny");
        deny.addActionListener(this);
        south.add(accept);
        south.add(deny);

        //Add the panels to JFrame
        add(north, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        this.setLocationRelativeTo(cg);
        this.setMinimumSize(new Dimension(400, 140));
        this.addWindowListener(this);
        this.setVisible(true);
        this.pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        //If user hits accept button
        if (o == accept) {
            fc.setSelectedFile(new File(cMsg.getMessage()));
            int returnVal = fc.showSaveDialog(this);
            //Update Jlabel to display status and new name
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                fileInfo.setText("<html>You've accepted the file " + cMsg.getMessage()
                        + "<br>Saving as: " + fc.getSelectedFile() + "</html>");
                //resize JFrame if label too large
                this.pack();
                return;
            }
            return;
        }
        //if user hits deny button
        if (o == deny) {
            sendDeny();
            this.dispose();
            return;
        }

    }

    //Inform server that request was denied
    private void sendDeny(){
        System.out.println("Closing Sequence");
        //Send a deny message to the server
        cg.denyFileTransfer(transferId);
    }


    @Override
    public void windowClosing(WindowEvent e) {
        sendDeny();
    }

    public void windowClosed(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e){}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}

    //Main method for testing
    public static void main(String[] args) {
        FileReceiveGUI window = new FileReceiveGUI(null,
                new ChatMessage(ChatMessage.FILE, ChatMessage.FILESEND, 03, 6023451
                        , "Filename!", new UserId(3, "SomePerson")));
    }
}
