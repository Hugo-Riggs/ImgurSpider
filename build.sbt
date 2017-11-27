name := "ImgurSpider"
version := "1.0"
scalaVersion := "2.12.1"

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "org.jsoup" % "jsoup" % "1.8.3",
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6",
    "org.json4s" % "json4s-native_2.12" % "3.5.2"
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
