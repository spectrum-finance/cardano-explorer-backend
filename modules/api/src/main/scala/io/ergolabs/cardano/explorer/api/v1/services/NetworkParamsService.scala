package io.ergolabs.cardano.explorer.api.v1.services

import cats.Monad
import cats.syntax.option._
import cats.syntax.either._
import io.ergolabs.cardano.explorer.core.db.repositories.RepoBundle
import mouse.anyf._
import tofu.doobie.LiftConnectionIO
import tofu.doobie.transactor.Txr
import tofu.syntax.monadic._
import io.ergolabs.cardano.explorer.api.v1.models.EnvParams

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
        stakes      <- repos.network.getEpochStakes(epochParams.epochNo)
      } yield EnvParams(???, ???, ???, ???, ???, ???)
//        EnvParams(
//        meta.startTime,
//        meta.networkName,
//        epochParams.epochNo,
//        stakes.ids
//      )
        ) ||> txr.trans
  }
}