package io.ergolabs.cardano.explorer.api

import cats.effect.{Concurrent, ConcurrentEffect, ContextShift, Resource, Timer}
import cats.syntax.semigroupk._
import io.ergolabs.cardano.explorer.api.configs.{ConfigBundle, HttpConfig}
import io.ergolabs.cardano.explorer.api.routes.unliftRoutes
import io.ergolabs.cardano.explorer.api.types.TraceId
import io.ergolabs.cardano.explorer.api.v1.routes.{AssetsRoutes, BlocksRoutes, DocsRoutes, OutputsRoutes, TransactionsRoutes, NetworkParamsRoutes}
import io.ergolabs.cardano.explorer.api.v1.services.{Assets, Blocks, Outputs, Transactions, NetworkParamsService}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.{Router, Server}
import org.http4s.syntax.kleisli._
import sttp.tapir.server.http4s.Http4sServerOptions
import tofu.lift.Unlift

import scala.concurrent.ExecutionContext

object HttpServer {

  def make[
    I[_]: ConcurrentEffect: ContextShift: Timer,
    F[_]: Concurrent: ContextShift: Timer: Unlift[*[_], I]: TraceId.Local
  ](conf: ConfigBundle, ec: ExecutionContext)(implicit
    txs: Transactions[F],
    outs: Outputs[F],
    blocks: Blocks[F],
    assets: Assets[F],
    networkParams: NetworkParamsService[F],
    opts: Http4sServerOptions[F, F]
  ): Resource[I, Server] = {
    val txsR     = TransactionsRoutes.make[F](conf.requests)
    val outsR    = OutputsRoutes.make[F](conf.requests)
    val blocksR  = BlocksRoutes.make[F]
    val assetsR  = AssetsRoutes.make[F]
    val networkR = NetworkParamsRoutes.make[F]
    val docsR    = DocsRoutes.make[F](conf.requests)
    val api      = Router("/" -> unliftRoutes[F, I](txsR <+> outsR <+> blocksR <+> assetsR <+> networkR <+> docsR)).orNotFound
    BlazeServerBuilder[I](ec).bindHttp(conf.http.port, conf.http.host).withHttpApp(api).resource
  }
}
