package io.ergolabs.cardano.explorer.api.v1.services

import cats.Monad
import mouse.anyf._
import io.ergolabs.cardano.explorer.api.v1.models.BlockInfo
import io.ergolabs.cardano.explorer.core.db.repositories.RepoBundle
import tofu.doobie.LiftConnectionIO
import tofu.doobie.transactor.Txr
import tofu.syntax.monadic._

trait Blocks[F[_]] {

  def getBestBlockInfo: F[BlockInfo]
}

object Blocks {

  def make[F[_], D[_]: Monad: LiftConnectionIO](implicit
    txr: Txr[F, D],
    repos: RepoBundle[D]
  ): Blocks[F] = new Live[F, D](txr, repos)

  final class Live[F[_], D[_]: Monad](txr: Txr[F, D], repos: RepoBundle[D]) extends Blocks[F] {
    import repos._

    override def getBestBlockInfo: F[BlockInfo] =
      (for {
        bestBlockHeader <- repos.blocks.getBestBlock
        slotLeader      <- repos.slots.getSlotLeaderById(bestBlockHeader.slotLeaderId)
      } yield BlockInfo.inflate(bestBlockHeader, slotLeader)) ||> txr.trans
  }
}
