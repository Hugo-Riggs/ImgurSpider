/*
* This java program can be used to download images off of the
* imgur dot com / r / name
* domain. (Where name can be any existing directory).
*
* Hugo Riggs
 */
package imgurParse;

// imports for image graphics

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;

//imports for web scraping
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/*
* ImgurSpider class
*	helpful links
*	http://jsoup.org/cookbook/
*	http://jsoup.org/apidocs/
*/
public class ImgurSpider implements Runnable {

    private String threadName;
    private static ImgurSpider spider;
    private static Document doc;
    private static Document tempDoc;
    private static Element content;
    private static Elements links;
    private static Elements linkName;//Adding this new line for getting image names
    private static String dirName;
    private static String imgurDirectoryName = "";
    private static int pageNumber = 0;
    private static boolean g_quickMode = true;
    private static boolean debugPrint = true;
    private static boolean g_gifs = false;
    private static String[] args;
    private static int maxDownloadedImages = -1;
    static int linksFound = 0;             // counts links found

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

    public ImgurSpider(String threadName) {
        this.threadName = threadName;
        System.out.println("Thread " + threadName);
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
        if (debugPrint)
            System.out.println("\ndebug: " + str);
    }

    private static Document getDocumentJsoup(String URLasString) {
        Document doc = null;
        while (true) {
            debug("\nconnecting to " + URLasString + " . . .  ");  // Print that we are attempting to connect to to a URL.
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
                Thread.sleep(5000);  // Continue to try every 5 seconds until a connection is made.
            } catch (InterruptedException e) {
                System.out.println("Thread interruption exception " + e);
            }
        }
        return doc;
    }

    private static String getURLfromArgs(String[] args) throws MalformedURLException {
        for (String s : args) {
            if (!s.contains("-"))
                return s;
        }
        throw new MalformedURLException("cannot read url from argumnets.");
    }

    private static String getImgurtDirectoryName(String url) {
        return url.substring(url.indexOf("/r/") + 3, url.length());
    }

    // Process initial user input
    private static void readArgs(String[] args) {

        while (true)  // While they have not entered a necessary argument.
            if (args.length == 0) { // No argument print a helpful message
                printHelp();
                System.exit(0);
            } else
                break;

        for (int i = 0; i < args.length; i++) // Read the arguments for setting program flags
            if (args[i].contains("-"))
                switch (args[i]) {
                    case "-qm":
                        g_quickMode = true;
                        debug("q_quickMode = true");
                        break;
                    case "-v":
                        debugPrint = true;
                        debug("debugPrint = true");
                        break;
                    case "-gifs":
                        g_gifs = true;
                        debug("g_gifs = true");
                        break;
                    default:
                        if (args[i].contains("-n: ")) {
                            debug(args[i]);
                            maxDownloadedImages = Integer.parseInt(args[i].substring(args[i].indexOf("-n: ") + 4, args[i].length()));
                            debug("maxDownloadedImages = " + maxDownloadedImages);
                        }
                }
        try {
            imgurDirectoryName = getImgurtDirectoryName(getURLfromArgs(args));
            debug(imgurDirectoryName);

            doc = getDocumentJsoup(getURLfromArgs(args));
            debug(doc.title());

            // Get directory name for saving locally
            dirName = ithWord(doc.title(), 1);

            // Create element objects which are used in html string parsing
            content = doc.getElementById("content");

            // Grab HTML DIV tag which holds image URL
            String localReference = doc.getElementsByClass("options").toString();
            localReference = localReference.substring(localReference.indexOf("item nodisplay"), localReference.length());
            localReference = localReference.substring(localReference.indexOf("href=") + 6, localReference.length());
            localReference = localReference.substring(0, localReference.indexOf('"'));
            debug(localReference);

            //Establish connection with the image server , create a swap connection
            tempDoc = getDocumentJsoup("https://imgur.com" + localReference + "/page/" + pageNumber + "/hit?scrolled");
            doc = getDocumentJsoup("https://imgur.com" + localReference + "/page/" + pageNumber + "/hit?scrolled");
        } catch (Exception e) {
            System.out.println("Exception " + e);
        }
        // Fill element with html body
        content = doc.body();

        // Fill element with url's found in the body
        links = content.getElementsByTag("a");
        linkName = content.getElementsByTag("p");
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

    // When the thread is created this method runs first.
    // Create a loop which breaks on thread interrupt.
    // A thread interrupt is triggered by the user when they
    // close the Swing window.
    public void run() {

        Thread thisThread = Thread.currentThread();
        int repeatCntr = 0;             // used to cound the number of times the program loops
        int g_pageLimit = 50;
        String linkHref = "";           // stores URL values during program execution
        ArrayList<String> names = new ArrayList<>(); // Stores names of images in mutable list type

        readArgs(args);

        while (spiderThread == thisThread) {   // scrape algorithm program loop
            if (pageNumber != 0) {
                repeatCntr = 0;
                while (doc == tempDoc) {    // A loop to ensure we have new page content
                    doc = getDocumentJsoup("https://imgur.com/r/" + imgurDirectoryName + "/page/" + pageNumber + "/hit?scrolled");
                    content = doc.body();                       // Retrieve html out of the document, place into string.
                    links = content.getElementsByTag("a");      // Select all linked data.
                    linkName = content.getElementsByTag("p");   // Select all paragraph data.
                    pageNumber++;                                 // Increment page counter.
                    repeatCntr++;                               // Do the same with repeat counter.
                    if (repeatCntr > 5) {                       // If we fail to get a new page after 5 times, stop.
                        break;
                    }
                }
            } else {
                pageNumber++;
            }

            int linkNameIndex = 0;
            String imageName;

            for (Element link : links) {        //      Go through links we found in the HTML
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

                    linkNameIndex++;
                    if (!utilityString.equals("")) {

                        if (maxDownloadedImages == linksFound)
                            stop();

                        if (!utilityString.equals("")) {
                            file_download(linkHref, imageName);
                            linksFound++;
                        }
                    }
                }
            }
            tempDoc = doc;
            if (pageNumber > g_pageLimit && g_pageLimit != 0)
                break;
        }
    }

    public static void file_download(String linkHref, String imageName) {
        String imageID = linkHref.substring(linkHref.lastIndexOf('/')+1, linkHref.length());
        String dlLink = "";
        String ext = ""; // string variable for image extension eg .jpg, .gif

        Image image;

        Element body = null;
        Elements images = null;

        if (!g_quickMode) {
            doc = getDocumentJsoup("https://imgur.com" + linkHref);
            debug("https://imgur.com" + linkHref);
            String id = linkHref.substring(3);
            id = id.substring(id.indexOf("/") + 1);
            body = doc.getElementById(id);
            images = g_gifs ?  body.getElementsByAttribute("video/webm") : body.getElementsByTag("img");// According the the Imgur html code this is one way to select the animated GIF
        } else {
            if (!imageID.equals("")) {
                body = doc.getElementById(imageID);
                images = body.getElementsByTag("img");
            }
        }
        boolean isGIF = false; // Assume the image is not a GIF image.
        if (images != null && images.toString().length() == 0) { // If this is true
            isGIF = true;                                        // then we have a GIF
            if (body != null)
                images = body.getElementsByTag("script");
        }

        if (!(images == null))
            dlLink = images.toString();

        if (dlLink.length() == 0) // We have no potential download link, so go to next loop
            return;
        debug(dlLink);

        if (!isGIF) {        // In the case of a Jpeg, Png or other still image, identify source data, via strings
            dlLink = dlLink.substring(dlLink.indexOf('"') + 3, dlLink.length());
            if (!g_quickMode)
                dlLink = "https://" + dlLink.substring(2, dlLink.indexOf('"'));
             else {
                dlLink = "https://" + dlLink.substring(dlLink.indexOf('"') + 3, dlLink.lastIndexOf('"'));
                dlLink = dlLink.substring(0, dlLink.indexOf("b.")) + dlLink.substring(dlLink.indexOf("b.") + 1, dlLink.length());
            }
        } else if (isGIF) { // In the case of a GIF use these string manipulations.
            dlLink = dlLink.substring(dlLink.indexOf("//") + 2, dlLink.length());
            dlLink = "https://" + dlLink.substring(2, dlLink.indexOf("',"));
        }

        if (dlLink.contains("?1"))
            dlLink = dlLink.substring(0, dlLink.indexOf("?1"));

        if (!g_quickMode) {
            ext = dlLink.substring(dlLink.indexOf(".") + 1, dlLink.length());
            ext = ext.substring(ext.indexOf(".") + 1, ext.length());
        } else {
            ext = dlLink.substring(dlLink.indexOf(".") + 1, dlLink.length());
            ext = ext.substring(ext.indexOf(".") + 1, ext.length());
            ext = ext.substring(ext.indexOf(imageID) + imageID.length() + 1, ext.length());
        }

        if (g_gifs) { // GIF
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

            } catch ( IOException e) {
                System.out.println("bad url " + e);
            }

        } else { // NOT GIF
          PixelGrabber pg = null;
            try {
                URL url = new URL(dlLink);
                image = java.awt.Toolkit.getDefaultToolkit().createImage(url);
                pg = new PixelGrabber(image, 0, 0, -1, -1, true);
                pg.grabPixels();
            } catch (MalformedURLException | InterruptedException e) {
                System.out.println("exception " + e);
            }

            int width = pg.getWidth(), height = pg.getHeight();
            if (width < 0 || height < 0) {
                System.out.println("breaking because image width or height was invalid.");
                return;
            }

            DataBuffer buffer = new DataBufferInt((int[]) pg.getPixels(), pg.getWidth() * pg.getHeight());
            WritableRaster raster = Raster.createPackedRaster(buffer, width, height, width, RGB_MASKS, null);
            BufferedImage bi = new BufferedImage(RGB_OPAQUE, raster, false, null);

            String path = ""; // String value to hold saving path.
            String g_saveTo = "";// TODO: add a method to get this location, or prompt

            boolean g_imgSv = false;// TODO: this could be if, g_saveTo is not null save it there. To get rid of this var

            if (!g_imgSv)
                path = Paths.get("user.dir").toAbsolutePath().toString().replaceFirst("user.dir", "");
            else if (g_imgSv)
                path = g_saveTo;


            if (!imageID.equals("")) {
                if (linksFound == 0)                // If this is the first image create the path for the images
                    new File(path + "//" + dirName + "//").mkdirs();

                File file = new File((path + "//" + dirName + "//") + imageName + "." + ext);

                if (file.exists() && !file.isDirectory()) {
                    try {
                        ImageIO.write(bi, ext, new File((path + "//" + dirName + "//") + imageName + new Random().nextInt() +  "." + ext));
                    } catch (IOException ioEx) {
                        System.out.println("error writing image " + ioEx);
                    }
                    linksFound++;
                } else if (!file.exists() && !file.isDirectory()){
                     try {
                        ImageIO.write(bi, ext, new File((path + "//" + dirName + "//") + imageName + "." + ext));
                    } catch (IOException ioEx) {
                        System.out.println("error writing image " + ioEx);
                    }
                    linksFound++;
                }
        } }
    }


    private volatile static Thread spiderThread;

    public void stop(){
        debug("stopping");
        spiderThread = null;
        System.exit(0);
    }


    // The arguments string is set when the thread starts.
    // See if it is still possible to call this code through the main code.
    public void start(String[] args) throws IOException, InterruptedException {
        if (args == null)
            printHelp();
        this.args = args;
        System.out.println("Starting " + threadName);
        if (spiderThread == null) {
            spiderThread = new Thread(this, threadName);
            spiderThread.start();
        }
    }


}
