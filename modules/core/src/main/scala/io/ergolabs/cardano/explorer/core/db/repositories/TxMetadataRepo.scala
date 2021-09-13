package io.ergolabs.cardano.explorer.core.db.repositories

import cats.data.NonEmptyList
import cats.tagless.syntax.functorK._
import cats.{FlatMap, Functor}
import derevo.derive
import doobie.ConnectionIO
import io.ergolabs.cardano.explorer.core.db.models.TxMetadata
import io.ergolabs.cardano.explorer.core.db.sql.TxMetadataSql
import tofu.doobie.LiftConnectionIO
import tofu.doobie.log.EmbeddableLogHandler
import tofu.higherKind.derived.representableK
import tofu.logging.Logs
import tofu.syntax.monadic._

@derive(representableK)
trait TxMetadataRepo[F[_]] {

  def getByTxId(txId: Long): F[Option[TxMetadata]]

  def getByTxIds(txIds: NonEmptyList[Long]): F[List[TxMetadata]]
}

object TxMetadataRepo {

  def make[I[_]: Functor, D[_]: FlatMap: LiftConnectionIO](implicit
    elh: EmbeddableLogHandler[D],
    logs: Logs[I, D]
  ): I[TxMetadataRepo[D]] =
    logs.forService[TxMetadataRepo[D]].map { implicit l =>
      elh.embed { implicit lh =>
        new LiveCIO(new TxMetadataSql).mapK(LiftConnectionIO[D].liftF)
      }
    }

  final class LiveCIO(sql: TxMetadataSql) extends TxMetadataRepo[ConnectionIO] {

    def getByTxId(txId: Long): ConnectionIO[Option[TxMetadata]] =
      sql.getByTxId(txId).option

    def getByTxIds(txIds: NonEmptyList[Long]): ConnectionIO[List[TxMetadata]] =
      sql.getByTxIds(txIds).to[List]
  }
}
