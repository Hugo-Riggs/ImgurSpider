package imgurParse;


// imports for image graphics 
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JCheckBox;
// imports for Graphical User Interface
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UnsupportedLookAndFeelException;

//imports for web scraping
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class GUI extends JFrame {
	
   private Thread t;
   private String threadName;
	
	private javax.swing.JLabel URLlabel;
    private javax.swing.JTextField URL;
    private javax.swing.JButton jButton1;
    private JCheckBox check, check1;
    ImgurSpider spider;
    
	//Constructor:
	public GUI(ImgurSpider spider ) {
		this.spider=spider;
		initComponents();
	}
	

	private void initComponents(){
		setTitle("Imgur Spider");
		setSize(300,150);
		setLocation(10,200);
		
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		
		this.setLayout(null);
		
		URLlabel = new javax.swing.JLabel();
		URL = new javax.swing.JTextField();
		jButton1 = new javax.swing.JButton();
		
		URLlabel.setText("URL: (example: http://imgur.com/r/wallpapers)");
		URLlabel.setBounds(4, 4, 300, 20);
		
		URL.setText("");
		URL.setBounds(4,20, 160, 20);
		
		check = new JCheckBox("source are GIFs");
		check.setBounds(165,20,165,20);
		
		jButton1.setText("Start");
		jButton1.setBounds(4,40,80,30);
		
		jButton1.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					jButton1ActionPerformed(evt);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		
        
        add(URL);
        add(URLlabel);
        add(jButton1);
        add(check);
        this.setVisible(true);
	}
	
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) throws IOException, InterruptedException {
    	String url=URL.getText();
    	String[] args = {url, "", "", ""};
    	if(check.isSelected())
    		args[1]="-gifs";
    	spider.start(args);
	}
    
    
	
}// end GUI class


			
			
/////////////////////////////
//// Parse a imgur page for its images
////
////

