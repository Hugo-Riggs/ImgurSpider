/*
* Hugo Riggs
* This java program can be used to download images off of the
* imgur dot com / r / name
* domain. (Where name can be any existing directory).
*
 */
package imgurParse;

// imports for image graphics

import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
/*import java.io.ByteArrayOutputStream;*/
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.ArrayList;

// imports for Graphical User Interface
import javax.imageio.ImageIO;
import javax.swing.JCheckBox;
import javax.swing.JFrame;

//imports for web scraping
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

}// end GUI class

/////////////////////////////
//// ImgurSpider class
//	helpful links
//	http://jsoup.org/cookbook/
//	http://jsoup.org/apidocs/

class ImgurSpider implements Runnable {

    private Thread t;
    private String threadName;

    // Accessible to all member functions
    private static ImgurSpider spider;

    // Jsoup variables
    private static Document doc;
    private static Document tempDoc;
    private static Element content;
    private static Elements links;
    private static Elements linkName;//Adding this new line for getting image names

    private static String dirName;

    private static int g_strtpg = 0;
    private static boolean g_quickMode = true;
    private static boolean debugPrint = false;
    private static boolean g_gifs = false;
    private static String g_url = "";
    private static String g_htmlChunk = "";
    private static String[] args;

    // Graphics constants
    private static final int[] RGB_MASKS = {0xFF0000, 0xFF00, 0xFF};
    private static final ColorModel RGB_OPAQUE =
            new DirectColorModel(32, RGB_MASKS[0], RGB_MASKS[1], RGB_MASKS[2]);

    // Our spider is a Singleton.
    public static ImgurSpider getInstance(String n) {
        if (spider == null) {                           // If the object does not exist,
            spider = new ImgurSpider(n);                // create the object.
        }
        return spider;                                  // else return the existing object.
    }

    private ImgurSpider(String threadName) {
        this.threadName = threadName;
        System.out.println("Thread " + threadName);
    }

    private ImgurSpider() {
        System.out.println("Thread name not set");
    }

    // Return the i-th word of a String.
    private static String ithWord(String s, int i) {
        try {
            String tmp = s;
            String ithword = "";
            for (int j = 0; j < i; j++) {
                if (tmp.indexOf(" ") == -1)
                    ithword = tmp.substring(0, tmp.length());
                else {
                    ithword = tmp.substring(0, tmp.indexOf(" "));
                    tmp = tmp.substring(tmp.indexOf(" ") + 1, tmp.length());
                }
            }
            return ithword;
        } catch (Exception e) {
            System.out.println("Invalid string: " + s);
            return "";
        }
    }


    private static void debug(String str) {
        if(debugPrint)
            System.out.println("debug: " + str);
    }

    // Print that we are attempting to connect to to a URL.
    // Continue to try every 5 seconds until a connection is made.
    private static Document getDocument(String URLasString)  {
        Document doc = null;
        while (true) {

            debug("\nconnecting to " + URLasString + " . . .  ");
            try {
                doc = Jsoup.connect(URLasString).get();
            } catch (Exception e) {
                System.out.println(e);
            }
            if (doc != null) {
                debug("***connected***\n");
                break;
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e){
                System.out.println("Thread interruption exception " + e);
            }
        }
        return doc;
    }


