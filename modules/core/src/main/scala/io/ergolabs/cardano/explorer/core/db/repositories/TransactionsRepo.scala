package io.ergolabs.cardano.explorer.core.db.repositories

import cats.tagless.syntax.functorK._
import cats.{FlatMap, Functor}
import derevo.derive
import doobie.ConnectionIO
import io.ergolabs.cardano.explorer.core.db.SortOrder
import io.ergolabs.cardano.explorer.core.db.models.Transaction
import io.ergolabs.cardano.explorer.core.db.sql.TransactionsSql
import io.ergolabs.cardano.explorer.core.types.TxHash
import tofu.doobie.LiftConnectionIO
import tofu.doobie.log.EmbeddableLogHandler
import tofu.higherKind.Mid
import tofu.higherKind.derived.representableK
import tofu.logging.{Logging, Logs}
import tofu.syntax.logging._
import tofu.syntax.monadic._

@derive(representableK)
trait TransactionsRepo[F[_]] {

  def getByTxHash(txHash: TxHash): F[Option[Transaction]]

  def getAll(offset: Int, limit: Int, ordering: SortOrder): F[List[Transaction]]

  def countAll: F[Int]
}

object TransactionsRepo {

  def make[I[_]: Functor, D[_]: FlatMap: LiftConnectionIO](implicit
    elh: EmbeddableLogHandler[D],
    logs: Logs[I, D]
  ): I[TransactionsRepo[D]] =
    logs.forService[TransactionsRepo[D]].map { implicit l =>
      elh.embed { implicit lh =>
        new Tracing[D] attach new LiveCIO(new TransactionsSql).mapK(LiftConnectionIO[D].liftF)
      }
    }

  final class LiveCIO(sql: TransactionsSql) extends TransactionsRepo[ConnectionIO] {

    def getByTxHash(txHash: TxHash): ConnectionIO[Option[Transaction]] =
      sql.getByTxHash(txHash).option

    def getAll(offset: Int, limit: Int, ordering: SortOrder): ConnectionIO[List[Transaction]] =
      sql.getAll(offset, limit, ordering).to[List]

    def countAll: ConnectionIO[Int] =
      sql.countAll.unique
  }

  final class Tracing[F[_]: Logging: FlatMap] extends TransactionsRepo[Mid[F, *]] {

    def getByTxHash(txHash: TxHash): Mid[F, Option[Transaction]] =
      for {
        _ <- trace"getByTxHash(txHash=$txHash)"
        r <- _
        _ <- trace"getByTxHash(txHash=$txHash) -> $r"
      } yield r

    def getAll(offset: Int, limit: Int, ordering: SortOrder): Mid[F, List[Transaction]] =
      for {
        _ <- trace"getAll(offset=$offset, limit=$limit, ordering=${ordering.value})"
        r <- _
        _ <- trace"getAll(offset=$offset, limit=$limit, ordering=${ordering.value}) -> $r"
      } yield r

    def countAll: Mid[F, Int] =
      for {
        _ <- trace"countAll()"
        r <- _
        _ <- trace"countAll() -> $r"
      } yield r
  }
}
