package io.ergolabs.cardano.explorer.core.db.repositories

import cats.tagless.syntax.functorK._
import cats.{FlatMap, Functor}
import derevo.derive
import doobie.ConnectionIO
import io.ergolabs.cardano.explorer.core.db.SortOrder
import io.ergolabs.cardano.explorer.core.db.models.Transaction
import io.ergolabs.cardano.explorer.core.db.sql.TransactionsSql
import io.ergolabs.cardano.explorer.core.types.{Addr, TxHash}
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

  def getByBlock(blockHeight: Int): F[List[Transaction]]

  def getByAddress(addr: Addr, offset: Int, limit: Int): F[List[Transaction]]

  def countByAddress(addr: Addr): F[Int]
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

    def getByBlock(blockHeight: Int): ConnectionIO[List[Transaction]] =
      sql.getByBlock(blockHeight).to[List]

    def getByAddress(addr: Addr, offset: Int, limit: Int): ConnectionIO[List[Transaction]] =
      sql.getByAddress(addr, offset, limit).to[List]

    def countByAddress(addr: Addr): ConnectionIO[Int] =
      sql.countByAddress(addr).unique
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

    def getByBlock(blockHeight: Int): Mid[F, List[Transaction]] =
      for {
        _ <- trace"getByBlockHeight(blockHeight=$blockHeight)"
        r <- _
        _ <- trace"getByBlockHeight(blockHeight=$blockHeight) -> $r"
      } yield r

    def getByAddress(addr: Addr, offset: Int, limit: Int): Mid[F, List[Transaction]] =
      for {
        _ <- trace"getByAddress(addr=$addr, offset=$offset, limit=$limit)"
        r <- _
        _ <- trace"getByAddress(addr=$addr, offset=$offset, limit=$limit) -> $r"
      } yield r

    def countByAddress(addr: Addr): Mid[F, Int] =
      for {
        _ <- trace"countByAddress(addr=$addr)"
        r <- _
        _ <- trace"countByAddress(addr=$addr) -> $r"
      } yield r
  }
}