    // Process initial user input
    private static void readArgs(String[] args) throws IOException, InterruptedException {

        while (true) { // While they have not entered a necessary argument.
            if (args.length == 0) { // No argument print a helpful message
                printHelp();
                System.exit(0);
            } else
                break;
        }

        for (int i = 0; i < args.length; i++) { // Read the arguments for setting program flags
            switch (args[i]) {
                case "-qm":
                    g_quickMode = true;
                    break;
                case "-v":
                    debugPrint = true;
                    break;
                case "-gifs":
                    g_gifs = true;
                    break;
            }
        }

        if (args.length != 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].contains("http:")) g_url = args[i];
            }
        }
        if (!args[args.length - 1].contains("http") && args[args.length - 1].contains("imgur.com")) {
            g_url = "http://" + args[args.length - 1];
        } else if (!args[args.length - 1].contains("http") && !args[args.length - 1].contains("imgur.com")) {
            g_url = "http://imgur.com/r/" + args[args.length - 1];
        }

        doc = getDocument(g_url);
        debug(doc.title());

        // Get directory name for saving locally
        dirName = ithWord(doc.title(), 1);

        // Create element objects which are used in html string parsing
        content = doc.getElementById("content");

        // Grab HTML DIV tag which holds image URL
        String localReference = selectURL(doc);

        //Establish connection with the image server , create a swap connection
        tempDoc = getDocument("https://imgur.com" + localReference + "/page/" + g_strtpg + "/hit?scrolled");
        doc = getDocument("https://imgur.com" + localReference + "/page/" + g_strtpg + "/hit?scrolled");

        // Fill element with html body
        content = doc.body();

        // Fill element with url's found in the body
        links = content.getElementsByTag("a");
        linkName = content.getElementsByTag("p");
        // feed these variables to the runScrape function which
        // downloads and saves images (.jpg,.gif,.png ...)
        //runScrape(dirName, links, content, doc, tempdoc);
    }

    // Proper command line usage instruction.
    private static void printHelp() {
        System.out.println("usage:" +
                "\n\t[URL] [tags]" +
                "\ntags:" +
                "\n-gifs -> for gif images" +
                "\n -v -> for more text output" +
                "\n -qm -> quick mode for non-gifs only");
    }

    // Part of the initial scraping process
    private static String selectURL(Document doc) {
        g_htmlChunk = doc.getElementsByClass("options").toString();
        g_htmlChunk = g_htmlChunk.substring(g_htmlChunk.indexOf("item nodisplay"), g_htmlChunk.length());
        g_htmlChunk = g_htmlChunk.substring(g_htmlChunk.indexOf("href=") + 6, g_htmlChunk.length());
        g_htmlChunk = g_htmlChunk.substring(0, g_htmlChunk.indexOf('"'));
        return g_htmlChunk;
    }

    // When the thread is created this method runs first.
    // Create a loop which breaks on thread interrupt.
    // A thread interrupt is triggered by the user when they
    // close the Swing window.
    public void run() {

        while (!Thread.currentThread().isInterrupted()) {        // The image scraping continues while the thread has not been interrupted.

            // Process user arguments, and display a usage message.
            try {
                readArgs(args);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                System.out.println(" failed reading arguments ");
                printHelp();
            }

            int repeatCntr = 0;             // used to cound the number of times the program loops
            int linksFound = 0;             // counts links found
            String linkHref = "";           // stores URL values during program execution
            ArrayList<String> names = new ArrayList<>(); // Stores names of images in mutable list type


            while (true) {    // scrape algorithm program loop
                if (Thread.currentThread().isInterrupted()) {       // Exit from GUI window causes thread interrupt
                    System.out.println("Thread interrupted\nExiting...");
                    break;
                }

                if (g_strtpg != 0) {            // We created a connection when reading arguments. This condition
                                                // evaluates to true in the event we load any new pages.
                    while (doc == tempDoc) {    // A loop to ensure we have new page content
                        doc = getDocument("https://imgur.com" + g_htmlChunk + "/page/" + g_strtpg + "/hit?scrolled");
                        debug("https://imgur.com" + g_htmlChunk + "/page/" + g_strtpg + "/hit?scrolled");

                        content = doc.body();                       // Retrieve html out of the document, place into string.
                        links = content.getElementsByTag("a");      // Select all linked data.
                        linkName = content.getElementsByTag("p");   // Select all paragraph data.
                        g_strtpg++;                                 // Increment page counter.
                        repeatCntr++;                               // Do the same with repeat counter.
                        if (repeatCntr > 5) {                       // If we fail to get a new page after 5 times, stop.
                            break;
                        }
                    }
                } else {
                    g_strtpg++;
                }

                // 	Go through links we found in the HTML
                int linkNameIndex = 0;
                String imageName;
                for (Element link : links) {
                    linkHref = link.attr("href");
                    if (!names.contains(linkHref) && !linkHref.contains("javascript") && doc != tempDoc) {  // Exclude already selected and links containing "javascript" in their naming.
                        names.add(linkHref);
                        String utilityString = linkHref.substring(linkHref.indexOf("m/") + 2, linkHref.length());

                        // Erase symbols in the names which prevent saving on Windows machines.
                        imageName = linkName.get(linkNameIndex).toString().replaceAll("<p>|</p>", "")
                                .replace("\\", "")
                                .replace("/", "")
                                .replace(":", "")
                                .replace("*", "")
                                .replace("?", "")
                                .replace("\"", "")
                                .replace("<", "")
                                .replace(">", "")
                                .replace("|", "");// illegal save chars: \/:*?"<>|

                        debug("Image name " + imageName);

                        linkNameIndex++;

                        // only run majority of code below if image has a name, otherwise the image is almost certainly
                        // doesn't exist.
                        if (!utilityString.equals("")) {

                            utilityString = utilityString.substring(utilityString.indexOf('/') + 1, utilityString.length());
                            utilityString = utilityString.substring(utilityString.indexOf('/') + 1, utilityString.length());

                            String tmp = ""; // tmp -> used to hold a to string value of the images Elements variable.

                            Element body = null;
                            Elements images = null;

                            if (!g_quickMode) {
                                doc = getDocument("https://imgur.com" + linkHref);
                                debug("https://imgur.com" + linkHref);

                                String id = linkHref.substring(3);
                                id = id.substring(id.indexOf("/") + 1);
                                body = doc.getElementById(id);
                                // According the the Imgur html code this is one way to select the animated GIF
                                // versus still image source data.
                                if (!g_gifs) {
                                    images = body.getElementsByTag("img");
                                } else {
                                    images = body.getElementsByAttribute("video/webm");
                                }

                            } else {
                                if (!utilityString.equals("")) {
                                    body = doc.getElementById(utilityString);
                                    images = body.getElementsByTag("img");
                                }
                            }

                            boolean isGIF = false; // Assume the image is not a GIF image.
                            if (images != null && images.toString().length() == 0) { // If this is true
                                isGIF = true;                                        // then we have a GIF
                                if (body != null) {
                                    images = body.getElementsByTag("script");
                                }
                            }
                            if (!(images == null))
                                tmp = images.toString();

                            String dlLink = tmp;
                            if (dlLink.length() == 0) { // We have no potential download link, so go to next loop
                                continue;// replaced break with continue
                            }

                            // In the case of a Jpeg, Png or other still image, identify source data
                            // with the string manipulations below
                            if (!isGIF) {
                                debug("\nstep 1 " + dlLink);
                                dlLink = dlLink.substring(dlLink.indexOf('"') + 3, dlLink.length());
                                debug("\nstep 2 " +dlLink+"\n");
                                if (!g_quickMode) {
                                    dlLink = "https://" + dlLink.substring(2, dlLink.indexOf('"'));
                                } else {
                                    dlLink = "https://" + dlLink.substring(dlLink.indexOf('"') + 3, dlLink.lastIndexOf('"'));
                                    dlLink = dlLink.substring(0, dlLink.indexOf("b.")) + dlLink.substring(dlLink.indexOf("b.") + 1, dlLink.length());
                                }
                            } else if (isGIF) { // In the case of a GIF use these string manipulations.
                                debug("\nstep 1 " + dlLink+"\n");
                                dlLink = dlLink.substring(dlLink.indexOf("//") + 2, dlLink.length());
                                debug("\nstep 2 " +dlLink+"\n");
                                dlLink = "https://" + dlLink.substring(2, dlLink.indexOf("',"));
                            }

                            // Image link (URL) found now it is time to:
                            // Create the image object
                            Image image;

                            if (dlLink.contains("?1")) {
                                dlLink = dlLink.substring(0, dlLink.indexOf("?1"));
                            }

                            String ext = ""; // string variable for image extension eg .jpg, .gif
                            if (!g_quickMode) {
                                ext = dlLink.substring(dlLink.indexOf(".") + 1, dlLink.length());
                                ext = ext.substring(ext.indexOf(".") + 1, ext.length());
                            } else {
                                ext = dlLink.substring(dlLink.indexOf(".") + 1, dlLink.length());
                                ext = ext.substring(ext.indexOf(".") + 1, ext.length());
                                ext = ext.substring(ext.indexOf(utilityString) + utilityString.length() + 1, ext.length());
                            }

                            debug("\nstep 3 " +dlLink+"\n");

                            if (g_gifs) {
                                try {
                                    byte[] b = new byte[1];
                                    URL url = new URL(dlLink);
                                    URLConnection urlConnection = url.openConnection();
                                    urlConnection.connect();
                                    DataInputStream di = new DataInputStream(urlConnection.getInputStream());
                                    FileOutputStream fo = new FileOutputStream(imageName + ".gif");
                                    while (-1 != di.read(b, 0, 1)) {
                                        fo.write(b, 0, 1);
                                    }
                                    di.close();
                                    fo.close();

                                } catch (MalformedURLException malURL) {
                                    System.out.println("bad url " + malURL);
                                } catch (IOException ioEX) {
                                    System.out.println("io ex, maybe file not found" + ioEX);
                                }

                            } else {
                                // This is where still image capture occurs.
                                // Connection to the image data stream followed by
                                // passing image data throw a few buffers.
                                // Eventually a write buffer creates the file on the local machine.

                                PixelGrabber pg = null;
                                try {
                                    URL url = new URL(dlLink);
                                    image = java.awt.Toolkit.getDefaultToolkit().createImage(url);
                                    pg = new PixelGrabber(image, 0, 0, -1, -1, true);
                                    pg.grabPixels();

                                } catch (MalformedURLException malURL) {
                                    System.out.println("bad URL");
                                } catch (InterruptedException intEx) {
                                    System.out.println(intEx);
                                }

                                int width = pg.getWidth(), height = pg.getHeight();
                                if (width < 0 || height < 0) {
                                    System.out.println("breaking because image width or height was invalid.");
                                   continue;//     break;
                                }

                                // Data buffers
                                DataBuffer buffer = new DataBufferInt((int[]) pg.getPixels(), pg.getWidth() * pg.getHeight());
                                WritableRaster raster = Raster.createPackedRaster(buffer, width, height, width, RGB_MASKS, null);
                                BufferedImage bi = new BufferedImage(RGB_OPAQUE, raster, false, null);

                                String path = ""; // String value to hold saving path.
                                String g_saveTo = "";// TODO: add a method to get this location, or prompt

                                int g_imgSv = 0;// TODO: this could be if, g_saveTo is not null save it there. To get rid of this var

                                if (g_imgSv == 0) {
                                    path = Paths.get("user.dir").toAbsolutePath().toString().replaceFirst("user.dir", "");
                                } else if (g_imgSv == 1) {
                                    path = g_saveTo;
                                }

                                 debug("saving to path: " + path);

                                if (!utilityString.equals("")) {
                                    // If this is the first image create the path for the images
                                    if (linksFound == 0) {
                                        new File(path + "//" + dirName + "//").mkdirs();
                                    }
                                    // test to see if a file exists  // TODO: this overwrites same named files, so instead just generate a unique name
                                    File file = new File((path + "//" + dirName + "//") + utilityString + "." + ext);
                                    if (!file.exists()) {
                                        try {
                                            debug("writing image: " + ( imageName + "." + ext));
                                            ImageIO.write(bi, ext, new File((path + "//" + dirName + "//") + imageName + "." + ext)); // utilityString -> imageName
                                        } catch (IOException ioEx) {
                                            System.out.println("error writing image " + ioEx);
                                        }

                                        linksFound++;

                                    }
                                    if (debugPrint)
                                        System.out.println("page " + g_strtpg + " pictures downloaded " + linksFound);
                                }
                            }
                        } else {
                            System.out.println("broken image");
                            //  System.out.println(doc.toString());
                            System.out.println("https://imgur.com" + g_htmlChunk + "/page/" + g_strtpg + "/hit?scrolled");
                        }//end if for empty image string
                    }//end if (!names.contains(linkHref)&&!linkHref.contains("javascript")&&doc!=tempdoc)
                } // END FOR loop, (which looks at individual links on the page)

                tempDoc = doc;

                int g_pageLimit = 50;// to local
                if (g_strtpg > g_pageLimit && g_pageLimit != 0) {
                    System.out.println("Stopped because current page number " + g_strtpg + " is > limit: " + g_pageLimit);
                    break;
                }
            }
            //System.out.println("repeatCntr" + repeatCntr);
            System.out.println("g_strtpg " + g_strtpg + " : page we ended on");
            System.out.println("linksFound " + linksFound);
        }

    }

    // The arguments string is set when the thread starts.
    // See if it is still possible to call this code through the main code.
    public void start(String[] args) throws IOException, InterruptedException {
        if (args == null)
            printHelp();
        this.args = args;
        System.out.println("Starting " + threadName);
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }

}


class Main {

    public static void main(String args[]) throws IOException, InterruptedException {

        // The ImgurSpider object is a Singleton.
        ImgurSpider spider;
        spider = ImgurSpider.getInstance("Imgur Spider");

        final GUI gui;
        if(args.length == 0)
            gui = new GUI(spider);	// Pass the spider object into the graphical user interface.

        spider.start(args);  // non GUI version
    }

}
