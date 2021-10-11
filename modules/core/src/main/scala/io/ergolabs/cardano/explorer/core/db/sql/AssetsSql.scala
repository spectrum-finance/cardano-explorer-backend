package io.ergolabs.cardano.explorer.core.db.sql

import cats.data.NonEmptyList
import doobie._
import doobie.syntax.all._
import io.ergolabs.cardano.explorer.core.db.instances._
import io.ergolabs.cardano.explorer.core.db.models.{AssetMintEvent, AssetOutput}
import io.ergolabs.cardano.explorer.core.types.{Asset32, AssetRef, TxHash}

final class AssetsSql(implicit lh: LogHandler) {

  def getByTxId(txId: Long): Query0[AssetOutput] =
    sql"""
         |select
         |  o.id,
         |  o.tx_id,
         |  encode(a.name, 'escape'),
         |  a.quantity,
         |  encode(a.policy, 'hex'),
         |  o.index
         |from ma_tx_out a
         |left join tx_out o on o.id = a.tx_out_id
         |where o.tx_id = $txId
         |""".stripMargin.query

  def getByTxHash(txHash: TxHash): Query0[AssetOutput] =
    sql"""
         |select
         |  o.id,
         |  o.tx_id,
         |  encode(a.name, 'escape'),
         |  a.quantity,
         |  encode(a.policy, 'hex'),
         |  o.index
         |from ma_tx_out a
         |left join tx_out o on o.id = a.tx_out_id
         |left join tx t on t.id = o.tx_id
         |where t.hash = decode($txHash, 'escape')
         |""".stripMargin.query

  def getByTxIds(txIds: NonEmptyList[Long]): Query0[AssetOutput] = {
    val q =
      sql"""
           |select
           |  o.id,
           |  o.tx_id,
           |  encode(a.name, 'escape'),
           |  a.quantity,
           |  encode(a.policy, 'hex'),
           |  o.index
           |from ma_tx_out a
           |left join tx_out o on o.id = a.tx_out_id
           |""".stripMargin
    (q ++ Fragments.in(fr"where o.tx_id", txIds)).query[AssetOutput]
  }

  def getByOutputId(outputId: Long): Query0[AssetOutput] =
    sql"""
         |select
         |  o.id,
         |  o.tx_id,
         |  encode(a.name, 'escape'),
         |  a.quantity,
         |  encode(a.policy, 'hex'),
         |  o.index
         |from ma_tx_out a
         |left join tx_out o on o.id = a.tx_out_id
         |where o.id = $outputId
         |""".stripMargin.query

  def getByOutputIds(outputIds: NonEmptyList[Long]): Query0[AssetOutput] = {
    val q =
      sql"""
           |select
           |  o.id,
           |  o.tx_id,
           |  encode(a.name, 'escape'),
           |  a.quantity,
           |  encode(a.policy, 'hex'),
           |  o.index
           |from ma_tx_out a
           |left join tx_out o on o.id = a.tx_out_id
           |""".stripMargin
    (q ++ Fragments.in(fr"where a.tx_out_id", outputIds)).query[AssetOutput]
  }

  def getMintEventsByAsset(ref: AssetRef): Query0[AssetMintEvent] =
    sql"""
         |select
         |  a.id,
         |  encode(a.policy, 'hex'),
         |  encode(a.name, 'escape'),
         |  a.quantity,
         |  a.tx_id
         |from ma_tx_mint a
         |where a.policy = ${ref.policyId} and
         |      a.name = decode(${ref.name}, 'escape')
         |""".stripMargin.query
}
