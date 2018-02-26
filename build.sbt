import sbt.Keys.libraryDependencies

name := "camt-reporter"
version := "0.1"
scalaVersion := "2.12.4"
assemblyJarName in assembly := "camt-reporter.jar"

lazy val root = (project in file(".")).
  enablePlugins(ScalaxbPlugin).
  settings(
    name := "camt-reporter",
    scalaxbPackageName in (Compile, scalaxb) := "generated",
    scalaxbDispatchVersion in (Compile, scalaxb) := "0.11.3"
  )

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0",
  "org.slf4j" % "slf4j-api" % "1.7.1",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.1",
  "ch.qos.logback" % "logback-classic" % "1.0.3",
  "com.github.scopt" %% "scopt" % "3.7.0",
  "joda-time" % "joda-time" % "2.9.9",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

coverageExcludedPackages := "generated.*;scalaxb.*"

enablePlugins(JavaAppPackaging)
//enablePlugins(DockerPlugin)
