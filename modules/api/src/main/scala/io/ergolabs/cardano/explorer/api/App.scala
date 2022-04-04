package io.ergolabs.cardano.explorer.api

import cats.arrow.FunctionK
import cats.data.ReaderT
import cats.effect.{Blocker, Resource}
import cats.syntax.option._
import cats.~>
import io.ergolabs.cardano.explorer.api
import io.ergolabs.cardano.explorer.api.App.{InitF, RunF}
import io.ergolabs.cardano.explorer.api.configs.ConfigBundle
import sttp.capabilities.WebSockets
import io.ergolabs.cardano.explorer.api.v1.services.{Assets, Blocks, NetworkParamsService, Outputs, Transactions}
import io.ergolabs.cardano.explorer.core.db.repositories.RepoBundle
import io.ergolabs.cardano.explorer.core.gateway.WebSocketGateway
import io.ergolabs.cardano.explorer.core.ogmios.service.OgmiosService
import org.http4s.server.Server
import sttp.client3.SttpBackend
import sttp.tapir.server.http4s.Http4sServerOptions
import tofu.doobie.log.EmbeddableLogHandler
import tofu.doobie.transactor.Txr
import tofu.lift.{IsoK, Unlift}
import zio._
import zio.interop.monix._
import tofu.logging.Logs
import tofu.logging.derivation.loggable.generate
import zio.interop.catz._
import monix.eval
import zio.{ExitCode, RIO, URIO, ZIO}

object App extends EnvApp[AppContext] {

  implicit val serverOptions: Http4sServerOptions[RunF, RunF] = Http4sServerOptions.default[RunF, RunF]
  //todo: only for debug
  implicit def fromTask2I: eval.Task ~> InitF = new FunctionK[eval.Task, InitF]{
    override def apply[A](fa: eval.Task[A]): InitF[A] = ZIO.fromMonixTask(fa)
  }
  implicit def fromTask2F: eval.Task ~> RunF = new FunctionK[eval.Task, RunF]{
    override def apply[A](fa: eval.Task[A]): RunF[A] = new ReaderT[InitF, AppContext, A](ctx => ZIO.fromMonixTask(fa))
  }
  def fromF2Task(unlift: Unlift[RunF, InitF]): RunF ~> eval.Task = new FunctionK[RunF, eval.Task]{
    override def apply[A](fa: RunF[A]): eval.Task[A] =  Runtime.default.unsafeRun(unlift.lift(fa).toMonixTask)
  }

  def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    init(args.headOption).use(_ => ZIO.never).orDie

  def init(configPathOpt: Option[String]): Resource[InitF, Server] =
    for {
      blocker <- Blocker[InitF]
      configs <- Resource.eval(ConfigBundle.load[InitF](configPathOpt, blocker))
      ctx                                = AppContext.init(configs)
      implicit0(ul: Unlift[RunF, InitF]) = Unlift.byIso(IsoK.byFunK(wr.runContextK(ctx))(wr.liftF))
      implicit0(rF: FunctionK[RunF, eval.Task])   = fromF2Task(ul)
      trans <- PostgresTransactor.make[InitF]("explorer-db-pool", configs.pg)
      implicit0(xa: Txr.Continuational[RunF]) = Txr.continuational[RunF](trans.mapK(wr.liftF))
      implicit0(elh: EmbeddableLogHandler[xa.DB]) <-
        Resource.eval(doobieLogging.makeEmbeddableHandler[InitF, RunF, xa.DB]("explorer-db-logging"))
      implicit0(logsDb: Logs[InitF, xa.DB]) = Logs.sync[InitF, xa.DB]
      implicit0(txReps: RepoBundle[xa.DB]) <- Resource.eval(RepoBundle.make[InitF, xa.DB])
      implicit0(sttpBackF: SttpBackend[RunF, WebSockets]) <- makeSttpBackend[InitF, RunF](ctx, none)
      implicit0(gateway: WebSocketGateway[RunF]) <- Resource.eval(WebSocketGateway.make[InitF, RunF](configs.webSocketCfg))
      implicit0(ogmios: OgmiosService[RunF]) <- Resource.eval(OgmiosService.make[InitF, RunF])
      implicit0(m: NetworkParamsService[RunF]) = NetworkParamsService.make[RunF, xa.DB]
      implicit0(txs: Transactions[RunF])       = Transactions.make[RunF, xa.DB]
      implicit0(outs: Outputs[RunF])           = Outputs.make[RunF, xa.DB]
      implicit0(blocks: Blocks[RunF])          = Blocks.make[RunF, xa.DB]
      implicit0(assets: Assets[RunF])          = Assets.make[RunF, xa.DB]
      server <- HttpServer.make[InitF, RunF](configs, runtime.platform.executor.asEC)
    } yield server
}
