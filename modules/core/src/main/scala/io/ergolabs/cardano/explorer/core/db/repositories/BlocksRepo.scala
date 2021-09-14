package io.ergolabs.cardano.explorer.core.db.repositories

import cats.{FlatMap, Functor}
import derevo.derive
import doobie.ConnectionIO
import cats.tagless.syntax.functorK._
import io.ergolabs.cardano.explorer.core.db.models.BlockHeader
import io.ergolabs.cardano.explorer.core.db.sql.BlocksSql
import tofu.doobie.LiftConnectionIO
import tofu.doobie.log.EmbeddableLogHandler
import tofu.higherKind.derived.representableK
import tofu.logging.Logs
import tofu.syntax.monadic._

@derive(representableK)
trait BlocksRepo[F[_]] {

  def getBestBlock: F[BlockHeader]
}

object BlocksRepo {

  def make[I[_]: Functor, D[_]: FlatMap: LiftConnectionIO](implicit
    elh: EmbeddableLogHandler[D],
    logs: Logs[I, D]
  ): I[BlocksRepo[D]] =
    logs.forService[BlocksRepo[D]].map { implicit l =>
      elh.embed { implicit lh =>
        new LiveCIO(new BlocksSql).mapK(LiftConnectionIO[D].liftF)
      }
    }

  final class LiveCIO(sql: BlocksSql) extends BlocksRepo[ConnectionIO] {
    override def getBestBlock: ConnectionIO[BlockHeader] = sql.getBestBlockHeader.unique
  }
}
