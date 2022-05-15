package io.ergolabs.cardano.explorer.api.v1.routes

import cats.effect.{Concurrent, ContextShift, Timer}
import cats.syntax.semigroupk._
import io.ergolabs.cardano.explorer.api.configs.RequestConfig
import io.ergolabs.cardano.explorer.api.v1.endpoints.OutputsEndpoints
import io.ergolabs.cardano.explorer.api.v1.services.Outputs
import io.ergolabs.cardano.explorer.api.v1.syntax._
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}

final class OutputsRoutes[F[_]: Concurrent: ContextShift: Timer](requestConfig: RequestConfig)(implicit
  service: Outputs[F],
  opts: Http4sServerOptions[F, F]
) {

  private val endpoints = new OutputsEndpoints(requestConfig)

  private val interpreter = Http4sServerInterpreter(opts)

  def routes: HttpRoutes[F] =
    getAllR <+>
    getUnspentIndexedR <+>
    searchUnspentR <+>
    getUnspentR <+>
    getUnspentByAddrR <+>
    getUnspentByPCredR <+>
    getUnspentByAssetR <+>
    getByOutRefR

  def getByOutRefR: HttpRoutes[F] =
    interpreter.toRoutes(endpoints.getByOutRef)(ref => service.getByOutRef(ref).orNotFound(s"Output{ref=$ref}"))

  def getAllR: HttpRoutes[F] =
    interpreter.toRoutes(endpoints.getAll) { case (paging, ordering) =>
      service.getAll(paging, ordering).eject
    }

  def getUnspentR: HttpRoutes[F] =
    interpreter.toRoutes(endpoints.getUnspent) { case (paging, ordering) =>
      service.getUnspent(paging, ordering).eject
    }

  def getUnspentIndexedR: HttpRoutes[F] =
    interpreter.toRoutes(endpoints.getUnspentIndexed) { case (indexing, ordering) =>
      service.getUnspent(indexing, ordering).eject
    }

  def getUnspentByAddrR: HttpRoutes[F] =
    interpreter.toRoutes(endpoints.getUnspentByAddr) { case (addr, paging) =>
      service.getUnspentByAddr(addr, paging).eject
    }

  def getUnspentByPCredR: HttpRoutes[F] =
    interpreter.toRoutes(endpoints.getUnspentByPCred) { case (pcred, paging, ordering) =>
      service.getUnspentByPCred(pcred, paging, ordering).eject
    }

  def getUnspentByAssetR: HttpRoutes[F] =
    interpreter.toRoutes(endpoints.getUnspentByAsset) { case (asset, paging, ordering) =>
      service.getUnspentByAsset(asset, paging, ordering).eject
    }

  def searchUnspentR: HttpRoutes[F] =
    interpreter.toRoutes(endpoints.searchUnspent) { case (paging, query) =>
      service.searchUnspent(query, paging).eject
    }
}

object OutputsRoutes {

  def make[F[_]: Concurrent: ContextShift: Timer](requestConfig: RequestConfig)(implicit
    service: Outputs[F],
    opts: Http4sServerOptions[F, F]
  ): HttpRoutes[F] =
    new OutputsRoutes(requestConfig).routes
}
