package imgurParse;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

// Add a graphical user interface for easier use by the end user.
// This JFrame class is used to create a window.
class GUI extends JFrame {

    private javax.swing.JTextField URL;

    private JCheckBox check;
    private ImgurSpider spider;

    // Constructor for GUI:
    public GUI(ImgurSpider spider) {
        this.spider = spider;
        initComponents();
    }

    private void initComponents() {
        setTitle("Imgur Spider");
        setSize(300, 150);
        setLocation(10, 200);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        this.setLayout(null);
        javax.swing.JLabel URLlabel = new javax.swing.JLabel();     // initialize a label

        URL = new javax.swing.JTextField();

        javax.swing.JButton startButton = new javax.swing.JButton(); // initialize a button

        URLlabel.setText("URL: (example: http://imgur.com/r/wallpapers)");
        URLlabel.setBounds(4, 4, 300, 20);

        URL.setText("");
        URL.setBounds(4, 20, 160, 20);

        check = new JCheckBox("source are GIFs");
        check.setBounds(165, 20, 165, 20);

        startButton.setText("Start");
        startButton.setBounds(4, 40, 80, 30);

        startButton.addActionListener(evt -> {
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
        this.setVisible(true);
    }

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) throws IOException, InterruptedException {
        String url = URL.getText();
        String[] args = {url, "", "", ""};
        if (check.isSelected())
            args[1] = "-gifs";
        spider.start(args);
    }

}
