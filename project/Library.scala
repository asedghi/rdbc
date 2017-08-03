import sbt._

object Library {
  private val scalatestVersion = "3.0.3"

  val reactiveStreams = "org.reactivestreams" % "reactive-streams" % "1.0.0"
  val sourcecode = "com.lihaoyi" %% "sourcecode" % "0.1.4"
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
  val scalactic = "org.scalactic" %% "scalactic" % scalatestVersion
  val scalamock = "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0"
  val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion
  val reactiveStreamsTck = "org.reactivestreams" % "reactive-streams-tck" % "1.0.0"
  val akkaStream = "com.typesafe.akka" %% "akka-stream" % "2.5.3"
}
