package io.ergolabs.cardano.explorer.api.v1.services

import cats.Monad
import cats.data.{NonEmptyList, OptionT}
import io.ergolabs.cardano.explorer.api.v1.models._
import io.ergolabs.cardano.explorer.core.db.models.{Output => DbOutput}
import io.ergolabs.cardano.explorer.core.db.repositories.RepoBundle
import io.ergolabs.cardano.explorer.core.models.Sorting.SortOrder
import io.ergolabs.cardano.explorer.core.types.{Addr, AssetRef, OutRef, PaymentCred}
import mouse.anyf._
import tofu.doobie.LiftConnectionIO
import tofu.doobie.transactor.Txr
import tofu.syntax.monadic._

trait Outputs[F[_]] {

  def getByOutRef(ref: OutRef): F[Option[TxOutput]]

  def getAll(paging: Paging, ordering: SortOrder): F[List[TxOutput]]

  def getUnspent(paging: Paging, ordering: SortOrder): F[Items[TxOutput]]

  def getUnspent(indexing: Indexing, ordering: SortOrder): F[Items[TxOutput]]

  def getUnspentByAddr(addr: Addr, paging: Paging): F[Items[TxOutput]]

  def getUnspentByPCred(pcred: PaymentCred, paging: Paging, ordering: SortOrder): F[Items[TxOutput]]

  def getUnspentByAsset(asset: AssetRef, paging: Paging, ordering: SortOrder): F[Items[TxOutput]]

  def searchUnspent(query: UtxoSearch, paging: Paging): F[Items[TxOutput]]
}

object Outputs {

  def make[F[_], D[_]: Monad: LiftConnectionIO](implicit
    txr: Txr[F, D],
    repos: RepoBundle[D]
  ): Outputs[F] = new Live[F, D](txr, repos)

  final class Live[F[_], D[_]: Monad](txr: Txr[F, D], repos: RepoBundle[D]) extends Outputs[F] {
    import repos._

    def getByOutRef(ref: OutRef): F[Option[TxOutput]] =
      (for {
        out    <- OptionT(outputs.getByRef(ref))
        assets <- OptionT.liftF(assets.getByOutputId(out.id))
      } yield TxOutput.inflate(out, assets.map(_.asset))).value ||> txr.trans

    def getAll(paging: Paging, ordering: SortOrder): F[List[TxOutput]] =
      (for {
        txs   <- outputs.getAll(paging.offset, paging.limit, ordering)
        batch <- getBatch(txs)
      } yield batch) ||> txr.trans

    def getUnspent(paging: Paging, ordering: SortOrder): F[Items[TxOutput]] =
      (for {
        txs   <- outputs.getUnspent(paging.offset, paging.limit, ordering)
        total <- outputs.countUnspent
        batch <- getBatch(txs, total)
      } yield batch) ||> txr.trans

    def getUnspent(indexing: Indexing, ordering: SortOrder): F[Items[TxOutput]] =
      (for {
        txs   <- outputs.getUnspentIndexed(indexing.minIndex, indexing.limit, ordering)
        //total <- outputs.countUnspent
        batch <- getBatch(txs, 0) // todo
      } yield batch) ||> txr.trans

    def getUnspentByAddr(addr: Addr, paging: Paging): F[Items[TxOutput]] =
      (for {
        txs   <- outputs.getUnspentByAddr(addr, paging.offset, paging.limit)
        total <- outputs.countUnspentByAddr(addr)
        batch <- getBatch(txs, total)
      } yield batch) ||> txr.trans

    def getUnspentByPCred(pcred: PaymentCred, paging: Paging, ordering: SortOrder): F[Items[TxOutput]] =
      (for {
        txs   <- outputs.getUnspentByPCred(pcred, paging.offset, paging.limit, ordering)
        total <- outputs.countUnspentByPCred(pcred)
        batch <- getBatch(txs, total)
      } yield batch) ||> txr.trans

    def getUnspentByAsset(asset: AssetRef, paging: Paging, ordering: SortOrder): F[Items[TxOutput]] =
      (for {
        txs   <- outputs.getUnspentByAsset(asset, paging.offset, paging.limit, ordering)
        total <- outputs.countUnspentByAsset(asset)
        batch <- getBatch(txs, total)
      } yield batch) ||> txr.trans

    def searchUnspent(query: UtxoSearch, paging: Paging): F[Items[TxOutput]] =
      (for {
        txs   <- outputs.searchUnspent(query.paymentCred, query.containsAllOf, query.containsAnyOf, paging.offset, paging.limit)
        total <- outputs.countUnspent(query.paymentCred, query.containsAllOf, query.containsAnyOf)
        batch <- getBatch(txs, total)
      } yield batch) ||> txr.trans

    private def getBatch(os: List[DbOutput], total: Int): D[Items[TxOutput]] =
      NonEmptyList.fromList(os.map(_.id)) match {
        case Some(ids) =>
          for {
            assets <- assets.getByOutputIds(ids)
            xs = TxOutput.inflateBatch(os, assets)
          } yield Items(xs, total)
        case None => Items.empty[TxOutput].pure
      }

    private def getBatch(os: List[DbOutput]): D[List[TxOutput]] =
      NonEmptyList.fromList(os.map(_.id)) match {
        case Some(ids) =>
          for {
            assets <- assets.getByOutputIds(ids)
          } yield TxOutput.inflateBatch(os, assets)
        case None => List.empty[TxOutput].pure
      }
  }
}
