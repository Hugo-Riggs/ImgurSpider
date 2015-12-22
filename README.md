# imgurDownloader

USAGE: (downoad any images off of a /r/imgurdirectory)

java -jar imgur.com/r/natureporn 

TAGS: -qm (quick mode only jpgs), -gifs (only gifs), -v (verbose)
cannot download gifs in quick mode so do not combine -qm with -gifs

Example scrape:
java -jar imgur.com/r/gifs -gifs -v


This java program uses jsoup library for web crawling, specifically the imgur domain directories
