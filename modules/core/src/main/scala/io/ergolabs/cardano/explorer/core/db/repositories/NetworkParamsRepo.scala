package io.ergolabs.cardano.explorer.core.db.repositories

import io.ergolabs.cardano.explorer.core.db.models.MetaData
import io.ergolabs.cardano.explorer.core.db.models.EpochParams
import cats.tagless.syntax.functorK._
import tofu.higherKind.derived.representableK
import tofu.logging.Logs
import tofu.syntax.monadic._
import derevo.derive
import cats.{FlatMap, Functor}
import tofu.doobie.LiftConnectionIO
import tofu.doobie.log.EmbeddableLogHandler
import io.ergolabs.cardano.explorer.core.db.sql.NetworkParamsSql
import doobie.ConnectionIO

@derive(representableK)
trait NetworkParamsRepo[F[_]] {

  def getMeta: F[MetaData]

  def getLastEpochParams: F[EpochParams]

  def getCostModel(costModelId: Int): F[String]
}

object NetworkParamsRepo {

  def make[I[_]: Functor, D[_]: FlatMap: LiftConnectionIO](implicit
    elh: EmbeddableLogHandler[D],
    logs: Logs[I, D]
  ): I[NetworkParamsRepo[D]] =
    logs.forService[NetworkParamsRepo[D]].map { implicit l =>
      elh.embed { implicit lh =>
        new LiveCIO(new NetworkParamsSql).mapK(LiftConnectionIO[D].liftF)
      }
    }

  final class LiveCIO(sql: NetworkParamsSql) extends NetworkParamsRepo[ConnectionIO] {

    def getMeta: ConnectionIO[MetaData] =
      sql.getMeta.unique

    def getLastEpochParams: ConnectionIO[EpochParams] =
      sql.getLastEpochParams.unique

    def getCostModel(costModelId: Int): ConnectionIO[String] =
      sql.getCostModel(costModelId).unique
  }
}
