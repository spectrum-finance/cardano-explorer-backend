package io.ergolabs.cardano.explorer.api.v1.services

import cats.Monad
import cats.syntax.option._
import io.ergolabs.cardano.explorer.api.v1.models.AssetInfo
import io.ergolabs.cardano.explorer.core.db.repositories.RepoBundle
import io.ergolabs.cardano.explorer.core.types.AssetRef
import mouse.anyf._
import tofu.doobie.LiftConnectionIO
import tofu.doobie.transactor.Txr
import tofu.syntax.monadic._

trait Assets[F[_]] {

  def getInfo(ref: AssetRef): F[Option[AssetInfo]]
}

object Assets {

  def make[F[_], D[_]: Monad: LiftConnectionIO](implicit
    txr: Txr[F, D],
    repos: RepoBundle[D]
  ): Assets[F] = new Live[F, D](txr, repos)

  final class Live[F[_], D[_]: Monad](txr: Txr[F, D], repos: RepoBundle[D]) extends Assets[F] {

    def getInfo(ref: AssetRef): F[Option[AssetInfo]] =
      repos.assets.getMintEvents(ref).map {
        case Nil => none[AssetInfo]
        case mintEvents =>
          mintEvents
            .foldLeft(none[AssetInfo]) {
              case (Some(acc), nextEvent) =>
                Some(acc.copy(quantity = acc.quantity + nextEvent.quantity))
              case (_, event) =>
                Some(AssetInfo(event.policy, event.name, event.quantity))
            }
      } ||> txr.trans
  }
}
