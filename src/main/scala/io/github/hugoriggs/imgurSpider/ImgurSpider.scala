package io.github.hugoriggs.imgurSpider

import io.github.hugoriggs.fileprocs.directoryUtils._
import io.github.hugoriggs.fileprocs.jsonParser._
import io.github.hugoriggs.fileprocs._
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.jsoup.Connection
import util.control.Breaks._


object debug{
  private val b: Boolean  = true
  def apply(o: Object) = print(o + "\n")
}

// Used to simplify connecting to URLs
// and to set/get connection values
object ConnectionManager {

  private var timeoutMiliSeconds = 5000

  def setTimeoutSeconds(x: Int) = timeoutMiliSeconds = x * 1000

  def connectTo(url: String): Connection = { 
    var con : Connection = null
    breakable { 
      while(true){
        con = Jsoup.connect(url) 
        if(con != null) {
          debug(s"imgurspider-connection to:  ${url} successful\n")
          break 
        }
        debug("imgurspider-connection: failed to connect to " + url+ "\n")
        debug("Trying again...\n")
        Thread.sleep(timeoutMiliSeconds)
      } 
    }
    con
  }
}


// companion object with referenceable values
object ImgurSpider {

  val helpString = 
    "HELP\n" + 
    "name: imgrsipdr\n" + 
    "usage:\n" +
      "\t[URL]... [OPTIONS]...\t\n" +
    "Options:\n" +
      "\t-s\tstart page\n" +
      "\t-m\tend page" 

  val domain = "https://imgur.com"
  val domain1 = "https://i.imgur.com"
  val directory = Directory(".")
  def apply(s: Seq[String]) = new ImgurSpider(s)
  def apply = println(helpString)
  def setDefaultDirectory(dir: String) = Database.setDefDir(dir)
  Database.setAssociatedAppName("ImgurSpider")
  Database.getDatabase
}

// Spider class
class ImgurSpider(args: Seq[String]) {
  require(args.length > 0 , ImgurSpider.helpString)
  
  import ImgurSpider.{domain, helpString, domain1}
  import PathsAndNames.peelOff

  val seedURL = args.head
  val directoryName = getDirectoryName(seedURL)
  val albumName = getAlbumName(seedURL)

  private val tags = args.tail

  private var pageNumber = 0
  private var unnamedImageCounter = 0
  private var maxPageNumber = 50
  private var downloadCount = 0
  private var maxDownloads = 200
  
  // These values serve a helpful end condition
  // because scrolled pages will continue to 
  // load images that have already been loaded
  private var triedDownloadingExistingImageCounter = 0
  private var triedDownloadingExistingImageCounterMax = 5

  // Save files to this directory
  private var saveDir = ""

  // process arguments
  if(tags.size > 0){
    val whiteSpaceResizeRegex = "[ ]+".r
    tags.foreach(tag =>
      whiteSpaceResizeRegex
        .replaceAllIn(tag.trim, " ")
        .split(" ")
        match {
          case x if x.head == "-s" => pageNumber = x(1).toInt
          case x if x.head == "-m" => maxPageNumber = x(1).toInt
          case x if x.head == "-i" => maxDownloads = x(1).toInt
          case a: Any => println("problem reading argument tags\n" + helpString)
        }
    )
  }


  def setContinueOnExistingFileMax(n: Int): ImgurSpider = {
    triedDownloadingExistingImageCounterMax = n
    this
  }


  def setMaxDownloads(max: Int) = {
    maxDownloads= max
    this
  }


  def setSaveDirectory(dir: String): ImgurSpider ={
    saveDir = dir
    println("save directory set to " + saveDir)
    this
  }


  def getScrollPageURL = domain + directoryName + "/page/" + pageNumber + "/hit?scrolled"

//https://imgur.com/ajaxalbums/getimages/OZ0Xv/hit.json
  def getAlbumJsonUrl = domain + "/ajaxalbums/getimages" + albumName + "/hit.json"
  

  private def getAlbumName(url: String) = {
    val len = peelOff(url.reverse).length
    url.substring(len)
  }

  private def getDirectoryName(url: String) = peelOff(url.reverse, 3).reverse


  private def makeHtmlDoc(url: String): Document = {

    ConnectionManager.connectTo(url).get
  }


