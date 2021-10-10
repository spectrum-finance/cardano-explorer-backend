package io.ergolabs.cardano.explorer.core.db.repositories

import cats.data.NonEmptyList
import cats.tagless.syntax.functorK._
import cats.{FlatMap, Functor}
import derevo.derive
import doobie.ConnectionIO
import io.ergolabs.cardano.explorer.core.db.models.Output
import io.ergolabs.cardano.explorer.core.db.sql.OutputsSql
import io.ergolabs.cardano.explorer.core.types.{Addr, AssetRef, OutRef, TxHash}
import tofu.doobie.LiftConnectionIO
import tofu.doobie.log.EmbeddableLogHandler
import tofu.higherKind.derived.representableK
import tofu.logging.Logs
import tofu.syntax.monadic._

@derive(representableK)
trait OutputsRepo[F[_]] {

  def getByRef(ref: OutRef): F[Option[Output]]

  def getByTxId(txId: Long): F[List[Output]]

  def getByTxHash(txHash: TxHash): F[List[Output]]

  def getByTxIds(txIds: NonEmptyList[Long]): F[List[Output]]

  def getUnspent(offset: Int, limit: Int): F[List[Output]]

  def getUnspentIndexed(minIndex: Int, limit: Int): F[List[Output]]

  def countUnspent: F[Int]

  def getUnspentByAddr(addr: Addr, offset: Int, limit: Int): F[List[Output]]

  def countUnspentByAddr(addr: Addr): F[Int]

  def getUnspentByAsset(asset: AssetRef, offset: Int, limit: Int): F[List[Output]]

  def countUnspentByAsset(asset: AssetRef): F[Int]

  def searchUnspent(
    addr: Addr,
    containsAllOf: Option[List[AssetRef]],
    containsAnyOf: Option[List[AssetRef]],
    offset: Int,
    limit: Int
  ): F[List[Output]]

  def countUnspent(addr: Addr, containsAllOf: Option[List[AssetRef]], containsAnyOf: Option[List[AssetRef]]): F[Int]
}

object OutputsRepo {

  def make[I[_]: Functor, D[_]: FlatMap: LiftConnectionIO](implicit
    elh: EmbeddableLogHandler[D],
    logs: Logs[I, D]
  ): I[OutputsRepo[D]] =
    logs.forService[OutputsRepo[D]].map { implicit l =>
      elh.embed(implicit lh => new LiveCIO(new OutputsSql).mapK(LiftConnectionIO[D].liftF))
    }

  final class LiveCIO(sql: OutputsSql) extends OutputsRepo[ConnectionIO] {

    def getByRef(ref: OutRef): ConnectionIO[Option[Output]] =
      sql.getByOutRef(ref).option

    def getByTxId(txId: Long): ConnectionIO[List[Output]] =
      sql.getByTxId(txId).to[List]

    def getByTxHash(txHash: TxHash): ConnectionIO[List[Output]] =
      sql.getByTxHash(txHash).to[List]

    def getByTxIds(txIds: NonEmptyList[Long]): ConnectionIO[List[Output]] =
      sql.getByTxIds(txIds).to[List]

    def getUnspent(offset: Int, limit: Int): ConnectionIO[List[Output]] =
      sql.getUnspent(offset, limit).to[List]

    def getUnspentIndexed(minIndex: Int, limit: Int): ConnectionIO[List[Output]] =
      sql.getUnspentIndexed(minIndex, limit).to[List]

    def countUnspent: ConnectionIO[Int] =
      sql.countUnspent.unique

    def getUnspentByAddr(addr: Addr, offset: Int, limit: Int): ConnectionIO[List[Output]] =
      sql.getUnspentByAddr(addr, offset, limit).to[List]

    def countUnspentByAddr(addr: Addr): ConnectionIO[Int] =
      sql.countUnspentByAddr(addr).unique

    def getUnspentByAsset(asset: AssetRef, offset: Int, limit: Int): ConnectionIO[List[Output]] =
      sql.getUnspentByAsset(asset, offset, limit).to[List]

    def countUnspentByAsset(asset: AssetRef): ConnectionIO[Int] =
      sql.countUnspentByAsset(asset).unique

    def searchUnspent(
      addr: Addr,
      containsAllOf: Option[List[AssetRef]],
      containsAnyOf: Option[List[AssetRef]],
      offset: Int,
      limit: Int
    ): ConnectionIO[List[Output]] =
      sql.searchUnspent(addr, containsAllOf, containsAnyOf, offset, limit).to[List]

    def countUnspent(
      addr: Addr,
      containsAllOf: Option[List[AssetRef]],
      containsAnyOf: Option[List[AssetRef]]
    ): ConnectionIO[Int] =
      sql.countUnspent(addr, containsAllOf, containsAnyOf).unique
  }
}
