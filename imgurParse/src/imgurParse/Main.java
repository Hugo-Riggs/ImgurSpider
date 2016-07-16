package imgurParse;

import java.io.IOException;

class Main {
    public static void main(String args[]) throws IOException, InterruptedException {

        ImgurSpider spider;
        spider = ImgurSpider.getInstance("Imgur Spider");

        final GUI gui;
        if(args.length == 0)
            gui = new GUI(spider);	// Pass the spider object into the graphical user interface.

        spider.start(args);  // non GUI version
    }
}

