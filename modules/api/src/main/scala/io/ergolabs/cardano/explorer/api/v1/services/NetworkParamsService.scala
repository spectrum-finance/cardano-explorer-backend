package io.ergolabs.cardano.explorer.api.v1.services

import cats.Monad
import cats.syntax.option._
import cats.syntax.either._
import io.ergolabs.cardano.explorer.core.db.repositories.RepoBundle
import mouse.anyf._
import tofu.doobie.LiftConnectionIO
import tofu.doobie.transactor.Txr
import tofu.syntax.monadic._
import io.ergolabs.cardano.explorer.api.v1.models.{EnvParams, NetworkName, ProtocolParams, SystemStart}
import io.ergolabs.cardano.explorer.core.types.PoolId
import tofu.Throws
import tofu.syntax.raise._
import io.circe.parser
import io.ergolabs.cardano.explorer.core.ogmios.service.OgmiosService

trait NetworkParamsService[F[_]] {

  def getNetworkParams: F[EnvParams]
}

object NetworkParamsService {

  def make[F[_]: Monad, D[_]: Monad: LiftConnectionIO: Throws](implicit
    txr: Txr[F, D],
    repos: RepoBundle[D],
    ogmios: OgmiosService[F]
  ): NetworkParamsService[F] = new Live[F, D](txr, repos, ogmios)

  final class Live[F[_]: Monad, D[_]: Monad: Throws](
    txr: Txr[F, D],
    repos: RepoBundle[D],
    ogmios: OgmiosService[F]
  ) extends NetworkParamsService[F] {

    def getNetworkParams: F[EnvParams] =
      ((for {
        meta        <- repos.network.getMeta
        epochParams <- repos.network.getLastEpochParams
        costModel   <- repos.network.getCostModel(epochParams.costModelId)
        parsedCm    <- parser.parse(costModel).toRaise[D]
        transformed <- parsedCm.as[Map[String, Map[String, Int]]].toRaise[D]
        cmCorrectFormat = transformed.map { case (k, v) => "PlutusScriptV1" -> v }
      } yield (meta, epochParams, cmCorrectFormat)) ||> txr.trans).flatMap {
        case (meta, epochParams, cmCorrectFormat) =>
          ogmios.getEraSummaries.map(eraSums =>
            EnvParams(
              ProtocolParams.fromEpochParams(epochParams, cmCorrectFormat),
              NetworkName(meta.networkName),
              SystemStart.fromExplorer(meta.startTime),
              epochParams.collateralPercent,
              eraSums.infoList
            )
          )
      }
  }
}
