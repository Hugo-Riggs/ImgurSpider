package test;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Created by hugo on 1/26/17.
 */

import main.java.io.github.HugoRiggs.ImgurSpider.*;
import java.io.IOException;

class SimpleRun {
    public static void main(String args[]) throws IOException, InterruptedException  {

        // The Crawler object is a Singleton.
        Crawler crawler;
        crawler = Crawler.getInstance("Imgur Spider");

        crawler.start(args);
    }
}

class GuiRun{
     public static void main(String args[]) throws IOException, InterruptedException  {

        Crawler crawler;
        crawler = Crawler.getInstance("Imgur Spider");

		final GUI gui = new GUI(crawler);	// Pass the crawler object into the graphical user interface.
    }
}

class DownloaderImageRun {
     public static void main(String args[]) throws IOException, InterruptedException  {

         Downloader.imageFromURL("http://i.imgur.com/tDCGkey.jpg", "test", ".jpg", "/home/hugo/Downloads/");
    }
}
