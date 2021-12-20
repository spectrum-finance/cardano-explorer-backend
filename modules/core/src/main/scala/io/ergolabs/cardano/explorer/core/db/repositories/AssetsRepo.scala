package io.ergolabs.cardano.explorer.core.db.repositories

import cats.data.NonEmptyList
import cats.{FlatMap, Functor}
import cats.tagless.syntax.functorK._
import derevo.derive
import doobie.ConnectionIO
import io.ergolabs.cardano.explorer.core.db.models.{AssetInput, AssetMintEvent, AssetOutput}
import io.ergolabs.cardano.explorer.core.db.sql.AssetsSql
import io.ergolabs.cardano.explorer.core.types.{Asset32, AssetRef, TxHash}
import tofu.doobie.LiftConnectionIO
import tofu.doobie.log.EmbeddableLogHandler
import tofu.higherKind.derived.representableK
import tofu.logging.Logs
import tofu.syntax.monadic._

@derive(representableK)
trait AssetsRepo[F[_]] {

  def getByOutTxId(txId: Long): F[List[AssetOutput]]

  def getByInTxId(txId: Long): F[List[AssetInput]]

  def getByOutTxHash(txHash: TxHash): F[List[AssetOutput]]

  def getByOutTxIds(txIds: NonEmptyList[Long]): F[List[AssetOutput]]

  def getByInTxIds(txIds: NonEmptyList[Long]): F[List[AssetInput]]

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

    def getByOutTxId(txId: Long): ConnectionIO[List[AssetOutput]] =
      sql.getByOutTxId(txId).to[List]

    def getByInTxId(txId: Long): ConnectionIO[List[AssetInput]] =
      sql.getByInTxId(txId).to[List]

    def getByOutTxHash(txHash: TxHash): ConnectionIO[List[AssetOutput]] =
      sql.getByOutTxHash(txHash).to[List]

    def getByOutTxIds(txIds: NonEmptyList[Long]): ConnectionIO[List[AssetOutput]] =
      sql.getByOutTxIds(txIds).to[List]

    def getByInTxIds(txIds: NonEmptyList[Long]): ConnectionIO[List[AssetInput]] =
      sql.getByInTxIds(txIds).to[List]

    def getByOutputId(outputId: Long): ConnectionIO[List[AssetOutput]] =
      sql.getByOutputId(outputId).to[List]

    def getByOutputIds(outputIds: NonEmptyList[Long]): ConnectionIO[List[AssetOutput]] =
      sql.getByOutputIds(outputIds).to[List]

    def getMintEvents(ref: AssetRef): ConnectionIO[List[AssetMintEvent]] =
      sql.getMintEventsByAsset(ref).to[List]
  }
}
