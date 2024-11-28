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

  val PlutusV1 = "PlutusV1"
  val PlutusV2 = "PlutusV2"
  val PlutusV3 = "PlutusV3"
  val PlutusScriptV1 = "PlutusScriptV1"
  val PlutusScriptV2 = "PlutusScriptV2"
  val PlutusScriptV3 = "PlutusScriptV3"
  val UnknownPlutusScriptV = "UnknownPlutusScriptV"

  def make[F[_], D[_]: Monad: LiftConnectionIO: Throws](implicit
    txr: Txr[F, D],
    repos: RepoBundle[D]
  ): NetworkParamsService[F] = new Live[F, D](txr, repos)

  final class Live[F[_], D[_]: Monad: Throws](txr: Txr[F, D], repos: RepoBundle[D]) extends NetworkParamsService[F] {

    def getNetworkParams: F[EnvParams] =
      (for {
        meta            <- repos.network.getMeta
        epochParams     <- repos.network.getLastEpochParams
      } yield 
          EnvParams(
            ProtocolParams.fromEpochParams(epochParams),
            NetworkName(meta.networkName),
            SystemStart.fromExplorer(meta.startTime),
            epochParams.collateralPercent
          )
        ) ||> txr.trans

  }
}
