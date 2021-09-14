import sbt._

object Dependencies {

  object V {
    val tofu       = "0.10.3"
    val derevo     = "0.12.5"
    val catsEffect = "2.5.3"
    val doobie     = "0.13.4"
    val pureconfig = "0.14.1"
    val tapir      = "0.18.3"
    val newtype    = "0.4.3"
    val mouse      = "0.26.2"
    val enumeratum = "1.7.0"

    val betterMonadicFor = "0.3.1"
    val kindProjector    = "0.13.2"
  }

  object Libraries {
    val tofuCore       = "tf.tofu" %% "tofu-core"         % V.tofu
    val tofuConcurrent = "tf.tofu" %% "tofu-concurrent"   % V.tofu
    val tofuOptics     = "tf.tofu" %% "tofu-optics-macro" % V.tofu
    val tofuDerivation = "tf.tofu" %% "tofu-derivation"   % V.tofu
    val tofuLogging    = "tf.tofu" %% "tofu-logging"      % V.tofu
    val tofuDoobie     = "tf.tofu" %% "tofu-doobie"       % V.tofu
    val tofuStreams    = "tf.tofu" %% "tofu-streams"      % V.tofu
    val tofuFs2        = "tf.tofu" %% "tofu-fs2-interop"  % V.tofu
    val tofuZio        = "tf.tofu" %% "tofu-zio-interop"  % V.tofu

    val derevoCats        = "tf.tofu" %% "derevo-cats"              % V.derevo
    val derevoCatsTagless = "tf.tofu" %% "derevo-cats-tagless"      % V.derevo
    val derevoCirce       = "tf.tofu" %% "derevo-circe-magnolia"    % V.derevo
    val derevoPureconfig  = "tf.tofu" %% "derevo-pureconfig-legacy" % V.derevo

    val catsEffect = "org.typelevel" %% "cats-effect" % V.catsEffect

    val doobieCore   = "org.tpolecat" %% "doobie-core"     % V.doobie
    val doobiePg     = "org.tpolecat" %% "doobie-postgres" % V.doobie
    val doobieHikari = "org.tpolecat" %% "doobie-hikari"   % V.doobie

    val mouse = "org.typelevel" %% "mouse" % V.mouse

    val pureconfig = "com.github.pureconfig" %% "pureconfig-cats-effect" % V.pureconfig

    val tapirCore   = "com.softwaremill.sttp.tapir" %% "tapir-core"          % V.tapir
    val tapirCirce  = "com.softwaremill.sttp.tapir" %% "tapir-json-circe"    % V.tapir
    val tapirHttp4s = "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % V.tapir

    val newtype = "io.estatico" %% "newtype" % V.newtype

    val enumeratum = "com.beachape" %% "enumeratum" % V.enumeratum
  }

  object CompilerPlugins {

    val betterMonadicFor = compilerPlugin(
      "com.olegpy" %% "better-monadic-for" % V.betterMonadicFor
    )

    val kindProjector = compilerPlugin(
      "org.typelevel" % "kind-projector" % V.kindProjector cross CrossVersion.full
    )
  }
}
