# ImgurSpider

![imgurspider](https://cloud.githubusercontent.com/assets/8892714/14621246/717190d8-058f-11e6-852b-16ad18510ba2.png)

Description: 
  Downoad any images off of a /r/imgurdirectory ex:(http://imgur.com/r/FifthWorldPics).
  There is a command line .jar file and a GUI .jar file, and they are compiled
  and ready to run on a machine with java.
  
Usage:<br>
  java -jar [URL] [TAGS]
  
ex:<br>
  java -jar imgur.com/r/wallpapers -qm

URL:<br>
  Any URL on the imgur.com domain which appears as (/r/"something"). This was built with reddit in mind.

TAGS:<br>
  -qm (quick mode only jpgs), -gifs (only gifs), -v (verbose)
cannot download gifs in quick mode so do not combine -qm with -gifs

Example scrape:<br>
  java -jar imgur.com/r/gifs -gifs -v

Libraries used<br>
  This java program uses jsoup library for web crawling, using various jsoup API instructions.
  Java standard libraries are used for data structures, and graphics.
