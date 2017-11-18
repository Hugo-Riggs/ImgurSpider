//package main.java.io.github.HugoRiggs.ImgurSpider;
package io.github.hugoriggs.imgurSpider;

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


// imports for Graphical User Interface
import javax.imageio.ImageIO;

/**
 * Created by hugo on 1/26/17.
 */

public class Downloader {

    public static int gifFromURL(String dlLink, String imageName, String path){
        try {
            byte[] b = new byte[1];
            URL url = new URL(dlLink);
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();
            DataInputStream di = new DataInputStream(urlConnection.getInputStream());

            FileOutputStream fo = new FileOutputStream(path + imageName + ".gif");
            while (-1 != di.read(b, 0, 1)) {
                fo.write(b, 0, 1);
            }
            di.close();
            fo.close();

            } catch (MalformedURLException malURL) {
                System.out.println("bad url " + malURL);
				return -1;
			} catch (IOException ioEX) {
				System.out.println("io ex, maybe file not found" + ioEX);
				return -1;
			}
        System.out.print("downloader-success: " + dlLink +"\n");
			
		return 1;
    }


	// 1 -> image written, 0 -> image already existed, -1 -> other exception
    public static int mp4FromURL(String dlLink, String imageName, String path){
        try {
            byte[] b = new byte[1];
            URL url = new URL(dlLink);
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();
            DataInputStream di = new DataInputStream(urlConnection.getInputStream());

//            FileOutputStream fo = new FileOutputStream(path + imageName + ".gif");
            FileOutputStream fo = new FileOutputStream(path + imageName + ".mp4");
            while (-1 != di.read(b, 0, 1)) {
                fo.write(b, 0, 1);
            }
            di.close();
            fo.close();

            } catch (MalformedURLException malURL) {
                System.out.println("bad url " + malURL);
				return -1;
			} catch (IOException ioEX) {
				System.out.println("io ex, maybe file not found" + ioEX);
				return -1;
			}
//        System.out.println("image " + imageName + ".gif" );
        System.out.println("downloader-success: " + dlLink );
			
		return 1;
    }

	// 1 -> image written, 0 -> image already existed, -1 -> other exception
    public static int imageFromURL(String dlLink, String imageName, String ext, String path){    
		File file = new File(path + imageName +  ext); //  edit
		if (!file.exists()) {                          //  edit
			final int[] RGB_MASKS = {0xFF0000, 0xFF00, 0xFF};
			final ColorModel RGB_OPAQUE =
				new DirectColorModel(32, RGB_MASKS[0], RGB_MASKS[1], RGB_MASKS[2]);

			// Create the image object
			Image image;
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

//			System.out.println("image " + imageName + ext + " Width " + width
//			+ " height " + height);

			if(width<0||height<0){System.out.println("breaking because image width or height was invalid.");}

			DataBuffer buffer = new DataBufferInt((int[]) pg.getPixels(), pg.getWidth() * pg.getHeight());

			WritableRaster raster = Raster.createPackedRaster(buffer, width, height, width, RGB_MASKS, null);

			BufferedImage bi = new BufferedImage(RGB_OPAQUE, raster, false, null);

	//        File file = new File(path + imageName +  ext);
	//        if (!file.exists()) {
			  try {
				  ImageIO.write(bi, ext.substring(1), file);
				  System.out.print("downloader-success: "+dlLink+"\n");
				  return 1;
			  } catch (IOException ioEx){
				  System.out.println("error writing image " + ioEx);
				  return -1;
			  }
        } else {
            System.out.println("File existed already");
			return 0;
        }

    }
}
