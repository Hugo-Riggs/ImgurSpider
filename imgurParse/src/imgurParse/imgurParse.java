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
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
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

class GUI extends JFrame {
	
    private Thread t;
    private String threadName;

    private javax.swing.JTextField URL;

    private JCheckBox check;
    private ImgurSpider spider;
    
	// Constructor for GUI:
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
        javax.swing.JLabel URLlabel = new javax.swing.JLabel();	 // initialize a label

		URL = new javax.swing.JTextField();

		javax.swing.JButton startButton = new javax.swing.JButton(); // initialize a button

		URLlabel.setText("URL: (example: http://imgur.com/r/wallpapers)");
		URLlabel.setBounds(4, 4, 300, 20);
		
		URL.setText("");
		URL.setBounds(4,20, 160, 20);
		
		check = new JCheckBox("source are GIFs");
		check.setBounds(165,20,165,20);
		
		startButton.setText("Start");
		startButton.setBounds(4,40,80,30);
		
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
        this.setVisible(true);
	}
	
    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) throws IOException, InterruptedException {
    	String url=URL.getText();
    	String[] args = {url, "", "", ""};
    	if(check.isSelected())
    		args[1]="-gifs";
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
	private static ImgurSpider spider;

    // Jsoup variables
    private static Document doc;
    private static Document tempDoc;
    private static Element content;
    private static Elements links;

    private static String dirName;

     // Global object variables
    private static int g_strtpg=0;
    private static boolean g_prints=true;
    private static boolean g_quickMode = true;
    private static boolean g_gifs=false;
    private static String g_article="";
    private static String g_htmlChunk="";
    private static String[] args;

    // Graphics constants
    private static final int[] RGB_MASKS = {0xFF0000, 0xFF00, 0xFF};
    private static final ColorModel RGB_OPAQUE =
    new DirectColorModel(32, RGB_MASKS[0], RGB_MASKS[1], RGB_MASKS[2]);


	public static ImgurSpider getInstance(String n){
		if(spider == null){
			spider = new ImgurSpider(n);
		}

		return spider;
	}


	private ImgurSpider(String threadName) {
		this.threadName=threadName;
	}

	private ImgurSpider() {
        System.out.println("Thread name not set"); // Place this command in a thread message eventually.
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

/*
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
	  */
/*
	 	// This is important for creating directories in windows or unix 
		public static String addSlashes(String str){
			for(int itr=0;itr<str.length();itr++){
			   if(str.charAt(itr)=='\''){str = str.substring(0, itr) + "\\" + str.substring(itr, str.length()); itr++;}
			   if(str.charAt(itr)=='/'){str = str.substring(0, itr) + "/" + str.substring(itr, str.length()); itr++;}
			}
			return str;
		}
	*/
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

			while(true){ // While they have not entered a necessary argument.
			if(args.length==0){ // No argument print a helpful message
				printHelp();
				System.exit(0);
			}
			else 
				break;
			}

			for(int i=0;i<args.length;i++){ // Read the arguments for setting program flags
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
			if (!g_article.contains("http")){g_article="http://"+g_article;}
			
			doc = htmlGrab();
			if(g_prints)System.out.println(doc.title());
			// Get directory name for saving locally
			dirName = ithWord(doc.title(), 1);
			
			// Create element objects which are used in html string parsing
			content = doc.getElementById("content");
			links = content.getElementsByTag("a");
			
			// Grab HTML DIV tag which holds image URL
			String localReference = selectURL(doc);
			
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

	// When the thread is created this method runs first.
	// Create a loop which breaks on thread interupt.
	// A thread interrupt is triggered by the user when they
	// close the Swing window.
	public void run(){

		// The image scraping continues while the thread has not been interrupted.
        while (!Thread.currentThread().isInterrupted()) {
        	
            try {
				readArgs(args);
			} catch (IOException | InterruptedException e1) {
				e1.printStackTrace();
			}
			
			int repeatCntr=0;
			int linksFound=0;
			String linkHref = "";
			ArrayList<String> names = new ArrayList<>();

			// Always try this next loop.
			while(true){
				//
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
						catch(org.jsoup.HttpStatusException e){
                            e.printStackTrace();
						} catch (IOException e) {
                            e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

                        content = doc.body();
                        links = content.getElementsByTag("a");
                        g_strtpg++;
                        repeatCntr++;
                        if(repeatCntr>5){break;}
					}
				}else{g_strtpg++;}

				// 	Go through links we found in the HTML
				for (Element link : links) {
					try{
					 linkHref = link.attr("href");
                     if(!names.contains(linkHref)&&!linkHref.contains("javascript")&&doc!=tempDoc){
						 names.add(linkHref);
						 String imgName = linkHref.substring(linkHref.indexOf("m/")+2, linkHref.length());
					      if(g_prints)System.out.println("Image name " + imgName);
					      
					      // only run majority of code below if image has a name
					      if(!imgName.equals("")){
					    	  
					      imgName = imgName.substring(imgName.indexOf('/')+1, imgName.length() );
					      imgName = imgName.substring(imgName.indexOf('/')+1, imgName.length());

					      String tmp=""; // tmp -> used to hold a to string value of the images Elements variable.
					      int isGIF; //This case covers when the image is a .GIF

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
						 isGIF = 0;//This case covers when the image is a .GIF
                         if(images!=null && images.toString().length()==0){	// From html, determines that the image is a .jpeg
							 isGIF=1;
							 if(body!=null){
								 images = body.getElementsByTag("script");
							 }
						 }
					      if(!(images==null))
						 	tmp=images.toString();

					      String dlLink = tmp;
						 if(dlLink.length()==0){break;}
						 if(isGIF==0){
							 if(g_prints){	System.out.println("\nstep 1 " + dlLink);	}
							 dlLink = dlLink.substring(dlLink.indexOf('"')+3, dlLink.length());
							 if(g_prints){	System.out.println("\nstep 2 " +dlLink+"\n");	}
							 if(!g_quickMode){dlLink = "https://"+dlLink.substring(2, dlLink.indexOf('"'));}
							 else {dlLink = "https://"+dlLink.substring(dlLink.indexOf('"')+3, dlLink.lastIndexOf('"'));
							 dlLink = dlLink.substring(0, dlLink.indexOf("b."))+dlLink.substring(dlLink.indexOf("b.")+1, dlLink.length());}
						 }else if(isGIF==1){
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
                                    int g_imgSv=0;// TODO: this could be if, g_saveTo is not null save it there. To get rid of this var
									String g_saveTo="";// TODO: add a method to get this location, or prompt
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
				} // END FOR loop, (which looks at individual links on the page)

				tempDoc=doc;

				int g_pageLimit=50;// to local
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

	// The arguments string is set when the thread starts.
	// See if it is still possible to call this code through the main code.
   public void start (String[] args) throws IOException, InterruptedException
   {
	  if(args==null)
		  printHelp();
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
		
		// The ImgurSpider object is a Singleton.
		ImgurSpider spider;
		spider = ImgurSpider.getInstance("Imgur Spider");

		final GUI gui = new GUI(spider);	// Pass the spider object into the graphical user interface.

//		spider.start(args);  // non GUI version
	}
	
	
}