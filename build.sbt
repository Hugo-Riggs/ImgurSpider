name := "ImgurSpider"
version := "1.0"
scalaVersion := "2.12.1"

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "org.jsoup" % "jsoup" % "1.8.3",
    "io.github.hugoriggs" % "fileprocs_2.12" % "1.0.1" from "file://fileprocs_2.11.jar"
  )
)

lazy val guiSpider = project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    name := "GuiImgurSpider",
    version := "0.0.1",
    mainClass in assembly := Some("guiWrapper.SwingWrapper")
  )

lazy val spider = project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    name := "ImgurSpider",
    version := "0.0.1",
    mainClass in assembly := Some("ImgurSpider")
  )
