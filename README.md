# imgurDownloader

![imgurspider](https://cloud.githubusercontent.com/assets/8892714/14621246/717190d8-058f-11e6-852b-16ad18510ba2.png)

Description: 
  Downoad any images off of a /r/imgurdirectory (http://imgur.com/r/FifthWorldPics).
  There is a command line .jar file and a GUI .jar file, and they are compiled
  and ready to run on a machine with java.
  
Usage:
  java -jar [URL] [TAGS]
  
ex:
  java -jar imgur.com/r/wallpapers -qm

URL:
  Any URL on the imgur.com domain which appears as (/r/"something"). This was built with reddit in mind.

TAGS:
  -qm (quick mode only jpgs), -gifs (only gifs), -v (verbose)
cannot download gifs in quick mode so do not combine -qm with -gifs

Example scrape:
  java -jar imgur.com/r/gifs -gifs -v

Libraries used
  This java program uses jsoup library for web crawling, using various jsoup API instructions.
  Java standard libraries are used for data structures, and graphics.
