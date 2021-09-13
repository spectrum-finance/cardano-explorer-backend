package io.ergolabs.cardano.explorer.core.db.sql

import cats.data.NonEmptyList
import doobie._
import doobie.syntax.all._
import io.ergolabs.cardano.explorer.core.db.instances._
import io.ergolabs.cardano.explorer.core.db.models.Asset
import io.ergolabs.cardano.explorer.core.types.TxHash

final class AssetsSql(implicit lh: LogHandler) {

  def getByTxId(txId: Long): Query0[Asset] =
    sql"""
         |select
         |  o.id,
         |  t.id,
         |  a.name,
         |  a.quantity,
         |  a.policy,
         |  encode(t.hash, 'hex'),
         |  o.index
         |from ma_tx_out a
         |left join tx_out o on o.id = a.tx_out_id
         |left join tx t on t.id = o.tx_id
         |where t.id = $txId
         |""".stripMargin.query

  def getByTxHash(txHash: TxHash): Query0[Asset] =
    sql"""
         |select
         |  o.id,
         |  t.id,
         |  a.name,
         |  a.quantity,
         |  a.policy,
         |  encode(t.hash, 'hex'),
         |  o.index
         |from ma_tx_out a
         |left join tx_out o on o.id = a.tx_out_id
         |left join tx t on t.id = o.tx_id
         |where t.hash = decode($txHash, 'hex')
         |""".stripMargin.query

  def getByTxIds(txIds: NonEmptyList[Long]): Query0[Asset] = {
    val q =
      sql"""
           |select
           |  o.id,
           |  t.id,
           |  a.name,
           |  a.quantity,
           |  a.policy,
           |  encode(t.hash, 'hex'),
           |  o.index
           |from ma_tx_out a
           |left join tx_out o on o.id = a.tx_out_id
           |left join tx t on t.id = o.tx_id
           |""".stripMargin
    (q ++ Fragments.in(fr"where o.tx_id", txIds)).query[Asset]
  }

  def getByOutputId(outputId: Long): Query0[Asset] =
    sql"""
         |select
         |  o.id,
         |  t.id,
         |  a.name,
         |  a.quantity,
         |  a.policy,
         |  encode(t.hash, 'hex'),
         |  o.index
         |from ma_tx_out a
         |left join tx_out o on o.id = a.tx_out_id
         |left join tx t on t.id = o.tx_id
         |where o.id = $outputId
         |""".stripMargin.query
}
