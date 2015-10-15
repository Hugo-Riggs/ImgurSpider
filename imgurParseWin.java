package imgurParseWin;



import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/////////////////////////////                                                                                                                                                                 
//// Parse a imgur.com page for its images                                                                                                                                                    
////                                                                                                                                                                                          
////                                                                                                                                                                                          

//	http://jsoup.org/cookbook/                                                                                                                                                                
//	http://jsoup.org/apidocs/                                                                                                                                                                 

public class imgurParseWin {
	
    // Default settings for simple mode                                                                                                                                                       
    public static boolean skipConsole = false;
    public static boolean prints = true;
    public static int imgSv = 0;
    public static int strtpg = 0;
    public static int pageLimit = 50;
    public static boolean quickMode = false;
    public static String article = "";
    public static String saveTo = "";
	
	
	
    private static final int[] RGB_MASKS = {
        0xFF0000, 0xFF00, 0xFF
    };
    private static final ColorModel RGB_OPAQUE =
        new DirectColorModel(32, RGB_MASKS[0], RGB_MASKS[1], RGB_MASKS[2]);
	
    public static ImageUtilities imageUtil = new ImageUtilities();

    public static String ithWord(String s, int i) {
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

    public static void saveImage(String imageUrl, String destinationFile) throws IOException {

        URL url = new URL(imageUrl);
        InputStream is = url.openStream();
        OutputStream os = new FileOutputStream(destinationFile);

        byte[] b = new byte[2048];
        int length;

        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }

        is.close();
        os.close();

    }

