package io.ergolabs.cardano.explorer.api

import cats.effect.{Blocker, Resource}
import io.ergolabs.cardano.explorer.api.configs.ConfigBundle
import io.ergolabs.cardano.explorer.api.graphite.MetricsMiddleware.MetricsMiddleware
import io.ergolabs.cardano.explorer.api.graphite.{GraphiteClient, Metrics, MetricsMiddleware}
import io.ergolabs.cardano.explorer.api.v1.services.{Assets, Blocks, NetworkParamsService, Outputs, Transactions}
import io.ergolabs.cardano.explorer.core.db.repositories.RepoBundle
import org.http4s.server.Server
import sttp.tapir.server.http4s.Http4sServerOptions
import tofu.doobie.log.EmbeddableLogHandler
import tofu.doobie.transactor.Txr
import tofu.lift.{IsoK, Unlift}
import tofu.logging.Logs
import tofu.logging.derivation.loggable.generate
import zio.interop.catz._
import zio.{ExitCode, URIO, ZIO}

object App extends EnvApp[AppContext] {

  implicit val serverOptions: Http4sServerOptions[RunF, RunF] = Http4sServerOptions.default[RunF, RunF]

  def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    init(args.headOption).use(_ => ZIO.never).orDie

  def init(configPathOpt: Option[String]): Resource[InitF, Server] =
    for {
      blocker <- Blocker[InitF]
      configs <- Resource.eval(ConfigBundle.load[InitF](configPathOpt, blocker))
      ctx                                = AppContext.init(configs)
      implicit0(ul: Unlift[RunF, InitF]) = Unlift.byIso(IsoK.byFunK(wr.runContextK(ctx))(wr.liftF))
      trans <- PostgresTransactor.make[InitF]("explorer-db-pool", configs.pg)
      implicit0(graphiteClient: GraphiteClient[RunF]) <-
        GraphiteClient.make[InitF, RunF](configs.graphite, configs.graphitePathPrefix)
      implicit0(metrics: Metrics[RunF]) <- Resource.eval(Metrics.create[InitF, RunF])
      implicit0(xa: Txr.Continuational[RunF]) = Txr.continuational[RunF](trans.mapK(wr.liftF))
      implicit0(elh: EmbeddableLogHandler[xa.DB]) <-
        Resource.eval(doobieLogging.makeEmbeddableHandler[InitF, RunF, xa.DB]("explorer-db-logging"))
      implicit0(logsDb: Logs[InitF, xa.DB]) = Logs.sync[InitF, xa.DB]
      implicit0(txReps: RepoBundle[xa.DB]) <- Resource.eval(RepoBundle.make[InitF, xa.DB])
      implicit0(m: NetworkParamsService[RunF]) = NetworkParamsService.make[RunF, xa.DB]
      implicit0(txs: Transactions[RunF])       = Transactions.make[RunF, xa.DB]
      implicit0(outs: Outputs[RunF])           = Outputs.make[RunF, xa.DB]
      implicit0(blocks: Blocks[RunF])          = Blocks.make[RunF, xa.DB]
      implicit0(assets: Assets[RunF])          = Assets.make[RunF, xa.DB]
      implicit0(metricsMiddleware: MetricsMiddleware[RunF]) = MetricsMiddleware.make[RunF]
      server <- HttpServer.make[InitF, RunF](configs, runtime.platform.executor.asEC)
    } yield server
}
