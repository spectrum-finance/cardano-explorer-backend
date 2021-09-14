package io.ergolabs.cardano.explorer.core.db.repositories

import cats.{FlatMap, Functor}
import derevo.derive
import doobie.ConnectionIO
import cats.tagless.syntax.functorK._
import io.ergolabs.cardano.explorer.core.db.models.SlotLeaderInfo
import io.ergolabs.cardano.explorer.core.db.sql.{RedeemerSql, SlotsSql}
import tofu.doobie.LiftConnectionIO
import tofu.doobie.log.EmbeddableLogHandler
import tofu.higherKind.derived.representableK
import tofu.logging.Logs
import tofu.syntax.monadic._

@derive(representableK)
trait SlotsRepo[F[_]] {

  def getSlotLeaderById(id: BigInt): F[Option[SlotLeaderInfo]]
}

object SlotsRepo {

  def make[I[_]: Functor, D[_]: FlatMap: LiftConnectionIO](implicit
    elh: EmbeddableLogHandler[D],
    logs: Logs[I, D]
  ): I[SlotsRepo[D]] =
    logs.forService[SlotsRepo[D]].map { implicit l =>
      elh.embed(implicit lh => new LiveCIO(new SlotsSql).mapK(LiftConnectionIO[D].liftF))
    }

  final private class LiveCIO(sql: SlotsSql) extends SlotsRepo[ConnectionIO] {

    override def getSlotLeaderById(id: BigInt): ConnectionIO[Option[SlotLeaderInfo]] =
      sql.getSlotLeaderInfoById(id).option
  }
}
