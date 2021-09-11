import Dependencies._
import sbt.Keys.organization

lazy val commonSettings = Seq(
  scalaVersion := "2.13.6",
  organization := "io.ergolabs",
  version := "0.0.1",
  scalacOptions ++= commonScalacOption,
  libraryDependencies ++= List(CompilerPlugins.betterMonadicFor, CompilerPlugins.kindProjector)
)

lazy val commonScalacOption = List(
  "-Ymacro-annotations",
  "-Yrangepos",
  "-Wconf:cat=unused:info"
)

lazy val explorerBackend = project
  .in(file("."))
  .withId("cardano-explorer-backend")
  .settings(commonSettings)
  .settings(name := "cardano-explorer-backend")
  .aggregate(core, api)

lazy val core =
  (project in file("modules/core"))
    .settings(commonSettings)
    .settings(
      name := "explorer-core",
      libraryDependencies ++= List(
        Libraries.tofuCore,
        Libraries.tofuDerivation,
        Libraries.tofuDoobie,
        Libraries.tofuLogging,
        Libraries.doobieCore,
        Libraries.newtype,
        Libraries.mouse
      )
    )

lazy val api =
  (project in file("modules/api"))
    .settings(commonSettings)
    .settings(
      name := "explorer-api",
      libraryDependencies ++= List(
        Libraries.tofuConcurrent,
        Libraries.tofuOptics,
        Libraries.tofuStreams,
        Libraries.doobiePg,
        Libraries.doobieHikari,
        Libraries.tapirCirce,
        Libraries.tapirHttp4s
      )
    )
    .dependsOn(core)
