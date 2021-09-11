package io.ergolabs.cardano.explorer.api.v1.services

import cats.Monad
import cats.data.OptionT
import io.ergolabs.cardano.explorer.api.v1.models.Transaction
import io.ergolabs.cardano.explorer.core.db.repositories.TxRepoBundle
import io.ergolabs.cardano.explorer.core.types.TxHash
import mouse.anyf._
import tofu.doobie.LiftConnectionIO
import tofu.doobie.transactor.Txr

trait Transactions[F[_]] {

  def getByTxHash(txHash: TxHash): F[Option[Transaction]]
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
        ins    <- OptionT.liftF(inputs.getByTxHash(txHash))
        outs   <- OptionT.liftF(outputs.getByTxHash(txHash))
        assets <- OptionT.liftF(assets.getByTxHash(txHash))
      } yield Transaction.inflate(tx, ins, outs, assets)).value ||> txr.trans
  }
}