  def isUrlDirectory(url : String): Boolean = {
    url.contains("/r/") &&
    peelOff(seedURL, 1).substring(peelOff(seedURL, 1).length-3) == "/r/"
  }


  def isUrlAlbum(url : String): Boolean  = {
    !isUrlDirectory(url) &&
    seedURL.contains("/a/") || seedURL.contains("/gallery/") ||seedURL.contains("/r/")
  }


  def getPageData: (List[String], List[String]) = {

    // functions in scope for conditional blocks below
    import ConnectionManager.connectTo

    def getName(s: String): String = 
      try{ 
        peelOff(s.reverse, 3).reverse.drop(1) 
      }  catch { 
        case a: Any =>
          s
          //s.replaceAll(domain, "").replaceAll(domain1, "") 
      }

    def imgPage(doc: Document) = {

//      val doc = connectTo(seedURL).get
      val imagePage = new ImagePage(doc)
      val imagePageLinks = imagePage.itemList

      var lb = collection.mutable.ListBuffer[String]()
      imagePageLinks.foreach(imageLink => lb += imageLink) 

      val imageLinks = lb.toList
      (imageLinks.map(link => getName(link)), imageLinks)
    }


    def scrollPage(doc: Document)={

      val body = doc.body
      val linkNames = body.getElementsByTag("p")
      val scrolledPage = new ScrolledPage(doc)
      val pageLinks = scrolledPage.itemList

      var lb = collection.mutable.ListBuffer[String]()
      pageLinks.foreach( 
        link => {
          val imagePage = new ImagePage(connectTo(link).get)
          val imageLinks = imagePage.itemList
          imageLinks.foreach(imageLink => 
              lb += imageLink
          )
        }
      )

      val imageLinks = lb.toList
      (imageLinks.map(link => getName(link)), imageLinks)
    }

    if(isUrlDirectory(seedURL)) {
      try{ // try it as a scroll page
        val ret =  scrollPage(makeHtmlDoc(getScrollPageURL))
        pageNumber += 1 // page number affects scrolled pages only
        if(pageNumber-1 == maxPageNumber){
          sys.exit(-1)
        }
        ret
      } catch { case a: Any => // if that doesn't work, its an image page
        imgPage(makeHtmlDoc(seedURL)) 
      }
    }  // ELSE IF ITS A GALLERY (ALREADY AN IMAGE PAGE)
    else if (isUrlAlbum(seedURL)) {
        imgPage(makeHtmlDoc(seedURL))
    } // UNKOWN / UN-HANDLED URL
    else {
      print(s"PARSING: failed to match URL to parsing method")
      (List[String](), List[String]())
    }
  }


