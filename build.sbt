name := "acolyte-shapeless"

organization := "com.github.peterneyens"

scalaVersion := "2.11.7"

version := "0.1"


libraryDependencies in ThisBuild ++= List(
  "com.chuusai"    %% "shapeless"           % "2.2.5",
  "org.eu.acolyte" %% "jdbc-scala"          % "1.0.34",
  "org.specs2"     %% "specs2-core"         % "3.6.4"  % "test"
)

scalacOptions in ThisBuild ++= List(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard"
)

scalacOptions in Test ++= Seq("-Yrangepos")
