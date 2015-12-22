package imgurParse;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class scrapeFunctions extends imgurParse {
	// Return the ith word of a String.
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
	 	 
		public static String addSlashes(String str){
			for(int itr=0;itr<str.length();itr++){
			   if(str.charAt(itr)=='\''){str = str.substring(0, itr) + "\\" + str.substring(itr, str.length()); itr++;}
			   if(str.charAt(itr)=='/'){str = str.substring(0, itr) + "/" + str.substring(itr, str.length()); itr++;}
			}
			return str;
		}

		private static Document htmlGrab() throws IOException, InterruptedException {
			Document doc=null;
			while(true){
				System.out.print("\nconnecting to " + g_article  + " . . .  ");
				try{
				doc = Jsoup.connect(g_article).get();
				} catch(Exception e){System.out.println(e);}
				if(doc!=null){
					System.out.print("***link established***\n");
					break;
				}
				Thread.sleep(5000);
			}
			return doc;
		}
		
	   
}
