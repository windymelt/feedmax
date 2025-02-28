import _root_.caliban.tools.Codegen

val scala3Version = "3.6.3"

lazy val root = project
  .in(file("."))
  .enablePlugins(CalibanPlugin)
  .settings(
    Compile / caliban / calibanSettings ++= Seq(
      calibanSetting(file("./feedmax.graphql"))(
        _.genType(Codegen.GenType.Schema)
          .clientName("FeedmaxSchema.scala")
          .packageName("dev.capslock.feedmax.graphql"),
      ),
    ),
  )
  .settings(
    name                             := "feedmax",
    version                          := "0.1.0-SNAPSHOT",
    scalaVersion                     := scala3Version,
    fork                             := true,
    libraryDependencies += "dev.zio" %% "zio"                 % "2.1.15",
    libraryDependencies += "dev.zio" %% "zio-config"          % "4.0.3",
    libraryDependencies += "dev.zio" %% "zio-config-typesafe" % "4.0.3",
    libraryDependencies += "dev.zio" %% "zio-http"            % "3.0.1",
    libraryDependencies += "dev.zio" %% "zio-json"            % "0.7.21",
    libraryDependencies += "com.github.ghostdogpr" %% "caliban"       % "2.9.2",
    libraryDependencies += "com.github.ghostdogpr" %% "caliban-quick" % "2.9.2",
    libraryDependencies += "dev.capslock"          %% "rss4s"         % "0.0.1",
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test,
  )
