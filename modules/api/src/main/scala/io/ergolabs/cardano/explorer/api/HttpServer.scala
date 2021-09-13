package io.ergolabs.cardano.explorer.api

import cats.effect.{Concurrent, ConcurrentEffect, ContextShift, Resource, Timer}
import io.ergolabs.cardano.explorer.api.configs.HttpConfig
import io.ergolabs.cardano.explorer.api.routes.unliftRoutes
import io.ergolabs.cardano.explorer.api.types.TraceId
import io.ergolabs.cardano.explorer.api.v1.routes.TransactionsRoutes
import io.ergolabs.cardano.explorer.api.v1.services.Transactions
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
    opts: Http4sServerOptions[F, F]
  ): Resource[I, Server] = {
    val routes = TransactionsRoutes.make[F]
    val api    = Router("/" -> unliftRoutes[F, I](routes)).orNotFound
    BlazeServerBuilder[I](ec).bindHttp(conf.port, conf.host).withHttpApp(api).resource
  }
}
