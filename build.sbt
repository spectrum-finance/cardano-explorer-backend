import Dependencies._
import sbt.Keys.organization

lazy val commonSettings = Seq(
  scalaVersion := "2.13.6",
  organization := "io.ergolabs",
  version := "0.7.1",
  scalacOptions ++= commonScalacOption,
  libraryDependencies ++= List(CompilerPlugins.betterMonadicFor, CompilerPlugins.kindProjector),
  assembly / test := {},
  assembly / assemblyMergeStrategy := {
    case "logback.xml"                                             => MergeStrategy.first
    case "module-info.class"                                       => MergeStrategy.discard
    case other if other.contains("scala/annotation/nowarn.class")  => MergeStrategy.first
    case other if other.contains("scala/annotation/nowarn$.class") => MergeStrategy.first
    case other if other.contains("io.netty.versions")              => MergeStrategy.first
    case other                                                     => (assemblyMergeStrategy in assembly).value(other)
  }
)

lazy val commonScalacOption = List(
  "-Ymacro-annotations",
  "-Yrangepos",
  "-Wconf:cat=unused:info",
  "-language:implicitConversions",
  "-feature"
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
        Libraries.tofuFs2,
        Libraries.derevoCatsTagless,
        Libraries.doobiePg,
        Libraries.newtype,
        Libraries.mouse,
        Libraries.tapirCore,
        Libraries.tapirCirce,
        Libraries.derevoCirce,
        Libraries.enumeratum,
        Libraries.enumeratumCirce
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
        Libraries.tofuZio,
        Libraries.doobieHikari,
        Libraries.http4sServer,
        Libraries.tapirCirce,
        Libraries.tapirHttp4s,
        Libraries.tapirRedoc,
        Libraries.tapirDocs,
        Libraries.tapirOpenApi,
        Libraries.derevoPureconfig,
        Libraries.pureconfig
      )
    )
    .dependsOn(core)
    .enablePlugins(JavaAppPackaging, UniversalPlugin, DockerPlugin)
