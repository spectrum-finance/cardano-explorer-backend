package io.ergolabs.cardano.explorer.core.db.repositories

import cats.{FlatMap, Functor}
import cats.tagless.syntax.functorK._
import derevo.derive
import doobie.ConnectionIO
import io.ergolabs.cardano.explorer.core.db.models.Asset
import io.ergolabs.cardano.explorer.core.db.sql.AssetsQ
import io.ergolabs.cardano.explorer.core.types.TxHash
import tofu.doobie.LiftConnectionIO
import tofu.doobie.log.EmbeddableLogHandler
import tofu.higherKind.derived.representableK
import tofu.logging.Logs
import tofu.syntax.monadic._

@derive(representableK)
trait AssetsR[F[_]] {

  def getByTxId(txId: Long): F[List[Asset]]

  def getByTxHash(txHash: TxHash): F[List[Asset]]
}

object AssetsR {

  def make[I[_]: Functor, D[_]: FlatMap: LiftConnectionIO](implicit
    elh: EmbeddableLogHandler[D],
    logs: Logs[I, D]
  ): I[AssetsR[D]] =
    logs.forService[AssetsR[D]].map { implicit l =>
      elh.embed { implicit lh =>
        new LiveCIO(new AssetsQ).mapK(LiftConnectionIO[D].liftF)
      }
    }

  final class LiveCIO(sql: AssetsQ) extends AssetsR[ConnectionIO] {

    def getByTxId(txId: Long): ConnectionIO[List[Asset]] =
      sql.getByTxId(txId).to[List]

    def getByTxHash(txHash: TxHash): ConnectionIO[List[Asset]] =
      sql.getByTxHash(txHash).to[List]
  }
}
