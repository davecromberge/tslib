import com.typesafe.sbt.SbtGit.git
import sbt._
import sbt.Keys._

object BuildSettings {

  val compilerFlags = Seq(
    "-deprecation",
    "-unchecked",
    "-Xexperimental",
    "-Xlint:_,-infer-any",
    "-feature",
    "-target:jvm-1.8"
  )

  lazy val buildSettings = Seq(
    git.useGitDescribe := true,
    organization := "com.outlyer",
    scalaVersion := Dependencies.Versions.scalaVersion,
    scalacOptions ++= BuildSettings.compilerFlags,
    crossPaths := true,
    sourcesInBase := false,
    exportJars := true, // Needed for one-jar, with multi-project
    // https://github.com/sbt/sbt/issues/1636
    evictionWarningOptions in update := EvictionWarningOptions.empty,
    packageOptions in (Compile, packageBin) += Package.ManifestAttributes(
      "Build-Date"   -> java.time.Instant.now().toString
    )
  )

  def profile: Project => Project = p =>
    p.settings(buildSettings: _*)
}