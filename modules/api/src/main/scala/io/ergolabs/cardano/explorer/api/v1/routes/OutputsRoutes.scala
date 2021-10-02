package io.ergolabs.cardano.explorer.api.v1.routes

import cats.effect.{Concurrent, ContextShift, Timer}
import io.ergolabs.cardano.explorer.api.v1.endpoints.OutputsEndpoints
import io.ergolabs.cardano.explorer.api.v1.services.Outputs
import io.ergolabs.cardano.explorer.api.v1.syntax._
import org.http4s.HttpRoutes
import cats.syntax.semigroupk._
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}

final class OutputsRoutes[F[_]: Concurrent: ContextShift: Timer](implicit
  service: Outputs[F],
  opts: Http4sServerOptions[F, F]
) {

  private val endpoints = new OutputsEndpoints
  import endpoints._

  private val interpreter = Http4sServerInterpreter(opts)

  def routes: HttpRoutes[F] = getUnspentByAddrR <+> getUnspentByAssetR <+> getSearchUnspentR <+> getByOutRefR

  def getByOutRefR: HttpRoutes[F] =
    interpreter.toRoutes(getByOutRef)(ref => service.getByOutRef(ref).orNotFound(s"Output{ref=$ref}"))

  def getUnspentByAddrR: HttpRoutes[F] =
    interpreter.toRoutes(getUnspentByAddr) { case (addr, paging) =>
      service.getUnspentByAddr(addr, paging).eject
    }

  def getUnspentByAssetR: HttpRoutes[F] =
    interpreter.toRoutes(getUnspentByAsset) { case (asset, paging) =>
      service.getUnspentByAsset(asset, paging).eject
    }

  def getSearchUnspentR: HttpRoutes[F] =
    interpreter.toRoutes(getSearchUnspent) { case (paging, query) =>
      service.getSearchUnspent(query, paging).eject
    }
}

object OutputsRoutes {

  def make[F[_]: Concurrent: ContextShift: Timer](implicit
    service: Outputs[F],
    opts: Http4sServerOptions[F, F]
  ): HttpRoutes[F] =
    new OutputsRoutes().routes
}
