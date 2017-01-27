package main.java.io.github.HugoRiggs.ImgurSpider;

/**
 * Created by hugo on 1/26/17.
 */

/// imports for Graphical User Interface
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;


/** Add a graphical user interface for easier use by the end user.
 *   This JFrame class is used to create a window.
 */
public class GUI extends JFrame {

    private Thread t;
    private String threadName;

    private javax.swing.JTextField URL;
    private javax.swing.JTextField path;
    javax.swing.JLabel pathLabel = new javax.swing.JLabel();	 // initialize a label

    private JCheckBox check;
    private Crawler crawler;

    // Constructor for GUI:
    public GUI(Crawler crawler ) {
        this.crawler=crawler;
        initComponents();
    }

    private void initComponents(){
        setTitle("Imgur Spider");
        setSize(360,150);
        setLocation(10,200);

        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        this.setLayout(null);

        // URL input aspect
        javax.swing.JLabel URLlabel = new javax.swing.JLabel();	 // initialize a label
        URL = new javax.swing.JTextField();
        URLlabel.setText("URL: (example: http://imgur.com/r/wallpapers)");
        URLlabel.setBounds(4, 4, 350, 20);
        URL.setText("");
        URL.setBounds(4,20, 260, 20);

        // Path
        pathLabel.setText("Path: ");
        pathLabel.setBounds(4, 45, 260, 20);
        path = new javax.swing.JTextField();
        path.setText("");
        path.setBounds(4, 61, 260, 20);

        // Start button
        javax.swing.JButton startButton = new javax.swing.JButton(); // initialize a button
        startButton.setText("Start");
        startButton.setBounds(4, 86,80,30);

        // Download GIFs Checkbox
        check = new JCheckBox("source are GIFs");
        check.setBounds(165,86,165,20);

        startButton.addActionListener( evt ->  {
            try {
                startButtonActionPerformed(evt);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });


        add(URL);
        add(URLlabel);
        add(startButton);
        add(check);
        add(path);
        add(pathLabel);
        this.setVisible(true);
    }

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) throws IOException, InterruptedException {
        String url=URL.getText();
        String[] args = {url, "", "", ""};
        if(check.isSelected())
            args[1]="-g";
        args[2] = "--path="+path.getText();
        crawler.start(args);
    }

}// end GUI class



