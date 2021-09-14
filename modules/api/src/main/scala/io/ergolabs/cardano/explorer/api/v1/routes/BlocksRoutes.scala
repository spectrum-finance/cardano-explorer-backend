package io.ergolabs.cardano.explorer.api.v1.routes

import cats.effect.{Concurrent, ContextShift, Timer}
import io.ergolabs.cardano.explorer.api.v1.endpoints.{BlocksEndpoints, OutputsEndpoints}
import io.ergolabs.cardano.explorer.api.v1.services.{Blocks, Outputs, Transactions}
import io.ergolabs.cardano.explorer.api.v1.syntax._
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}

final class BlocksRoutes[F[_]: Concurrent: ContextShift: Timer](implicit
  service: Blocks[F],
  opts: Http4sServerOptions[F, F]
) {

  private val endpoints = new BlocksEndpoints
  import endpoints._

  private val interpreter = Http4sServerInterpreter(opts)

  def routes: HttpRoutes[F] = getBestBlockInfoR

  def getBestBlockInfoR: HttpRoutes[F] = interpreter.toRoutes(getBestBlockInfo)(_ => service.getBestBlockInfo.eject)
}

object BlocksRoutes {

  def make[F[_]: Concurrent: ContextShift: Timer](implicit
    service: Blocks[F],
    opts: Http4sServerOptions[F, F]
  ): HttpRoutes[F] =
    new BlocksRoutes[F]().routes
}
