package io.ergolabs.cardano.explorer.core.db.repositories

import io.ergolabs.cardano.explorer.core.db.models.MetaData
import io.ergolabs.cardano.explorer.core.db.models.EpochParams
import io.ergolabs.cardano.explorer.core.db.models.EpochStakes
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

  def getEpochStakes(epochNo: Int): F[EpochStakes]

  def getCostModel(cost_model_id: Int): F[String]
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

    def getEpochStakes(epochNo: Int): ConnectionIO[EpochStakes] =
      sql.getEpochStakes(epochNo).to[List].map(EpochStakes)

    def getCostModel(cost_model_id: Int): ConnectionIO[String] =
      sql.getCostModel(cost_model_id).unique
  }
}
