import play.Project._

name := "BlogExtractor"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache
  )     

playJavaSettings
