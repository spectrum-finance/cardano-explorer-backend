package io.ergolabs.cardano.explorer.api.v1.routes

import cats.effect.{Concurrent, ContextShift, Timer}
import cats.syntax.semigroupk._
import io.ergolabs.cardano.explorer.api.configs.RequestConfig
import io.ergolabs.cardano.explorer.api.streaming
import io.ergolabs.cardano.explorer.api.v1.endpoints.TransactionsEndpoints
import io.ergolabs.cardano.explorer.api.v1.services.Transactions
import io.ergolabs.cardano.explorer.api.v1.syntax._
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}

final class TransactionsRoutes[F[_]: Concurrent: ContextShift: Timer](requestConfig: RequestConfig)(implicit
  service: Transactions[F],
  opts: Http4sServerOptions[F, F]
) {

  private val endpoints = new TransactionsEndpoints[F](requestConfig)

  private val interpreter = Http4sServerInterpreter(opts)

  def routes: HttpRoutes[F] = streamAllR <+> getAllR <+> getByBlockR <+> getByAddressR <+> getByPCredR <+> getByTxHashR

  def getByTxHashR: HttpRoutes[F] =
    interpreter.toRoutes(endpoints.getByTxHash)(q => service.getByTxHash(q).orNotFound(s"Transaction{txHash=$q}"))

  def getAllR: HttpRoutes[F] =
    interpreter.toRoutes(endpoints.getAll) { case (paging, order) =>
      service.getAll(paging, order).eject
    }

  def streamAllR: HttpRoutes[F] =
    interpreter.toRoutes(endpoints.streamAll) { case (paging, order) =>
      streaming.bytesStream(service.streamAll(paging, order))
    }

  def getByBlockR: HttpRoutes[F] =
    interpreter.toRoutes(endpoints.getByBlock)(service.getByBlock(_).eject)

  def getByAddressR: HttpRoutes[F] =
    interpreter.toRoutes(endpoints.getByAddress) { case (addr, p) => service.getByAddress(addr, p).eject }

  def getByPCredR: HttpRoutes[F] =
    interpreter.toRoutes(endpoints.getByPCred) { case (addr, paging, order) =>
      service.getByPCred(addr, paging, order).eject
    }
}

object TransactionsRoutes {

  def make[F[_]: Concurrent: ContextShift: Timer](requestConfig: RequestConfig)(implicit
    service: Transactions[F],
    opts: Http4sServerOptions[F, F]
  ): HttpRoutes[F] =
    new TransactionsRoutes(requestConfig).routes
}
