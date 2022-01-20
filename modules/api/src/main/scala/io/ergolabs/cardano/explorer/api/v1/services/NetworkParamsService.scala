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

trait NetworkParamsService[F[_]] {

  def getNetworkParams: F[EnvParams]
}

object NetworkParamsService {

  def make[F[_], D[_]: Monad: LiftConnectionIO: Throws](implicit
    txr: Txr[F, D],
    repos: RepoBundle[D]
  ): NetworkParamsService[F] = new Live[F, D](txr, repos)

  final class Live[F[_], D[_]: Monad: Throws](txr: Txr[F, D], repos: RepoBundle[D]) extends NetworkParamsService[F] {

    def getNetworkParams: F[EnvParams] =
      (for {
        meta            <- repos.network.getMeta
        epochParams     <- repos.network.getLastEpochParams
        costModel       <- repos.network.getCostModel(epochParams.costModelId)
        parsedCm        <- parser.parse(costModel).toRaise
        transformed     <- parsedCm.as[Map[String, Map[String, Int]]].toRaise
        cmCorrectFormat = transformed.map { case (k, v) => "PlutusScriptV1" -> v }         
      } yield 
          EnvParams(
            ProtocolParams.fromEpochParams(epochParams, cmCorrectFormat),
            NetworkName(meta.networkName),
            SystemStart.fromExplorer(meta.startTime),
            epochParams.collateralPercent
          )
        ) ||> txr.trans

  }
}
