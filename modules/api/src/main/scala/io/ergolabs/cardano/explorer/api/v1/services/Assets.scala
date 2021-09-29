package io.ergolabs.cardano.explorer.api.v1.services

import cats.Monad
import io.ergolabs.cardano.explorer.api.v1.models.AssetInfo
import mouse.anyf._
import io.ergolabs.cardano.explorer.core.db.repositories.RepoBundle
import io.ergolabs.cardano.explorer.core.types.{Asset32, PolicyHash}
import tofu.doobie.LiftConnectionIO
import tofu.doobie.transactor.Txr
import tofu.syntax.monadic._
import cats.syntax.option._

trait Assets[F[_]] {

  def getInfo(assetId: Asset32): F[Option[AssetInfo]]
}

object Assets {

  def make[F[_], D[_]: Monad: LiftConnectionIO](implicit
    txr: Txr[F, D],
    repos: RepoBundle[D]
  ): Assets[F] = new Live[F, D](txr, repos)

  final class Live[F[_], D[_]: Monad](txr: Txr[F, D], repos: RepoBundle[D]) extends Assets[F] {

    override def getInfo(assetId: Asset32): F[Option[AssetInfo]] = {
      repos.assets.getMintEvents(assetId).map {
        case emptyList if emptyList.isEmpty => none[AssetInfo]
        case mintEvents =>
          mintEvents
            .foldLeft(AssetInfo.emptyById(assetId)) { case (acc, nextEvent) =>
              acc.copy(
                PolicyHash(nextEvent.policy.value),
                assetId,
                acc.quantity + nextEvent.quantity,
                acc.mintTxsIds :+ nextEvent.txId
              )
            }
            .some
      }
    } ||> txr.trans
  }
}
