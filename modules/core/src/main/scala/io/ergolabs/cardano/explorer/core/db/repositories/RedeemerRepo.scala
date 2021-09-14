package io.ergolabs.cardano.explorer.core.db.repositories

import cats.data.NonEmptyList
import cats.tagless.syntax.functorK._
import cats.{FlatMap, Functor}
import derevo.derive
import doobie.ConnectionIO
import io.ergolabs.cardano.explorer.core.db.models.Redeemer
import io.ergolabs.cardano.explorer.core.db.sql.RedeemerSql
import tofu.doobie.LiftConnectionIO
import tofu.doobie.log.EmbeddableLogHandler
import tofu.higherKind.derived.representableK
import tofu.logging.Logs
import tofu.syntax.monadic._

@derive(representableK)
trait RedeemerRepo[F[_]] {

  def getByTxId(txId: Long): F[List[Redeemer]]

  def getByTxIds(txIds: NonEmptyList[Long]): F[List[Redeemer]]
}

object RedeemerRepo {

  def make[I[_]: Functor, D[_]: FlatMap: LiftConnectionIO](implicit
    elh: EmbeddableLogHandler[D],
    logs: Logs[I, D]
  ): I[RedeemerRepo[D]] =
    logs.forService[RedeemerRepo[D]].map { implicit l =>
      elh.embed(implicit lh => new LiveCIO(new RedeemerSql).mapK(LiftConnectionIO[D].liftF))
    }

  final class LiveCIO(sql: RedeemerSql) extends RedeemerRepo[ConnectionIO] {

    def getByTxId(txId: Long): ConnectionIO[List[Redeemer]] =
      sql.getByTxId(txId).to[List]

    def getByTxIds(txIds: NonEmptyList[Long]): ConnectionIO[List[Redeemer]] =
      sql.getByTxIds(txIds).to[List]
  }
}
