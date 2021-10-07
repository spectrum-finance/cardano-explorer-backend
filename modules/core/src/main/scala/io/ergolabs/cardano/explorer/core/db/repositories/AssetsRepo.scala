package io.ergolabs.cardano.explorer.core.db.repositories

import cats.data.NonEmptyList
import cats.{FlatMap, Functor}
import cats.tagless.syntax.functorK._
import derevo.derive
import doobie.ConnectionIO
import io.ergolabs.cardano.explorer.core.db.models.{AssetMintEvent, AssetOutput}
import io.ergolabs.cardano.explorer.core.db.sql.AssetsSql
import io.ergolabs.cardano.explorer.core.types.{Asset32, AssetRef, TxHash}
import tofu.doobie.LiftConnectionIO
import tofu.doobie.log.EmbeddableLogHandler
import tofu.higherKind.derived.representableK
import tofu.logging.Logs
import tofu.syntax.monadic._

@derive(representableK)
trait AssetsRepo[F[_]] {

  def getByTxId(txId: Long): F[List[AssetOutput]]

  def getByTxHash(txHash: TxHash): F[List[AssetOutput]]

  def getByTxIds(txIds: NonEmptyList[Long]): F[List[AssetOutput]]

  def getByOutputId(outputId: Long): F[List[AssetOutput]]

  def getByOutputIds(outputIds: NonEmptyList[Long]): F[List[AssetOutput]]

  def getMintEvents(ref: AssetRef): F[List[AssetMintEvent]]
}

object AssetsRepo {

  def make[I[_]: Functor, D[_]: FlatMap: LiftConnectionIO](implicit
    elh: EmbeddableLogHandler[D],
    logs: Logs[I, D]
  ): I[AssetsRepo[D]] =
    logs.forService[AssetsRepo[D]].map { implicit l =>
      elh.embed { implicit lh =>
        new LiveCIO(new AssetsSql).mapK(LiftConnectionIO[D].liftF)
      }
    }

  final class LiveCIO(sql: AssetsSql) extends AssetsRepo[ConnectionIO] {

    def getByTxId(txId: Long): ConnectionIO[List[AssetOutput]] =
      sql.getByTxId(txId).to[List]

    def getByTxHash(txHash: TxHash): ConnectionIO[List[AssetOutput]] =
      sql.getByTxHash(txHash).to[List]

    def getByTxIds(txIds: NonEmptyList[Long]): ConnectionIO[List[AssetOutput]] =
      sql.getByTxIds(txIds).to[List]

    def getByOutputId(outputId: Long): ConnectionIO[List[AssetOutput]] =
      sql.getByOutputId(outputId).to[List]

    def getByOutputIds(outputIds: NonEmptyList[Long]): ConnectionIO[List[AssetOutput]] =
      sql.getByOutputIds(outputIds).to[List]

    def getMintEvents(ref: AssetRef): ConnectionIO[List[AssetMintEvent]] =
      sql.getMintEventsByAsset(ref).to[List]
  }
}
