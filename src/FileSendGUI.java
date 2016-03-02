import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Daniel on 2/29/2016.
 */
public class FileSendGUI extends JFrame implements ActionListener{

    ChatGUI parent;

    //Display file path
    JTextField tf;
    //Buttons to send a file or select a file
    JButton send, select;
    //GUI File chooser
    final JFileChooser fc = new JFileChooser();

    //Socket socket;
    FileInputStream fis;
    BufferedInputStream bis;
    //OutputStream os;
    ObjectOutputStream oos;

    FileSendGUI(ChatGUI parent) {

        super("Select a File to Send");
        this.parent = parent;

        //socket = parent.getSocket();
        oos = parent.getOOS();

        JPanel north, south;

        //North panel contains text field displaying selected file path
        north = new JPanel(new GridLayout(1,1));
        tf = new JTextField();
        tf.setText("Select a file:");
        tf.setEditable(false);
        tf.setBackground(Color.white);
        north.add(tf);


        //South panel contains select and send button
        south = new JPanel();
        select = new JButton("Select File");
        select.addActionListener(this);
        send = new JButton("Send");
        send.addActionListener(this);
        south.add(select);
        south.add(send);

        add(north, BorderLayout.NORTH);
        add(south, BorderLayout.CENTER);

        this.setPreferredSize(new Dimension(400, 140));
        this.setMinimumSize(new Dimension(400, 140));
        this.setLocationRelativeTo(null);

        this.setVisible(true);
    }

    //Function to handle writing file to output stream
    private void writeFile(){
        parent.fileNotification(fc.getSelectedFile().length(), fc.getSelectedFile().getName());
        System.out.println("fileNotification sent");
        tf.setText(fc.getSelectedFile().getAbsolutePath());
        File myFile = fc.getSelectedFile();
        if(myFile == null)
            return;
        byte [] mybytearray = new byte[(int)myFile.length()];
        parent.append("Sending File: " + fc.getSelectedFile().getName());
        try {
            fis = new FileInputStream(myFile);
            bis = new BufferedInputStream(fis);
            bis.read(mybytearray, 0, mybytearray.length);
            /*
            os = socket.getOutputStream();
            os.write(mybytearray, 0, mybytearray.length);
            os.flush();
            */
            oos.writeObject(mybytearray);
            parent.append("File Sent");
        }
        catch (IOException e1) {
            parent.append("File Send Error: " + e1.getMessage());
        }
        finally {
            if (bis != null) {
                try{
                    bis.close();
                }
                catch (IOException e2){
                }
            }
            /*
            if(os != null) {
                try {
                    os.close();
                }
                catch (IOException e3){
                }
            }
            */
        }
        parent.sendNull();

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if(o == select) {
            int returnVal = fc.showOpenDialog(this);
            if(returnVal == JFileChooser.CANCEL_OPTION)
                return;
            tf.setText(fc.getSelectedFile().getAbsolutePath());
            return;
        }
        if(o == send) {
            writeFile();
            this.dispose();
            return;
        }


    }

    public static void main(String arg[]) {
        FileSendGUI window = new FileSendGUI(null);
    }
}
