package io.ergolabs.cardano.explorer.api.v1.services

import cats.Monad
import cats.data.{NonEmptyList, OptionT}
import fs2.{Chunk, Pipe, Stream}
import io.ergolabs.cardano.explorer.api.v1.models.{Items, Paging, Transaction}
import io.ergolabs.cardano.explorer.core.db.models.{Transaction => DbTransaction}
import io.ergolabs.cardano.explorer.core.db.repositories.RepoBundle
import io.ergolabs.cardano.explorer.core.models.Sorting.SortOrder
import io.ergolabs.cardano.explorer.core.types.{Addr, PaymentCred, TxHash}
import mouse.anyf._
import tofu.doobie.LiftConnectionIO
import tofu.doobie.transactor.Txr
import tofu.syntax.monadic._

trait Transactions[F[_]] {

  def getByTxHash(txHash: TxHash): F[Option[Transaction]]

  def getAll(paging: Paging): F[Items[Transaction]]

  def streamAll(paging: Paging, ordering: SortOrder): Stream[F, Transaction]

  def getByBlock(blockHeight: Int): F[Items[Transaction]]

  def getByAddress(addr: Addr, paging: Paging): F[Items[Transaction]]

  def getByPCred(pcred: PaymentCred, paging: Paging): F[List[Transaction]]
}

object Transactions {

  val ChunkSize = 64

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
        inAssets  <- OptionT.liftF(assets.getByInTxId(tx.id))
        outAssets <- OptionT.liftF(assets.getByOutTxId(tx.id))
        meta      <- OptionT.liftF(metadata.getByTxId(tx.id))
      } yield Transaction.inflate(tx, ins, outs, inAssets, outAssets, redeemers, meta)).value ||> txr.trans

    def getAll(paging: Paging): F[Items[Transaction]] =
      (for {
        txs   <- transactions.getAll(paging.offset, paging.limit, SortOrder.Desc)
        total <- transactions.countAll
        batch <- getBatch(txs)
      } yield Items(batch, total)) ||> txr.trans

    def streamAll(paging: Paging, ordering: SortOrder): Stream[F, Transaction] =
      transactionsStream.streamAll(paging.offset, paging.limit, ordering)
        .chunkN(ChunkSize)
        .through(streamBatch)
        .thrushK(txr.transP)

    def getByBlock(blockHeight: Int): F[Items[Transaction]] =
      (for {
        txs   <- transactions.getByBlock(blockHeight)
        total <- transactions.countByBlock(blockHeight)
        batch <- getBatch(txs)
      } yield Items(batch, total)) ||> txr.trans

    def getByAddress(addr: Addr, paging: Paging): F[Items[Transaction]] =
      (for {
        txs   <- transactions.getByAddress(addr, paging.offset, paging.limit)
        total <- transactions.countByAddress(addr)
        batch <- getBatch(txs)
      } yield Items(batch, total)) ||> txr.trans

    def getByPCred(pcred: PaymentCred, paging: Paging): F[List[Transaction]] =
      (for {
        txs   <- transactions.getByPCred(pcred, paging.offset, paging.limit)
        batch <- getBatch(txs)
      } yield batch) ||> txr.trans

    private def getBatch(txs: List[DbTransaction]): D[List[Transaction]] =
      NonEmptyList.fromList(txs.map(_.id)) match {
        case Some(ids) =>
          for {
            ins       <- inputs.getByTxIds(ids)
            redeemers <- redeemer.getByTxIds(ids)
            outs      <- outputs.getByTxIds(ids)
            inAssets  <- assets.getByInTxIds(ids)
            outAssets <- assets.getByOutTxIds(ids)
            meta      <- metadata.getByTxIds(ids)
          } yield Transaction.inflateBatch(txs, ins, outs, inAssets, outAssets, redeemers, meta)
        case None => List.empty[Transaction].pure
      }

    private def streamBatch: Pipe[D, Chunk[DbTransaction], Transaction] =
      _ >>= (chunk => Stream.evals(getBatch(chunk.toList)))
  }
}