  private def readyDirectory(dirName: String){

    import ImgurSpider.directory
    val dirs = dirName.split("/")
    val len = dirs.length
//    var dir = directory
    var dir = Directory(saveDir)
//    var tmpPath = directory.getPath
      var tmpPath = dir.getPath
    for(i <- 0 until len) {
      val successful: Boolean = dir.makeDirectory(dirs(i))
      dir = Directory(tmpPath + dirs(i)) // CHANGE DIRECTORY
      tmpPath = dir.getPath
      if(successful) { println(s"made directory ${dirs(i)}") }
      else { println(s"Could not make directory ${dirs(i)}") }
    }

      setSaveDirectory(dir.getPath)
  }

//  RUN CRAWLER 
  def run = {

    var valueForWallpaperChanger: String = ""

    def subSext(url: String) = url.substring(url.length-4).toLowerCase

    def download(name: String, url: String): Int = {

      def removeThe_r(imagename: String): String = {
        val regularExpression = "r\\.".r
        regularExpression.replaceFirstIn(imagename, ".")
      }

      subSext(url) match {

      case ".jpg" =>
        valueForWallpaperChanger = saveDir + name
        Downloader.imageFromURL(url, name.dropRight(4), ".jpg", saveDir)
      case "jpeg" =>
        valueForWallpaperChanger = saveDir + removeThe_r(name)
        Downloader.imageFromURL(url, name.dropRight(4), ".jpg", saveDir)
      case ".png" =>
        valueForWallpaperChanger = saveDir + name
        Downloader.imageFromURL(url, name.dropRight(4), ".png", saveDir)
      case ".mp4" => Downloader.mp4FromURL( url, name.dropRight(4), saveDir)
      case ".gif" => Downloader.gifFromURL( url, name.dropRight(4), saveDir)
      case "gifv" => Downloader.mp4FromURL( url, name.dropRight(4), saveDir)
      case a: Any =>
        println("ERROR MATCHING EXTENTION TO DOWNLOADER ext="+subSext(url))
        -1
    }
  }

    readyDirectory(directoryName.drop(1))

    breakable { 
      var doBreak = false
      while(true) {

      if(doBreak) break // crawler kill switch

      val NamesandURL = try {
        val (names, url) = getPageData // gathers file URL/Names
        names.zip(url)  // zip data together
      } catch { 
        case e: Exception => // (html parsing exception)
          doBreak = true    // ensure break out on next iteration
          List[(String, String)]()  // Return empty 
      }

      breakable { NamesandURL.foreach({
        case (name, url) => 
          try{ 
            // download function returns:[image already downloaded -> 0, 
            // downloaded image -> 1, exception -> -1]
            val result= download(name, url) 
            if(result == 0) { // image already downloaded
              triedDownloadingExistingImageCounter+= 1 
              print(s"FAILED DOWNLOAD: File named ${name} was not downloaded because it already exists.\n")
            } else if(result == 1) { // downloaded new image
              downloadCount+= 1
              print(s"imgurspider-download-confirmed: File ${name}\n")
            }
            // Tried download the same image too many times so quit crawling
            if(triedDownloadingExistingImageCounter >= 
                triedDownloadingExistingImageCounterMax ) { 
              doBreak=true
              println("STOPPING: tried to download an existing file too many times") 
              break
            }
            if(downloadCount >= maxDownloads) {
              doBreak=true 
              print("STOPPING: max downloads reached\n") 
              break
            }
          } catch { case e: Exception => print(e) }
      }) } // inner break 
    } } // outer break

    println("valueForWallpaperChanger " + valueForWallpaperChanger.replaceAll("/", "\\\\"))
    WallpaperChanger.main(Array(valueForWallpaperChanger.replaceAll("/", "\\\\")))
  } // end
  

  // this class gathers URLs (single image/galleries)
  // from directory pages(URLs) on imgur
  class ScrolledPage(doc: Document) {

    def itemList = {

      // values: string, jsoup members, list buffer
      import ImgurSpider.domain
      val elements = doc.getElementsByTag("a")
      val it = elements.iterator
      val lb = collection.mutable.ListBuffer[String]()

        // iteratively create URL in the buffer
      while(it.hasNext) {
        val href = it.next.attr("href")
        if(href.contains("/r/"))
          lb += domain+href
      }
      lb.toList
    }
  }

  // This class gets image/video URLs from a display page
  // whether it be a single image or gallery. 
  // Although gifs are technically image files,
  // they are converted by imgur into mp4's (gifv)
  // a video format.
  class ImagePage(doc: Document) {

    def itemList: List[String] = {

      val lb = collection.mutable.ListBuffer[String]()

      // Expand a album before parsing
      val truncatedKey = "js-post-truncated"
      val jsButton = doc.getElementsByClass(truncatedKey)
  //    if(jsButton.length > 0) {
  //      // handle load more images
        // switch to grid view,
  //      doc.
  //    }

      if(doc.location.contains("/gallery/") || doc.location.contains("/a/")){
        val doc1 = org.jsoup.Jsoup.connect(getAlbumJsonUrl).ignoreContentType(true).get
        val jsonString = doc1.getElementsByTag("body").toString.drop(6).dropRight(7)
//        print(s"start parsing string ${jsonString}\n")
        val ja = JsonParser.jsonStringToMap(jsonString)
        ja("data")("images").getList.foreach(a => lb += (domain1 + "/"+a("hash") + a("ext")))
        return lb.toList
      }
      
      // values: jsoup members, list buffer, string
      import ImgurSpider.domain1
      val postImages = doc.getElementsByClass("post-image")
      val postImagesIterator = postImages.iterator
        // goes through all images 
        // if it detects a video container
        // it forks the process to handle it
      while(postImagesIterator.hasNext) {
        val next = postImagesIterator.next
        if(next.getElementsByClass("video-container").size > 0){
          lb += "https:" + next.getElementsByTag("source").attr("src")
        } else {
          lb += "https:" + next.getElementsByTag("img").attr("src")
        }
      }
      lb.toList
    }
  }
}
