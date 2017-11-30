import sbt._

object Dependencies {
  object Versions {
    val scalaVersion      = "2.12.3"
    val scalaCheckVersion = "1.13.5"
    val scalaTestVersion  = "3.0.4"
    val scodec            = "1.10.3"
    val crossScala = Seq(scalaVersion)
  }

  import Versions._

  lazy val scalaTest          = "org.scalatest" %% "scalatest" % scalaTestVersion
  lazy val scalaCheck         = "org.scalacheck" %% "scalacheck" % scalaCheckVersion
  lazy val scodecCore         = "org.scodec" %% "scodec-core" % scodec
}
