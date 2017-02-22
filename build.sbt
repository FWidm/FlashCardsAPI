name := """FlashCardsAPINew"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  filters,
  //javaJpa,
  "mysql" % "mysql-connector-java" % "5.1.34"
)