    public static byte[] returnBytes(BufferedImage image) {
        byte[] imageInByte = null;
        try {

            BufferedImage originalImage = image;


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(originalImage, "jpg", baos);
            baos.flush();
            imageInByte = baos.toByteArray();
            baos.close();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return imageInByte;
    }



    public static String addSlashes(String str) {

        for (int itr = 0; itr < str.length(); itr++) {
            if (str.charAt(itr) == '\'') {
                str = str.substring(0, itr) + "\\" + str.substring(itr, str.length());
                itr++;
            }
            if (str.charAt(itr) == '/') {
                str = str.substring(0, itr) + "/" + str.substring(itr, str.length());
                itr++;
            }
        }

        return str;
    }

    public static void scrape() throws IOException, InterruptedException, sun.awt.image.ImageFormatException {



        if (!skipConsole) {
            Scanner scan = new Scanner(System.in);
            System.out.println("Run setup ? (y or n)");
            String response = scan.nextLine();
            while (!response.equalsIgnoreCase("y") && !response.equalsIgnoreCase("n")) {
                System.out.println("Run setup ? (y or n)");
                response = scan.nextLine();
            }
            if (response.equalsIgnoreCase("y")) {
                System.out.println("Enter directory to save images to (leave blank for current directory)");
                saveTo = scan.nextLine();
                //saveTo=addSlashes(saveTo);                                                                                                                                                     
                if (saveTo.length() == 0) {
                    saveTo = imgurParseWin.class.getClassLoader().getResource(".").getPath();
                }
                imgSv = 1;
                response = "";
                while (response == "") {
                    System.out.println("disable printing y or n");
                    response = scan.nextLine();
                    if (response.equalsIgnoreCase("y")) {
                        prints = false;
                    } else {}
                }

                System.out.println("start page (0 is default)");
                strtpg = scan.nextInt();
                System.out.println("use quick mode (only save jpg) y or n");
                response = scan.next();
                if (response.equalsIgnoreCase("y")) {
                    quickMode = true;
                } else {}
                pageLimit = -1;
                while (true) {
                    System.out.println("set page limit (1 page ~ 60 images) 0 for endless scrape");
                    pageLimit = scan.nextInt();
                    if (pageLimit != -1) {
                        break;
                    }
                }

            } else {}


            while (article == "") {
                System.out.print("enter imgur url ");
                article = scan.next();
            }
        }

        // Collect HTML                                                                                                                                                                       
        Document doc = null;
        while (true) {
            System.out.print("\nconnecting . . . ");
            doc = Jsoup.connect( /*"https://imgur.com/"+*/ article).get();
            System.out.print("***link established***\n");
            if (doc.hasText()) {
                break;
            }
            Thread.sleep(5000);
        }
        //	print Title                                                                                                                                                                       
        String title = doc.title();
        if (prints) System.out.println(title);
        String dirName = ithWord(title, 1);

        // Create objects which hold image URLS                                                                                                                                               
        Element content = doc.getElementById("content");
        Elements links = content.getElementsByTag("a");

        // Grab HTML DIV tag which holds image URL                                                                                                                                            
        String htmlChunk = "";
        htmlChunk = doc.getElementsByClass("options").toString();
        htmlChunk = htmlChunk.substring(htmlChunk.indexOf("item nodisplay"), htmlChunk.length());
        htmlChunk = htmlChunk.substring(htmlChunk.indexOf("href=") + 6, htmlChunk.length());
        htmlChunk = htmlChunk.substring(0, htmlChunk.indexOf('"'));

        int counter = strtpg;
        int linksFound = 0;
        Document tempdoc = Jsoup.connect("https://imgur.com" + htmlChunk + "/page/" + counter + "/hit?scrolled").get();
        doc = Jsoup.connect("https://imgur.com" + htmlChunk + "/page/" + counter + "/hit?scrolled").get();
        System.out.println("Connected to " + "https://imgur.com" + htmlChunk + "/page/" + counter + "/hit?scrolled");
        content = doc.body();
        //System.out.println(content);                                                                                                                                                        
        links = content.getElementsByTag("a");
        String linkHref = "";

        ArrayList < String > names = new ArrayList < String > ();

        int repeatCntr = 0;
        while (true) {
            if (counter != 0) {
                while (doc == tempdoc) {
                    try {
                        doc = Jsoup.connect("https://imgur.com" + htmlChunk + "/page/" + counter + "/hit?scrolled").get();
                        Thread.sleep(5000);
                    } catch (java.net.SocketTimeoutException e) {
                        System.out.println(e);
                    } catch (org.jsoup.HttpStatusException me) {
                        System.out.println(me);
                    }
                    content = doc.body();
                    links = content.getElementsByTag("a");
                    counter++;
                    repeatCntr++;
                    if (repeatCntr > 5) {
                        break;
                    }
                }
            } else {
                counter++;
            }
            for (Element link: links) {
                linkHref = link.attr("href");
                //	 System.out.println(linkHref);                                                                                                                                                
                if (linkHref.length() == 1) {
                    if (prints) System.out.println("Bad link");
                } else {
                    if (!names.contains(linkHref) && !linkHref.contains("javascript") && doc != tempdoc && linkHref.length() != 1) {
                        names.add(linkHref);
                        //System.out.println(linkHref + " length " + linkHref.length());                                                                                                     
                        String imgName = linkHref.substring(linkHref.indexOf("m/") + 2, linkHref.length());
                        if (prints) System.out.println("Image name " + imgName);
                        if (imgName == "") {
                            break;
                        }
                        imgName = imgName.substring(imgName.indexOf('/') + 1, imgName.length());
                        imgName = imgName.substring(imgName.indexOf('/') + 1, imgName.length());

                        Element body = null;
                        Elements images = null;
                        int retries = 5;
                        for (int j = 0; j < retries; j++) {
                            try {
                                if (!quickMode) {
                                    doc = Jsoup.connect("https://imgur.com" + linkHref).get();
                                    body = doc.getElementById("image");
                                    images = body.getElementsByTag("img");
                                } else {
                                    try {
                                        body = doc.getElementById(imgName);
                                    } catch (java.lang.IllegalArgumentException e) {
                                        System.out.println(e);
                                        break;
                                    }
                                    images = body.getElementsByTag("img");
                                }
                            } catch (java.net.SocketTimeoutException e) {
                                System.out.println(e);
                            }
                            if (body != null) {
                                break;
                            }
                        }
                        if (body != null) {

                            int fork = 0; //This case covers when the image is a .GIF                                                                                                             
                            if (images.toString().length() == 0) {
                                fork = 1;
                                images = body.getElementsByTag("script");
                            }

                            //	 if(prints)System.out.println();                                                                                                                                      
                            //	 if(prints)System.out.println("step 0 " + body.toString());                                                                                                           
                            //	 if(prints)System.out.println();                                                                                                                                      

                            String dlLink = images.toString();
                            if (fork == 0) {

                                if (prints) System.out.println();
                                if (prints) System.out.println("step 1 " + dlLink);
                                if (prints) System.out.println();

                                dlLink = dlLink.substring(dlLink.indexOf('"') + 3,
                                    dlLink.length());

                                if (prints) System.out.println();
                                if (prints) System.out.println("step 2 " + dlLink);
                                if (prints) System.out.println();
                                if (!quickMode) {
                                    dlLink = "https://" + dlLink.substring(2, dlLink.indexOf('"'));
                                } else {
                                    dlLink = "https://" + dlLink.substring(dlLink.indexOf('"') + 3, dlLink.lastIndexOf('"'));
                                    dlLink = dlLink.substring(0, dlLink.indexOf("b.")) + dlLink.substring(dlLink.indexOf("b.") + 1, dlLink.length());
                                }

                            } else if (fork == 1) {

                                if (prints) System.out.println();
                                if (prints) System.out.println("step 1 " + dlLink);
                                if (prints) System.out.println();

                                dlLink = dlLink.substring(dlLink.indexOf("//") + 2,
                                    dlLink.length());

                                if (prints) System.out.println();
                                if (prints) System.out.println("step 2 " + dlLink);
                                if (prints) System.out.println();

                                dlLink = "https://" + dlLink.substring(2, dlLink.indexOf("',"));

                            }
                            String ext = "";
                            if (!quickMode) {
                                ext = dlLink.substring(dlLink.indexOf(".") + 1, dlLink.length());
                                ext = ext.substring(ext.indexOf(".") + 1, ext.length());
                            } else {
                                ext = dlLink.substring(dlLink.indexOf(".") + 1, dlLink.length());
                                ext = ext.substring(ext.indexOf(".") + 1, ext.length());
                                ext = ext.substring(ext.indexOf(imgName) + imgName.length() + 1, ext.length());
                            }


                            // Create the image object                                                                                                                                           
                            Image image;
                            String path = "";
                            // set download path                                                                                                                                                 
                            if (imgSv == 0) {
                                path = imgurParseWin.class.getClassLoader().getResource(".").getPath();
                            } else if (imgSv == 1) {
                                path = saveTo;
                                path = addSlashes(path);
                            }
                            // If this is the first image create the path for the images                                                                                                        
                            if (linksFound == 0) {
                                new File(path + "//" + dirName + "//").mkdirs();
                            }


                            RenderedImage imageGIF;
                            //Image image = null;                                                                                                                                                
                            if (dlLink.contains("gif")) {
                                System.out.println("GIF:- saving " + dlLink + " into " + (path + "//" + dirName + "//") + imgName + "." + ext);
                                String destinationFile = "image.gif";
                                saveImage(dlLink, (path + "//" + dirName + "//") + imgName + "." + ext);
                            } else {
                                try {
                                    if (prints) System.out.println();
                                    if (prints) System.out.println("step 3 " + dlLink);
                                    if (prints) System.out.println();

                                    URL url = new URL(dlLink);

                                    image = java.awt.Toolkit.getDefaultToolkit().createImage(url);

                                    PixelGrabber pg = new PixelGrabber(image, 0, 0, -1, -1, true);
                                    pg.grabPixels();
                                    int width = pg.getWidth(), height = pg.getHeight();
	
                                    DataBuffer buffer = new DataBufferInt((int[]) pg.getPixels(), pg.getWidth() * pg.getHeight());
                                    WritableRaster raster = Raster.createPackedRaster(buffer, width, height, width, RGB_MASKS, null);
                                    BufferedImage bi = new BufferedImage(RGB_OPAQUE, raster, false, null);




                                    if (prints) System.out.println("Image name " + imgName);

                                    if (prints) System.out.println();                                    //if(prints)System.out.println("step 4 ext:" +ext+ " imageheight:"+image.getHeight());                                                                                                           if (prints) System.out.println();                                    ImageIO.write(bi, ext, new File((path + "//" + dirName + "//") + imgName + "." + ext));                                    linksFound++;                                    if (prints) System.out.println("page " + counter + " pictures downloaded " + linksFound);                                } catch (IOException e) {                                    if (prints) System.out.println(e);                                }                            }                        }                    }                }            }            tempdoc = doc;            if (counter > pageLimit && pageLimit != 0) {                System.out.println("Stopped because counter > " + pageLimit);                break;            }            //	if(repeatCntr>5){ System.out.println("Stopped because repeatCntr > 5 "); break;}                                                                                                          }        System.out.println("repeatCntr" + repeatCntr);        System.out.println("counter " + counter + " : page we ended on");        System.out.println("linksFound " + linksFound);        System.exit(0);        counter = 0;        doc = Jsoup.connect( /*"https://imgur.com/"+*/ article + "#top-tag-container").get();        Element body = doc.body();    }    @    SuppressWarnings("resource")    public static void main(String args[]) throws IOException, InterruptedException, sun.awt.image.ImageFormatException {        scrape();        if (args.length == 0 || args[0] == "0") {            Interface UI = new Interface();        } else if (args[0] == "1") {            scrape();        }    }}