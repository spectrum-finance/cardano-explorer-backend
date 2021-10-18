package io.ergolabs.cardano.explorer.api.v1.routes

import cats.effect.{Concurrent, ContextShift, Timer}
import cats.syntax.semigroupk._
import io.ergolabs.cardano.explorer.api.v1.endpoints.OutputsEndpoints
import io.ergolabs.cardano.explorer.api.v1.services.Outputs
import io.ergolabs.cardano.explorer.api.v1.syntax._
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}

final class OutputsRoutes[F[_]: Concurrent: ContextShift: Timer](implicit
  service: Outputs[F],
  opts: Http4sServerOptions[F, F]
) {

  private val endpoints = OutputsEndpoints
  import endpoints._

  private val interpreter = Http4sServerInterpreter(opts)

  def routes: HttpRoutes[F] =
    getUnspentByAddrR <+>
    getUnspentByAssetR <+>
    searchUnspentR <+>
    getByOutRefR <+>
    getUnspentIndexedR <+>
    getUnspentR

  def getByOutRefR: HttpRoutes[F] =
    interpreter.toRoutes(getByOutRef)(ref => service.getByOutRef(ref).orNotFound(s"Output{ref=$ref}"))

  def getUnspentR: HttpRoutes[F] =
    interpreter.toRoutes(getUnspent) { paging =>
      service.getUnspent(paging).eject
    }

  def getUnspentIndexedR: HttpRoutes[F] =
    interpreter.toRoutes(getUnspentIndexed) { indexing =>
      service.getUnspent(indexing).eject
    }

  def getUnspentByAddrR: HttpRoutes[F] =
    interpreter.toRoutes(getUnspentByAddr) { case (addr, paging) =>
      service.getUnspentByAddr(addr, paging).eject
    }

  def getUnspentByAssetR: HttpRoutes[F] =
    interpreter.toRoutes(getUnspentByAsset) { case (asset, paging) =>
      service.getUnspentByAsset(asset, paging).eject
    }

  def searchUnspentR: HttpRoutes[F] =
    interpreter.toRoutes(searchUnspent) { case (paging, query) =>
      service.searchUnspent(query, paging).eject
    }
}

object OutputsRoutes {

  def make[F[_]: Concurrent: ContextShift: Timer](implicit
    service: Outputs[F],
    opts: Http4sServerOptions[F, F]
  ): HttpRoutes[F] =
    new OutputsRoutes().routes
}
