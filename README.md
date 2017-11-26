# Imgur Content Downloader 
## Basic usage with Scala Build Tools:
sbt console

```scala
import io.github.hugoriggs.imgurSpider._
val spdDir = ImgurSpider(Seq("http://imgur.com/r/evilbuildings"))
spdDir.setMaxDownloads(10).run

val spdAlbm = ImgurSpider(Seq("https://imgur.com/a/uAFvn"))
spdAlbm.run

val spdGal = ImgurSpider(Seq("https://imgur.com/gallery/wjkbB"))
spdGal.run
```

Note: you cab get the Scala Build Tools here: [scala-sbt.org/](http://www.scala-sbt.org/).
