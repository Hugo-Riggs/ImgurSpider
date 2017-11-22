<hr>
# Imgur Content Downloader 
<hr>
## Basic usage with Scala Build Tools:
<hr>
sbt console

```scala
import io.github.hugoriggs.imgurSpider._
val spdDir = ImgurSpider(Seq("http://imgur.com/r/evilbuildings"))
spdDir.setMaxDownloads(10).run

val spdAlbm = ImgurSpider(Seq("https://imgur.com/a/uAFvn"))
spdAlbm.run

```
<hr>
