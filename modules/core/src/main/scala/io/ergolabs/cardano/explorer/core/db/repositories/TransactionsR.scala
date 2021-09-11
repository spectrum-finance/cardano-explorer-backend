package io.ergolabs.cardano.explorer.core.db.repositories

import cats.tagless.syntax.functorK._
import cats.{FlatMap, Functor}
import derevo.derive
import doobie.ConnectionIO
import io.ergolabs.cardano.explorer.core.db.models.Transaction
import io.ergolabs.cardano.explorer.core.db.sql.TransactionsQ
import io.ergolabs.cardano.explorer.core.types.TxHash
import tofu.doobie.LiftConnectionIO
import tofu.doobie.log.EmbeddableLogHandler
import tofu.higherKind.Mid
import tofu.higherKind.derived.representableK
import tofu.logging.{Logging, Logs}
import tofu.syntax.logging._
import tofu.syntax.monadic._

@derive(representableK)
trait TransactionsR[F[_]] {

  def getByTxHash(txHash: TxHash): F[Option[Transaction]]
}

object TransactionsR {

  def make[I[_]: Functor, D[_]: FlatMap: LiftConnectionIO](implicit
    elh: EmbeddableLogHandler[D],
    logs: Logs[I, D]
  ): I[TransactionsR[D]] =
    logs.forService[TransactionsR[D]].map { implicit l =>
      elh.embed { implicit lh =>
        new Tracing[D] attach new LiveCIO(new TransactionsQ).mapK(LiftConnectionIO[D].liftF)
      }
    }

  final class LiveCIO(sql: TransactionsQ) extends TransactionsR[ConnectionIO] {

    def getByTxHash(txHash: TxHash): ConnectionIO[Option[Transaction]] =
      sql.getByTxHash(txHash).option
  }

  final class Tracing[F[_]: Logging: FlatMap] extends TransactionsR[Mid[F, *]] {

    def getByTxHash(txHash: TxHash): Mid[F, Option[Transaction]] =
      for {
        _ <- trace"getByTxHash(txHash=$txHash)"
        r <- _
        _ <- trace"getByTxHash(txHash=$txHash) -> $r"
      } yield r
  }
}
