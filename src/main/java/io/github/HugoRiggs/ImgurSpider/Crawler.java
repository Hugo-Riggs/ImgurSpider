package main.java.io.github.HugoRiggs.ImgurSpider;

/**
* Hugo Riggs
* Created by hugo on 1/26/17.
* This java program can be used to download images off of the
* imgur dot com / r / name
* domain. (Where name can be any existing directory).
*
*/

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Crawler implements Runnable {

    private Thread t;
    private String threadName;
    private static Crawler crawler;

    // Global object variables
    private static int g_strtpg = 0;
    private static boolean g_prints = true;
    private static String[] args;

    // Our crawler is a Singleton.
    public static Crawler getInstance(String n) {
        if (crawler == null) {
            crawler = new Crawler(n);
        }
        return crawler;
    }

    private Crawler(String threadName) {
        this.threadName = threadName;
    }

    private Crawler() {
        println("Thread name not set"); // Place this command in a thread message eventually.
    }

    // Return the i-th word of a String.
    private static String ithWord(String s, int i) {// why not leave this as public, otherwise it could be put in a separate utility class.
        // method written by Dr.C
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

//           public static byte[] returnBytes(BufferedImage image){
//               byte[] imageInByte = null;
//               try{
//                    BufferedImage originalImage = image;
//                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                    ImageIO.write( originalImage, "jpg", baos );
//                    baos.flush();
//                    imageInByte = baos.toByteArray();
//                    baos.close();
//                }catch(IOException e){
//                    System.out.println(e.getMessage());
//                }
//
//               return imageInByte;
//           }
//
//	// This is important for creating directories in windows or unix
//	public static String addSlashes(String str){
//		for(int itr=0;itr<str.length();itr++){
//		   if(str.charAt(itr)=='\''){str = str.substring(0, itr) + "\\" + str.substring(itr, str.length()); itr++;}
//		   if(str.charAt(itr)=='/'){str = str.substring(0, itr) + "/" + str.substring(itr, str.length()); itr++;}
//		}
//		return str;
//	}

    // Print that we are attempting to connecto to a URL.
    // Continue to try every 5 seconds until a connection is made.
    private static Document docGrab(String imgurURL) {
        Document doc = null;
        while (true) {
            System.out.print("\nconnecting to " + imgurURL + " . . .  ");
            try {
                doc = Jsoup.connect(imgurURL).get();
                if (doc != null) {
                    System.out.print("***connected***\n");
                    break;
                }
                Thread.sleep(5000);
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
        }
        return doc;
    }


    // Process initial user input
    private ArrayList<Object> parseArgs(String[] args) throws IOException, InterruptedException {
        boolean quickMode = false, gifs = false, verbose = false;
        String imgurURL = "", path = "";

        if (args.length == 0) { // If no argument print a helpful message
            printHelp();
            System.exit(0);
        }

        for (String arg : args) {
            // values and flags
            if (arg.contains("http:")) {
                imgurURL = arg;
            } else if(arg.contains("--path=")) {
                path = arg.substring(7, arg.length());
                String os = System.getProperty("os.name");
                if(os.contains("indows") && path.charAt(path.length()-1) != '\\')
                    path += "\\";
                else if (path.charAt(path.length()-1) != '/')
                    path += "/";
           } else if (arg.contains("-q") || arg.contains("--quickmode")){
                quickMode = true;
            } else if (arg.contains("-v") || arg.contains("--verbose")){
                verbose = true;
            } else if (arg.contains("-g") || arg.contains("--gifs")){
                println("GIF capture enabled");
                gifs = true;
            }
        }

        ArrayList ret = new ArrayList<Object>();
        ret.add(0, quickMode);
        ret.add(1, gifs);
        ret.add(2, verbose);
        ret.add(3, imgurURL);
        ret.add(4, path);

        return ret;

    }

    // Proper command line usage instruction.
    private static void printHelp() {
        System.out.println("usage:\n\t[URL] [tags]\ntags:\n-g, --gifs -> for gif images\n -v, --verbose -> for more text output\n -q, --quickmode -> quick mode for non-gifs only");
    }

    // Part of the initial scraping process
    private static String selectReference(Document doc) {
        String tmp = doc.getElementsByClass("options").toString();
        tmp = tmp.substring(tmp.indexOf("item nodisplay"), tmp.length());
        tmp = tmp.substring(tmp.indexOf("href=") + 6, tmp.length());
        tmp = tmp.substring(0, tmp.indexOf('"'));
        return tmp;
    }


    private String proccessStringForNotGifURL(String dlLink, boolean quickMode) {
        dlLink = dlLink.substring(dlLink.indexOf("//") + 2, dlLink.length());
        dlLink = "https://" + dlLink.substring(0, dlLink.indexOf('"'));
        return dlLink;
    }

    private String parseForExtention(String dlLink, boolean quickMode){
        String ext = "";
        ext = dlLink.substring((dlLink.lastIndexOf('.', dlLink.length()-1)));
        return ext;
    }

    private Elements getAlbumImages(Element body){
        Elements elements = body.select("script");
        println("elements size " + elements.size());
        return elements;
    }

    private ArrayList<String[]> handleAlbum(Document doc){
        ArrayList<String[]> downloadQueue = new ArrayList<>();
        return downloadQueue;
    }

    private ArrayList<String[]> handleDirectory(Document doc, boolean quickMode, boolean gifs ){
            String linkHref;
            ArrayList<String> sources = new ArrayList<>();
            ArrayList<String[]> downloadQueue = new ArrayList<>();

            try {  //Establish connection with the image server , create a swap connection
                doc = docGrab("https://imgur.com" + selectReference(doc) + "/page/" + g_strtpg + "/hit?scrolled");
                println(doc.title());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // get body
            Element htmlElement = doc.body();

            // Fill element with url's found in the body
            Elements links = htmlElement.getElementsByTag("a");
            Elements linksNames = htmlElement.getElementsByTag("p");

            // 	Go through links we found in the HTML
            int linkNameIndex = 0;
            String imageName;
            for (Element link : links) {

                linkHref = link.attr("href");
                // Exclude already selected and links containing "javascript" in their naming.
                if (sources.contains(linkHref) || linkHref.contains("javascript")) {
                    continue;
                } else {
                    sources.add(linkHref);

                    imageName =
                            linksNames.get(linkNameIndex).toString()
                                    .replaceAll("<p>|</p>|(\\\\\\\\|/|<|>|\\?|\\||\\\\|\\||\\.|\\:|\")+", "");
                    linkNameIndex++;

                    String tmpStr = linkHref.substring(linkHref.indexOf("m/") + 2, linkHref.length());
                    // only run majority of code below if image has a name
                    if (tmpStr.equals("")) continue;
                    else {

                        tmpStr = tmpStr.substring(tmpStr.indexOf('/') + 1, tmpStr.length());
                        tmpStr = tmpStr.substring(tmpStr.indexOf('/') + 1, tmpStr.length());

                        String tmp = ""; // tmp -> used to hold a to string value of the images Elements variable.

                        Element body = null;
                        Elements images = null;

                        if (!quickMode) {
                            try {
                                doc = Jsoup.connect("https://imgur.com" + linkHref).get();
                            } catch (Exception e) {
                                System.out.println("Error making connection to " + "https://imgur.com" + linkHref + "\n\n" + e);
                            }

                            String id = linkHref.substring(3);
                            id = id.substring(id.indexOf("/") + 1);
                            body = doc.getElementById(id);
                            if(body == null) {println("ERROR"); continue;}
                            if (!gifs) {
                                images = body.getElementsByTag("img");
                            } else {
                                images = body.getElementsByAttribute("video/webm");
                            }
                        } else { // Quick Mode
                            if (!tmpStr.equals("")) {
                                body = doc.getElementById(tmpStr);
                                images = getAlbumImages(body);
                            }
                        }

                        boolean isGIF = false;//This case covers when the image is a .GIF
                        if (images != null && images.toString().length() == 0) {    // From html, determines that the image is a .jpeg
                            isGIF = true;
                            if (body != null) {
                                images = body.getElementsByTag("script");
                            }
                        }

                        if (!(images == null))
                            tmp = images.toString();

                        String dlLink = tmp;
                        if (dlLink.length() == 0) {
                            break;
                        }

                        if (isGIF) {
                            dlLink = dlLink.substring(dlLink.indexOf("//") + 2, dlLink.length());
                            dlLink = "https://" + dlLink.substring(2, dlLink.indexOf("',"));
                        } else if (!isGIF) {
                            dlLink = proccessStringForNotGifURL(dlLink, quickMode);
                        }

                        // removing dangling characters
                        if (dlLink.contains("?1")) {
                            dlLink = dlLink.substring(0, dlLink.indexOf("?1"));
                        }

                        // get extention
                        String ext = parseForExtention(dlLink, quickMode);
                        dlLink = dlLink.replace("r"+ext, ext);
                        String[] touple3 = { dlLink, imageName, ext};
                        println("adding to download queue " + touple3[0] +", "+ touple3[1] +", "+ touple3[2]);
                        downloadQueue.add(touple3);
                    }

                }
            } // END FOR loop, (which looks at individual links on the page)
            g_strtpg++;
        return downloadQueue;
    }

    private void crawl(boolean quickMode, boolean gifs, boolean verbose, String imgurURL, String path) {

        // Initialization for the crawl
        if (path.isEmpty())
            path = Paths.get("user.dir").toAbsolutePath().toString().replaceFirst("user.dir", "");
        final String pathf = path;
        ArrayList<String[]> downloadQueue = new ArrayList<>();
        int repeatCntr = 0;
        int linksFound = 0;
        boolean isAlbum = false, isDirectory=false;
        if(imgurURL.contains("/a/")){
            if(!imgurURL.contains("?grid")) {
                imgurURL += "?grid";
        }
        isAlbum = true;
        } else if (imgurURL.contains("/r/")){
            isDirectory = true;
        }

        while (true) {    // Work loop

            if (Thread.currentThread().isInterrupted()) {
                println("Thread interrupted\n Exiting...");
                break;
            }

            Document doc = docGrab(imgurURL);

            // Crawling
            if(isAlbum){
                 downloadQueue.addAll(handleAlbum(doc)); // todo fill in handleAlbum method
            } else if (isDirectory) {
                downloadQueue.addAll(handleDirectory(doc, quickMode, gifs));
                linksFound ++;
            }

            // Downloading
            if (gifs) {
                downloadQueue.forEach(link -> Downloader.gifFromURL(link[0], link[1], pathf));
            } else {
                println("Downloading from queue");
                downloadQueue.forEach(link -> Downloader.imageFromURL(link[0], link[1], link[2], pathf));
            }

            repeatCntr++;
        }

        println("number of iterations: " + repeatCntr);
        println("linksFound " + linksFound);
    }

    private void println(String s) {
        if (g_prints)
            System.out.println(s);
    }

    private void print(String s) {
        if (g_prints)
            System.out.print(s);
    }


    // When the thread is created this method runs first.
    // Create a loop which breaks on thread interupt.
    // A thread interrupt is triggered by the user when they
    // close the Swing window. (if using GUI)
    public void run() {
        try {
            ArrayList ob = parseArgs(args);
            crawl((boolean) ob.get(0), (boolean) ob.get(1), (boolean) ob.get(2), (String) ob.get(3), (String) ob.get(4));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            println(" failed reading arguments ");
            printHelp();
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
