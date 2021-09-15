package io.ergolabs.cardano.explorer.api

import cats.effect.{Concurrent, ConcurrentEffect, ContextShift, Resource, Timer}
import cats.syntax.semigroupk._
import io.ergolabs.cardano.explorer.api.configs.HttpConfig
import io.ergolabs.cardano.explorer.api.routes.unliftRoutes
import io.ergolabs.cardano.explorer.api.types.TraceId
import io.ergolabs.cardano.explorer.api.v1.routes.{BlocksRoutes, OutputsRoutes, TransactionsRoutes}
import io.ergolabs.cardano.explorer.api.v1.services.{Blocks, Outputs, Transactions}
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
  ](conf: HttpConfig, ec: ExecutionContext)(implicit
    txs: Transactions[F],
    outs: Outputs[F],
    blocks: Blocks[F],
    opts: Http4sServerOptions[F, F]
  ): Resource[I, Server] = {
    val txsR    = TransactionsRoutes.make[F]
    val outsR   = OutputsRoutes.make[F]
    val blocksR = BlocksRoutes.make[F]
    val api     = Router("/" -> unliftRoutes[F, I](txsR <+> outsR <+> blocksR)).orNotFound
    BlazeServerBuilder[I](ec).bindHttp(conf.port, conf.host).withHttpApp(api).resource
  }
}