//	http://jsoup.org/cookbook/
//	http://jsoup.org/apidocs/

	class ImgurSpider implements Runnable {
	
   private Thread t;
   private String threadName;

   
   // Jsoup variables
   static Document doc;
   static Document tempDoc;
   static Element content;
   static Elements links;
   
   static String dirName;
   static String localReference;
   
   
	// Global scrape variables
	static int g_imgSv=0;
	private static int g_strtpg=0;
	static int g_pageLimit=50;
	static boolean g_prints=false;
	static boolean g_quickMode = true;
	static boolean g_gifs=false;
	static String g_saveTo="";
	static String g_article="";
	static String g_htmlChunk="";
	static String[] args;
	// Graphics constants
	private static final int[] RGB_MASKS = {0xFF0000, 0xFF00, 0xFF};
	private static final ColorModel RGB_OPAQUE =
	new DirectColorModel(32, RGB_MASKS[0], RGB_MASKS[1], RGB_MASKS[2]);
	
	public ImgurSpider(String threadName) {
		this.threadName=threadName;
	}


	// Return the i-th word of a String.
	   public static String ithWord(String s, int i) {
		   try {
			   String tmp = s;
			   String ithword = "";
		   for (int j = 0; j < i; j++) {
		         if (tmp.indexOf(" ") == -1)
		            ithword = tmp.substring(0, tmp.length());
		         else {
		            ithword = tmp.substring(0, tmp.indexOf(" "));
		            tmp = tmp.substring(tmp.indexOf(" ")+1, tmp.length());
		         }
		      }
		   return ithword;
	   }
	   catch (Exception e) {
		   System.out.println("Invalid string: "+s);
		   return "";
		   }
	   }	
	   
	   
	   public static byte[] returnBytes(BufferedImage image){
		   byte[] imageInByte = null;
		   try{
				BufferedImage originalImage = image;
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write( originalImage, "jpg", baos );
				baos.flush();
				imageInByte = baos.toByteArray();
				baos.close();
			}catch(IOException e){
				System.out.println(e.getMessage());
			}		
		   
		   return imageInByte;
	   }
	   
	 	// This is important for creating directories in windows or unix 
		public static String addSlashes(String str){
			for(int itr=0;itr<str.length();itr++){
			   if(str.charAt(itr)=='\''){str = str.substring(0, itr) + "\\" + str.substring(itr, str.length()); itr++;}
			   if(str.charAt(itr)=='/'){str = str.substring(0, itr) + "/" + str.substring(itr, str.length()); itr++;}
			}
			return str;
		}
		
		// Print that we are attempting to connecto to a URL.
		// Continue to try every 5 seconds until a connection is made.
		private static Document htmlGrab() throws IOException, InterruptedException {
			Document doc=null;
			while(true){
				
				System.out.print("\nconnecting to " + g_article  + " . . .  ");
				try{
				doc = Jsoup.connect(g_article).get();
				} catch(Exception e){System.out.println(e);}
				if(doc!=null){
					System.out.print("***connected***\n");
					break;
				}
				Thread.sleep(5000);
			}
			return doc;
		}
	
		
		
		// Process initial user input
		private static void readArgs(String[] args) throws IOException, InterruptedException {
			while(true){
			if(args.length==0){
				printHelp();
				System.exit(0);
			}
			else 
				break;
			}
			for(int i=0;i<args.length;i++){
				switch(args[i]){
					case "-qm":
						g_quickMode=true;
						break;
					case "-v":
						g_prints=true;
						break;
					case "-gifs":
						g_gifs=true;
						break;
				}
			}
			if(args.length!=0){for(int i = 0; i < args.length; i++){if(args[i].contains("http:"))g_article=args[i];}}
			if(g_article.contains("http")){}else if (!g_article.contains("http")){g_article="http://"+g_article;}
			
			doc = htmlGrab();
			if(g_prints)System.out.println(doc.title());
			// Get directory name for saving locally
			dirName = ithWord(doc.title(), 1);
			
			// Create element objects which are used in html string parsing
			content = doc.getElementById("content");
			links = content.getElementsByTag("a");
			
			// Grab HTML DIV tag which holds image URL
			localReference = selectURL(doc);
			
			//Establish connection with the image server , create a swap connection
			tempDoc = Jsoup.connect("https://imgur.com"+localReference+"/page/"+g_strtpg+"/hit?scrolled").get();
			doc = Jsoup.connect("https://imgur.com"+localReference+"/page/"+g_strtpg+"/hit?scrolled").get();
			
			// Fill element with html body
			content = doc.body();
			
			// Fill element with url's found in the body
			links = content.getElementsByTag("a");
			
			// feed these variables to the runScrape function which 
			// downloads and saves images (.jpg,.gif,.png ...)
			//runScrape(dirName, links, content, doc, tempdoc);
		}
		
		// Proper command line usage instruction.
		private static void printHelp(){ System.out.println("usage:\n\t[URL] [tags]\ntags:\n-gifs -> for gif images\n -v -> for more text output\n -qm -> quick mode for non-gifs only"); }
		
		// Part of the initial scraping process
		private static String selectURL(Document doc) {
			g_htmlChunk = doc.getElementsByClass("options").toString();
			g_htmlChunk = g_htmlChunk.substring(g_htmlChunk.indexOf("item nodisplay"), g_htmlChunk.length());
			g_htmlChunk = g_htmlChunk.substring(g_htmlChunk.indexOf("href=")+6,g_htmlChunk.length());
			g_htmlChunk = g_htmlChunk.substring(0,g_htmlChunk.indexOf('"'));
			return g_htmlChunk;
		}
		



	//private static void runScrape(String dirName, Elements links, Element content, Document doc, Document tempdoc) throws IOException, InterruptedException {
	// The lengthy scrape code which parses the imgur html server text for image files, and downloads them
	public void run(){
        while (!Thread.currentThread().isInterrupted()) {
        	
            try {
				readArgs(args);
			} catch (IOException | InterruptedException e1) {
				e1.printStackTrace();
			}
			
			int repeatCntr=0;
			int linksFound=0;
			String linkHref = "";
			ArrayList<String> names = new ArrayList<String>();
			while(true){
				if(Thread.currentThread().isInterrupted()) {
			        System.out.println("Thread interrupted\n Exiting...");
			        break;
			    }
				if(g_strtpg!=0){
					while(doc==tempDoc){
						try{
								doc = Jsoup.connect("https://imgur.com"+g_htmlChunk+"/page/"+g_strtpg+"/hit?scrolled").get();
								// print url 
								System.out.println("https://imgur.com"+g_htmlChunk+"/page/"+g_strtpg+"/hit?scrolled");
								Thread.sleep(5000);
						} catch(java.net.SocketTimeoutException e) {
							System.out.println(e);
						}
						catch(org.jsoup.HttpStatusException me){
							System.out.println(me);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					content = doc.body();
					links = content.getElementsByTag("a");
					g_strtpg++;
					repeatCntr++;
					if(repeatCntr>5){break;}
					}
				}else{g_strtpg++;}
			//	boolean outOfFor=false;
				for (Element link : links) {
					try{
					 linkHref = link.attr("href");
					 //if(linkHref.equals("/")||linkHref.equals("javascript:;")){System.out.println("broken image");outOfFor=true;break;}if(outOfFor)break;
					 //System.out.println("for looped " + linkHref);
					 if(!names.contains(linkHref)&&!linkHref.contains("javascript")&&doc!=tempDoc){
						 names.add(linkHref);
						 String imgName = linkHref.substring(linkHref.indexOf("m/")+2, linkHref.length());
					      if(g_prints)System.out.println("Image name " + imgName);
					      
					      // only run majority of code below if image has a name
					      if(!imgName.equals("")){
					    	  
					      imgName = imgName.substring(imgName.indexOf('/')+1, imgName.length() );
					      imgName = imgName.substring(imgName.indexOf('/')+1, imgName.length());

					      // LOCAL GLOBAL VARIABLES
					      String tmp="";
					      int fork;
					    {
						 Element body = null;
						 Elements images=null;
						 
						 if(!g_quickMode){
							 doc = Jsoup.connect("https://imgur.com"+linkHref).get();
							 System.out.println("https://imgur.com"+linkHref);
							 String id=linkHref.substring(3);
							 id=id.substring(id.indexOf("/")+1);
							 body = doc.getElementById(id);
							 if(!g_gifs){
								 images = body.getElementsByTag("img");
							 }else{
								 images=body.getElementsByAttribute("video/webm");
							 }
						 }else{
							 if(!imgName.equals("")){
								 body = doc.getElementById(imgName);
								 images = body.getElementsByTag("img");
							 }
						 }
						 fork = 0;//This case covers when the image is a .GIF
						 if(images!=null && images.toString().length()==0){
							 fork=1;
							 if(body!=null){
								 images = body.getElementsByTag("script");	
							 }
						 }
					      if(!(images==null))
						 	tmp=images.toString();
					      }
					      String dlLink = tmp;
						 if(dlLink.toString().length()==0){break;}
						 if(fork==0){
							 if(g_prints){	System.out.println("\nstep 1 " + dlLink);	}
							 dlLink = dlLink.substring(dlLink.indexOf('"')+3, dlLink.length());
							 if(g_prints){	System.out.println("\nstep 2 " +dlLink+"\n");	}
							 if(!g_quickMode){dlLink = "https://"+dlLink.substring(2, dlLink.indexOf('"'));}
							 else {dlLink = "https://"+dlLink.substring(dlLink.indexOf('"')+3, dlLink.lastIndexOf('"'));
							 dlLink = dlLink.substring(0, dlLink.indexOf("b."))+dlLink.substring(dlLink.indexOf("b.")+1, dlLink.length());}
						 }else if(fork==1){
							 if(g_prints){	System.out.println("\nstep 1 " + dlLink+"\n");	}
							 dlLink = dlLink.substring(dlLink.indexOf("//")+2,dlLink.length());
							 if(g_prints){	System.out.println("\nstep 2 " +dlLink+"\n");	}
							 dlLink = "https://"+dlLink.substring(2, dlLink.indexOf("',"));	 
						 }
						 // Create the image object
						 Image image;
						 //Image image = null;
							try{
								if(dlLink.contains("?1")){dlLink=dlLink.substring(0,dlLink.indexOf("?1"));}
								 String ext="";
								 if(!g_quickMode){
								 ext = dlLink.substring(dlLink.indexOf(".")+1, dlLink.length());
								 ext = ext.substring(ext.indexOf(".")+1, ext.length());
								 }else{
									 ext = dlLink.substring(dlLink.indexOf(".")+1, dlLink.length());
									 ext = ext.substring(ext.indexOf(".")+1, ext.length());
									 ext = ext.substring(ext.indexOf(imgName)+imgName.length()+1, ext.length());
								 }
								if(g_prints){	System.out.println("\nstep 3 " +dlLink+"\n");	}
								if(g_gifs){
									 byte[] b = new byte[1];
									    URL url = new URL(dlLink);
									    URLConnection urlConnection = url.openConnection();
									    urlConnection.connect();
									    DataInputStream di = new DataInputStream(urlConnection.getInputStream());
									    FileOutputStream fo = new FileOutputStream(imgName+".gif");
									    while (-1 != di.read(b, 0, 1))
									    	{fo.write(b, 0, 1);}
									    di.close();
									    fo.close();
								}else{
									URL url = new URL(dlLink);
									image = java.awt.Toolkit.getDefaultToolkit().createImage(url);
									PixelGrabber pg = new PixelGrabber(image, 0, 0, -1, -1, true);
									pg.grabPixels();
									int width = pg.getWidth(), height = pg.getHeight();
									if(width<0||height<0){System.out.println("breaking because image width or height was invalid.");break;}
									DataBuffer buffer = new DataBufferInt((int[]) pg.getPixels(), pg.getWidth() * pg.getHeight());
									WritableRaster raster = Raster.createPackedRaster(buffer, width, height, width, RGB_MASKS, null);
									BufferedImage bi = new BufferedImage(RGB_OPAQUE, raster, false, null);
									String path="";    
							      if(g_imgSv==0){
							    	  path = ImgurSpider.class.getClassLoader().getResource(".").getPath();
							      }
							      else if(g_imgSv==1){ 
							    	  path = g_saveTo; 
							      }
							      if(g_prints)System.out.println("Image name " + imgName);
							      if(!imgName.equals("")){
								      // If this is the first image create the path for the images
								      if(linksFound==0) {
								    	  new File(path+"//"+dirName+"//").mkdirs(); 
								      }
								      // test to see if a file exists
								      File file = new File((path+"//"+dirName+"//")+imgName+"."+ext);
								      if (!file.exists())
								      {
								    	  ImageIO.write(bi, ext, new File((path+"//"+dirName+"//")+imgName+"."+ext));
								    	  linksFound++;
								    	  
								      }
								      if(g_prints)System.out.println("page " + g_strtpg + " pictures downloaded " + linksFound);
							      }
								}
						  } catch (Exception e) {
							  if(g_prints)System.out.println(e);
						  }
					      }else{
					    	  System.out.println("broken image");
					    	//  System.out.println(doc.toString());
					    	  System.out.println("https://imgur.com"+g_htmlChunk+"/page/"+g_strtpg+"/hit?scrolled");
					      }//end if for empty image string
					 }//end if (!names.contains(linkHref)&&!linkHref.contains("javascript")&&doc!=tempdoc)
			} catch (Exception e) {
					  if(g_prints)System.out.println(e);
					}
				} // END FOR loop

				tempDoc=doc;
				if(g_strtpg>g_pageLimit&&g_pageLimit!=0)
				{ 
					System.out.println("Stopped because current page number " + g_strtpg + " is > limit: " + g_pageLimit);
					break;
				}
			}
			System.out.println("repeatCntr" + repeatCntr);
			System.out.println("g_strtpg " + g_strtpg + " : page we ended on");
			System.out.println("linksFound " + linksFound);
        }
	
	}

	
	   public void start (String[] args) throws IOException, InterruptedException
	   {
		   this.args = args;
	      System.out.println("Starting " +  threadName );
	      if (t == null)
	      {
	         t = new Thread (this, threadName);
	         t.start ();
	      }
	   }


	
}
	
class test{

	
	public static void main(String args[]) throws IOException, InterruptedException  {
		

		ImgurSpider spider = new ImgurSpider("spider");

		final GUI gui = new GUI(spider); 

	}
	
	
}