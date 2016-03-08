import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;

/**
 * Created by Daniel on 2/29/2016.
 */
public class FileReceiveGUI extends JFrame implements ActionListener, WindowListener{

    //JButtons to accept or deny file transfer request
    JButton accept, deny;
    //JLabel to display information about file request
    JLabel fileInfo;

    int transferId;
    long length;
    ClientGUI cg;
    ChatMessage cMsg;

    ObjectInputStream ois;
    FileOutputStream fos;
    BufferedOutputStream bos;

    final JFileChooser fc = new JFileChooser();

    //Constructor
    FileReceiveGUI(ClientGUI cg, ChatMessage cMsg, ObjectInputStream ois){

        super("New File Transfer Request");

        this.cg = cg;
        this.cMsg = cMsg;
        this.transferId = cMsg.getTransferId();
        this.ois = ois;

        this.length = cMsg.getFileSize();

        JPanel north, south;

        fileInfo = new JLabel("<html>You've received a file transfer(" + transferId + ")  request from user: <br>"
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
                deny.setEnabled(false);
                cg.acceptFileTransfer(transferId);
                return;
            }
            return;
        }
        //if user hits deny button
        if (o == deny) {
            sendDeny();
            cg.removeTransferRequest(this);
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

    private void sendAccept(){
    }

    void beginTransfer(ChatMessage cMsg){
        System.out.println("Beginning file transfer...");
        getFile();
    }

    void getFile(){
        //This should accept the file into a temporary file
        cg.append("Starting file transfer to server\n");
        try {
            byte [] mybytearray = new byte [((int) length)];
            System.out.println("Size of byte array: " + mybytearray.length);
            //is = socket.getInputStream();
            fos = new FileOutputStream(fc.getSelectedFile());
            bos = new BufferedOutputStream(fos);
            try{
                mybytearray = (byte []) ois.readObject();
            }
            catch (ClassNotFoundException e){
                cg.append("In reading file object:" + e.getMessage());
            }
            //bytesRead = is.read(mybytearray, 0, mybytearray.length);
            //current = 0;
            //int current = bytesRead;
            int current = (int)length;
            /*
            do {
                bytesRead = is.read(mybytearray, current, (mybytearray.length-current));
                if(bytesRead >= 0)
                    current += bytesRead;
                server.event("File progress: " + current + " of " + length);
            } while (current < length);
            //} while (is.available() > 0);
            */


            bos.write(mybytearray, 0, current);
            bos.flush();
        }
        catch (IOException e){
            cg.append("Error in receiving file" + e.getMessage() + "\n");
        }
        finally {
            try {
                if (fos != null)
                    fos.close();
                if(bos != null)
                    bos.close();
            }
            catch (IOException e){
                cg.append("Error closing file streams" + e.getMessage());
            }
        }
        cg.append("Obtained file: " + fc.getSelectedFile().getAbsolutePath());
        this.dispose();

    }

    int getTransferId(){
        return transferId;
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

}
