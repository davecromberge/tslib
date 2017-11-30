lazy val tslib = (project in file("."))
  .configure(BuildSettings.profile)
  .settings(libraryDependencies ++= Seq(
    Dependencies.scodecCore,
    Dependencies.scalaTest % Test,
    Dependencies.scalaCheck % Test
  ))
