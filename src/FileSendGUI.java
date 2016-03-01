import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Daniel on 2/29/2016.
 */
public class FileSendGUI extends JFrame implements ActionListener{

    ChatGUI parent;
    JTextField tf;
    JButton send, select;
    final JFileChooser fc = new JFileChooser();

    FileSendGUI(ChatGUI parent) {

        super("Select a File to Send");
        this.parent = parent;

        JPanel north, south;

        north = new JPanel(new GridLayout(1,1));
        tf = new JTextField();
        tf.setText("Select a file:");
        tf.setEditable(false);
        tf.setBackground(Color.white);
        north.add(tf);


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

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if(o == select) {
            int returnVal = fc.showOpenDialog(this);
            tf.setText(fc.getSelectedFile().getAbsolutePath());
            return;
        }
        if(o == send) {
           return;
        }


    }

    public static void main(String arg[]) {
        FileSendGUI window = new FileSendGUI(null);
    }
}
