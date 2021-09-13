package io.ergolabs.cardano.explorer.api

import cats.effect.{Blocker, Resource}
import io.ergolabs.cardano.explorer.api.configs.ConfigBundle
import io.ergolabs.cardano.explorer.api.v1.services.Transactions
import io.ergolabs.cardano.explorer.core.db.repositories.TxRepoBundle
import org.http4s.server.Server
import sttp.tapir.server.http4s.Http4sServerOptions
import tofu.doobie.log.EmbeddableLogHandler
import tofu.doobie.transactor.Txr
import tofu.lift.{IsoK, Unlift}
import tofu.logging.Logs
import zio.interop.catz._
import zio.{ExitCode, URIO, ZIO}

object App extends EnvApp[AppContext] {

  implicit val serverOptions: Http4sServerOptions[RunF, RunF] = Http4sServerOptions.default[RunF, RunF]

  def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    init(args.headOption).use(_ => ZIO.never).orDie

  def init(configPathOpt: Option[String]): Resource[InitF, Resource[InitF, Server]] =
    for {
      blocker <- Blocker[InitF]
      configs <- Resource.eval(ConfigBundle.load[InitF](configPathOpt, blocker))
      ctx                                = AppContext.init(configs)
      implicit0(ul: Unlift[RunF, InitF]) = Unlift.byIso(IsoK.byFunK(wr.runContextK(ctx))(wr.liftF))
      trans <- PostgresTransactor.make[InitF]("explorer-db-pool", configs.pg)
      implicit0(xa: Txr.Continuational[RunF]) = Txr.continuational[RunF](trans.mapK(wr.liftF))
      implicit0(elh: EmbeddableLogHandler[xa.DB]) <-
        Resource.eval(doobieLogging.makeEmbeddableHandler[InitF, RunF, xa.DB]("explorer-db-logging"))
      implicit0(logsDb: Logs[InitF, xa.DB]) = Logs.sync[InitF, xa.DB]
      implicit0(txReps: TxRepoBundle[xa.DB]) <- Resource.eval(TxRepoBundle.make[InitF, xa.DB])
      implicit0(txs: Transactions[RunF]) = Transactions.make[RunF, xa.DB]
      server                             = HttpServer.make[InitF, RunF](configs.http, runtime.platform.executor.asEC)
    } yield server
}
