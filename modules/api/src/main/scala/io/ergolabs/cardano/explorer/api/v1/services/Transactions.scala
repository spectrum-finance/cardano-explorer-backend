package io.ergolabs.cardano.explorer.api.v1.services

import cats.Monad
import cats.data.{NonEmptyList, OptionT}
import io.ergolabs.cardano.explorer.api.v1.models.{Items, Paging, Transaction}
import io.ergolabs.cardano.explorer.core.db.SortOrder
import io.ergolabs.cardano.explorer.core.db.repositories.TxRepoBundle
import io.ergolabs.cardano.explorer.core.types.TxHash
import mouse.anyf._
import tofu.doobie.LiftConnectionIO
import tofu.doobie.transactor.Txr
import tofu.syntax.monadic._

trait Transactions[F[_]] {

  def getByTxHash(txHash: TxHash): F[Option[Transaction]]

  def getAll(paging: Paging): F[Items[Transaction]]
}

object Transactions {

  def make[F[_], D[_]: Monad: LiftConnectionIO](implicit
    txr: Txr[F, D],
    repos: TxRepoBundle[D]
  ): Transactions[F] = new Live[F, D](txr, repos)

  final class Live[F[_], D[_]: Monad](txr: Txr[F, D], repos: TxRepoBundle[D]) extends Transactions[F] {
    import repos._

    def getByTxHash(txHash: TxHash): F[Option[Transaction]] =
      (for {
        tx     <- OptionT(transactions.getByTxHash(txHash))
        ins    <- OptionT.liftF(inputs.getByTxId(tx.id))
        outs   <- OptionT.liftF(outputs.getByTxId(tx.id))
        assets <- OptionT.liftF(assets.getByTxId(tx.id))
      } yield Transaction.inflate(tx, ins, outs, assets)).value ||> txr.trans

    def getAll(paging: Paging): F[Items[Transaction]] =
      transactions.getAll(paging.offset, paging.limit, SortOrder.Desc).flatMap { txs =>
        NonEmptyList.fromList(txs.map(_.id)) match {
          case Some(ids) =>
            for {
              total  <- transactions.countAll
              ins    <- inputs.getByTxIds(ids)
              outs   <- outputs.getByTxIds(ids)
              assets <- assets.getByTxIds(ids)
              xs = Transaction.inflateBatch(txs, ins, outs, assets)
            } yield Items(xs, total)
          case None => Items.empty[Transaction].pure
        }
      } ||> txr.trans
  }
}
