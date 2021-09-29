package io.ergolabs.cardano.explorer.api.v1.services

import cats.Monad
import cats.data.{NonEmptyList, OptionT}
import io.ergolabs.cardano.explorer.api.v1.models.{Items, Paging, Transaction}
import io.ergolabs.cardano.explorer.core.db.SortOrder
import io.ergolabs.cardano.explorer.core.db.models.{Transaction => DbTransaction}
import io.ergolabs.cardano.explorer.core.db.repositories.RepoBundle
import io.ergolabs.cardano.explorer.core.types.{Addr, TxHash}
import mouse.anyf._
import tofu.doobie.LiftConnectionIO
import tofu.doobie.transactor.Txr
import tofu.syntax.monadic._

trait Transactions[F[_]] {

  def getByTxHash(txHash: TxHash): F[Option[Transaction]]

  def getAll(paging: Paging): F[Items[Transaction]]

  def getByBlock(blockHeight: Int): F[Items[Transaction]]

  def getByAddress(addr: Addr, paging: Paging): F[Items[Transaction]]
}

object Transactions {

  def make[F[_], D[_]: Monad: LiftConnectionIO](implicit
    txr: Txr[F, D],
    repos: RepoBundle[D]
  ): Transactions[F] = new Live[F, D](txr, repos)

  final class Live[F[_], D[_]: Monad](txr: Txr[F, D], repos: RepoBundle[D]) extends Transactions[F] {
    import repos._

    def getByTxHash(txHash: TxHash): F[Option[Transaction]] =
      (for {
        tx        <- OptionT(transactions.getByTxHash(txHash))
        ins       <- OptionT.liftF(inputs.getByTxId(tx.id))
        redeemers <- OptionT.liftF(redeemer.getByTxId(tx.id))
        outs      <- OptionT.liftF(outputs.getByTxId(tx.id))
        assets    <- OptionT.liftF(assets.getByTxId(tx.id))
        meta      <- OptionT.liftF(metadata.getByTxId(tx.id))
      } yield Transaction.inflate(tx, ins, outs, assets, redeemers, meta)).value ||> txr.trans

    def getAll(paging: Paging): F[Items[Transaction]] =
      (for {
        txs   <- transactions.getAll(paging.offset, paging.limit, SortOrder.Desc)
        total <- transactions.countAll
        batch <- getBatch(txs, total)
      } yield batch) ||> txr.trans

    def getByBlock(blockHeight: Int): F[Items[Transaction]] =
      (for {
        txs   <- transactions.getByBlock(blockHeight)
        total <- transactions.countByBlock(blockHeight)
        batch <- getBatch(txs, total)
      } yield batch) ||> txr.trans

    def getByAddress(addr: Addr, paging: Paging): F[Items[Transaction]] =
      (for {
        txs   <- transactions.getByAddress(addr, paging.offset, paging.limit)
        total <- transactions.countByAddress(addr)
        batch <- getBatch(txs, total)
      } yield batch) ||> txr.trans

    private def getBatch(txs: List[DbTransaction], total: Int) =
      NonEmptyList.fromList(txs.map(_.id)) match {
        case Some(ids) =>
          for {
            ins       <- inputs.getByTxIds(ids)
            redeemers <- redeemer.getByTxIds(ids)
            outs      <- outputs.getByTxIds(ids)
            assets    <- assets.getByTxIds(ids)
            meta      <- metadata.getByTxIds(ids)
            xs = Transaction.inflateBatch(txs, ins, outs, assets, redeemers, meta)
          } yield Items(xs, total)
        case None => Items.empty[Transaction].pure
      }
  }
}
