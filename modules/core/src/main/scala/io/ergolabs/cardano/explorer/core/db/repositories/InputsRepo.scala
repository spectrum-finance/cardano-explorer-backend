package io.ergolabs.cardano.explorer.core.db.repositories

import cats.tagless.syntax.functorK._
import cats.{FlatMap, Functor}
import derevo.derive
import doobie.ConnectionIO
import io.ergolabs.cardano.explorer.core.db.models.Input
import io.ergolabs.cardano.explorer.core.db.sql.InputsSql
import io.ergolabs.cardano.explorer.core.types.TxHash
import tofu.doobie.LiftConnectionIO
import tofu.doobie.log.EmbeddableLogHandler
import tofu.higherKind.derived.representableK
import tofu.logging.Logs
import tofu.syntax.monadic._

@derive(representableK)
trait InputsRepo[F[_]] {

  def getByTxId(txId: Long): F[List[Input]]

  def getByTxHash(txHash: TxHash): F[List[Input]]
}

object InputsRepo {

  def make[I[_]: Functor, D[_]: FlatMap: LiftConnectionIO](implicit
    elh: EmbeddableLogHandler[D],
    logs: Logs[I, D]
  ): I[InputsRepo[D]] =
    logs.forService[InputsRepo[D]].map { implicit l =>
      elh.embed(implicit lh => new LiveCIO(new InputsSql).mapK(LiftConnectionIO[D].liftF))
    }

  final class LiveCIO(sql: InputsSql) extends InputsRepo[ConnectionIO] {

    def getByTxId(txId: Long): ConnectionIO[List[Input]] =
      sql.getByTxId(txId).to[List]

    def getByTxHash(txHash: TxHash): ConnectionIO[List[Input]] =
      sql.getByTxHash(txHash).to[List]
  }
}
