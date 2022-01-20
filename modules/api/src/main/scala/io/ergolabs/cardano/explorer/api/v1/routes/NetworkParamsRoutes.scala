package io.ergolabs.cardano.explorer.api.v1.routes

import cats.effect.{Concurrent, ContextShift, Timer}
import io.ergolabs.cardano.explorer.api.v1.endpoints.{AssetsEndpoints, BlocksEndpoints}
import io.ergolabs.cardano.explorer.api.v1.services.{Assets, Blocks}
import org.http4s.HttpRoutes
import io.ergolabs.cardano.explorer.api.v1.syntax._
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}
import io.ergolabs.cardano.explorer.api.v1.services.NetworkParamsService
import io.ergolabs.cardano.explorer.api.v1.endpoints.NetworkParamsEndpoints
import cats.syntax.either._
import io.ergolabs.cardano.explorer.api.v1.HttpError
import cats.Functor
import tofu.syntax.monadic._

final class NetworkParamsRoutes[F[_]: Concurrent: ContextShift: Timer: Functor](implicit
  service: NetworkParamsService[F],
  opts: Http4sServerOptions[F, F]
) {

  private val endpoints = NetworkParamsEndpoints
  import endpoints._

  private val interpreter = Http4sServerInterpreter(opts)

  def routes = getNetworkParamsR

  def getNetworkParamsR = interpreter.toRoutes(networkParamsDef) { _ =>
    service.getNetworkParams.map(r => r.asRight[HttpError])
  }
}

object NetworkParamsRoutes {

  def make[F[_]: Concurrent: ContextShift: Timer](implicit
    service: NetworkParamsService[F],
    opts: Http4sServerOptions[F, F]
  ): HttpRoutes[F] =
    new NetworkParamsRoutes[F]().routes
}
