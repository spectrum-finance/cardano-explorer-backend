package io.ergolabs.cardano.explorer.api.v1.routes

import cats.effect.{Concurrent, ContextShift, Timer}
import io.ergolabs.cardano.explorer.api.v1.endpoints.{AssetsEndpoints, BlocksEndpoints}
import io.ergolabs.cardano.explorer.api.v1.services.{Assets, Blocks}
import org.http4s.HttpRoutes
import io.ergolabs.cardano.explorer.api.v1.syntax._
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}

final class AssetsRoutes[F[_]: Concurrent: ContextShift: Timer](implicit
  service: Assets[F],
  opts: Http4sServerOptions[F, F]
) {

  private val endpoints = new AssetsEndpoints
  import endpoints._

  private val interpreter = Http4sServerInterpreter(opts)

  def routes = getAssetInfoInfoR

  def getAssetInfoInfoR = interpreter.toRoutes(getAssetInfoInfo) { assetId =>
    service.getInfo(assetId).orNotFound(s"AssetInfo{id=$assetId}")
  }
}

object AssetsRoutes {

  def make[F[_]: Concurrent: ContextShift: Timer](implicit
    service: Assets[F],
    opts: Http4sServerOptions[F, F]
  ): HttpRoutes[F] =
    new AssetsRoutes[F]().routes
}
