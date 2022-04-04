package io.ergolabs.cardano.explorer.core.ogmios.service

import cats.{Apply, Functor, Monad}
import derevo.derive
import io.ergolabs.cardano.explorer.core.gateway.WebSocketGateway
import io.ergolabs.cardano.explorer.core.ogmios.models.OgmiosResponseBody._
import io.ergolabs.cardano.explorer.core.ogmios.models.{OgmiosRequest, OgmiosResponse}
import io.ergolabs.cardano.explorer.core.ogmios.models.OgmiosResponseBody.EraSummaries
import io.ergolabs.cardano.explorer.core.ogmios.models.Query.QueryName
import tofu.higherKind.Mid
import tofu.higherKind.derived.representableK
import tofu.logging.{Logging, Logs}
import tofu.syntax.monadic._
import tofu.syntax.logging._

@derive(representableK)
trait OgmiosService[F[_]] {

  def getEraSummaries: F[EraSummaries]
}

object OgmiosService {

  def make[I[_]: Functor, F[_]: Monad](implicit gateway: WebSocketGateway[F], logs: Logs[I, F]): I[OgmiosService[F]] =
    logs.forService[OgmiosService[F]].map { implicit logging =>
      new OgmiosServiceTracingMid[F]() attach new Live[F](gateway)
    }

  final private class Live[F[_]: Monad](gateway: WebSocketGateway[F]) extends OgmiosService[F] {

    def getEraSummaries: F[EraSummaries] =
      gateway
        .send[OgmiosRequest, OgmiosResponse[EraSummaries]](OgmiosRequest.makeRequest(QueryName.EraSummaries))
        .map(_.result)
  }

  final private class OgmiosServiceTracingMid[F[_]: Apply](implicit logging: Logging[F]) extends OgmiosService[Mid[F, *]] {

    def getEraSummaries: Mid[F, EraSummaries] = info"Going to get era summaries from Ogmios" *> _
  }
}
