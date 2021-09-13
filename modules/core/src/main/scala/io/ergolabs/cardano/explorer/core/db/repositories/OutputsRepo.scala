package io.ergolabs.cardano.explorer.core.db.repositories

import cats.data.NonEmptyList
import cats.tagless.syntax.functorK._
import cats.{FlatMap, Functor}
import derevo.derive
import doobie.ConnectionIO
import io.ergolabs.cardano.explorer.core.db.models.Output
import io.ergolabs.cardano.explorer.core.db.sql.OutputsSql
import io.ergolabs.cardano.explorer.core.types.TxHash
import tofu.doobie.LiftConnectionIO
import tofu.doobie.log.EmbeddableLogHandler
import tofu.higherKind.derived.representableK
import tofu.logging.Logs
import tofu.syntax.monadic._

@derive(representableK)
trait OutputsRepo[F[_]] {

  def getByTxId(txId: Long): F[List[Output]]

  def getByTxHash(txHash: TxHash): F[List[Output]]

  def getByTxIds(txIds: NonEmptyList[Long]): F[List[Output]]
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

    def getByTxId(txId: Long): ConnectionIO[List[Output]] =
      sql.getByTxId(txId).to[List]

    def getByTxHash(txHash: TxHash): ConnectionIO[List[Output]] =
      sql.getByTxHash(txHash).to[List]

    def getByTxIds(txIds: NonEmptyList[Long]): ConnectionIO[List[Output]] =
      sql.getByTxIds(txIds).to[List]
  }
}
