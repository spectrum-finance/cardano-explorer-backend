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

trait NetworkParamsService[F[_]] {

  def getNetworkParams: F[EnvParams]
}

object NetworkParamsService {

  def make[F[_], D[_]: Monad: LiftConnectionIO](implicit
    txr: Txr[F, D],
    repos: RepoBundle[D]
  ): NetworkParamsService[F] = new Live[F, D](txr, repos)

  final class Live[F[_], D[_]: Monad](txr: Txr[F, D], repos: RepoBundle[D]) extends NetworkParamsService[F] {

    def getNetworkParams: F[EnvParams] =
      (for {
        meta        <- repos.network.getMeta
        epochParams <- repos.network.getLastEpochParams
        costModel <- repos.network.getCostModel(epochParams.costModelId)
        parsedCm = io.circe.parser.parse(costModel).leftMap(throw _).merge
                    .as[Map[String, Map[String, Int]]].leftMap(throw _).merge
                    .map { case (k, v) => "PlutusScriptV1" -> v }
        _ = println(parsedCm)
        // stakes      <- repos.network.getEpochStakes(epochParams.epochNo)
        res = EnvParams(
        ProtocolParams.fromEpochParams(epochParams, parsedCm),
        NetworkName(meta.networkName),
        SystemStart("2019-07-24T20:20:16Z"),
        List(PoolId("stake_test1uzxpncx82vfkl5ml00ws44hzfdh64r22kr93e79jqsumv0q8g8cy0")),
        epochParams.collateralPercent
      )
      _ = println(epochParams.priceStep)
      } yield res) ||> txr.trans
  }
}
