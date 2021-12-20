package io.ergolabs.cardano.explorer.core.db.sql

import cats.data.NonEmptyList
import doobie._
import doobie.syntax.all._
import io.ergolabs.cardano.explorer.core.db.instances._
import io.ergolabs.cardano.explorer.core.db.models.{AssetInput, AssetMintEvent, AssetOutput}
import io.ergolabs.cardano.explorer.core.types.{Asset32, AssetRef, TxHash}

final class AssetsSql(implicit lh: LogHandler) {

  def getByOutTxId(txId: Long): Query0[AssetOutput] =
    sql"""
         |select
         |  o.id,
         |  o.tx_id,
         |  encode(ma.name, 'escape'),
         |  a.quantity,
         |  encode(ma.policy, 'hex')
         |from ma_tx_out a
         |left join multi_asset ma on ma.id = a.ident
         |left join tx_out o on o.id = a.tx_out_id
         |where o.tx_id = $txId
         |""".stripMargin.query

  def getByInTxId(txId: Long): Query0[AssetInput] =
    sql"""
         |select
         |  o.id,
         |  i.tx_in_id,
         |  encode(ma.name, 'escape'),
         |  a.quantity,
         |  encode(ma.policy, 'hex')
         |from ma_tx_out a
         |left join multi_asset ma on ma.id = a.ident
         |left join tx_out o on o.id = a.tx_out_id
         |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
         |where i.tx_in_id = $txId
         |""".stripMargin.query

  def getByOutTxHash(txHash: TxHash): Query0[AssetOutput] =
    sql"""
         |select
         |  o.id,
         |  o.tx_id,
         |  encode(ma.name, 'escape'),
         |  a.quantity,
         |  encode(ma.policy, 'hex')
         |from ma_tx_out a
         |left join multi_asset ma on ma.id = a.ident
         |left join tx_out o on o.id = a.tx_out_id
         |left join tx t on t.id = o.tx_id
         |where t.hash = decode($txHash, 'escape')
         |""".stripMargin.query

  def getByOutTxIds(txIds: NonEmptyList[Long]): Query0[AssetOutput] = {
    val q =
      sql"""
           |select
           |  o.id,
           |  o.tx_id,
           |  encode(ma.name, 'escape'),
           |  a.quantity,
           |  encode(ma.policy, 'hex')
           |from ma_tx_out a
           |left join multi_asset ma on ma.id = a.ident
           |left join tx_out o on o.id = a.tx_out_id
           |""".stripMargin
    (q ++ Fragments.in(fr"where o.tx_id", txIds)).query[AssetOutput]
  }

  def getByInTxIds(txIds: NonEmptyList[Long]): Query0[AssetInput] = {
    val q =
      sql"""
           |select
           |  o.id,
           |  i.tx_in_id,
           |  encode(ma.name, 'escape'),
           |  a.quantity,
           |  encode(ma.policy, 'hex')
           |from ma_tx_out a
           |left join multi_asset ma on ma.id = a.ident
           |left join tx_out o on o.id = a.tx_out_id
           |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
           |""".stripMargin
    (q ++ Fragments.in(fr"where i.tx_in_id", txIds)).query[AssetInput]
  }

  def getByOutputId(outputId: Long): Query0[AssetOutput] =
    sql"""
         |select
         |  o.id,
         |  o.tx_id,
         |  encode(ma.name, 'escape'),
         |  a.quantity,
         |  encode(ma.policy, 'hex')
         |from ma_tx_out a
         |left join multi_asset ma on ma.id = a.ident
         |left join tx_out o on o.id = a.tx_out_id
         |where o.id = $outputId
         |""".stripMargin.query

  def getByOutputIds(outputIds: NonEmptyList[Long]): Query0[AssetOutput] = {
    val q =
      sql"""
           |select
           |  o.id,
           |  o.tx_id,
           |  encode(ma.name, 'escape'),
           |  a.quantity,
           |  encode(ma.policy, 'hex')
           |from ma_tx_out a
           |left join multi_asset ma on ma.id = a.ident
           |left join tx_out o on o.id = a.tx_out_id
           |""".stripMargin
    (q ++ Fragments.in(fr"where a.tx_out_id", outputIds)).query[AssetOutput]
  }

  def getMintEventsByAsset(ref: AssetRef): Query0[AssetMintEvent] =
    sql"""
         |select
         |  a.id,
         |  encode(ma.policy, 'hex'),
         |  encode(ma.name, 'escape'),
         |  a.quantity,
         |  a.tx_id
         |from ma_tx_mint a
         |left join multi_asset ma on ma.id = a.ident
         |where ma.policy = ${ref.policyId} and
         |      ma.name = decode(${ref.name}, 'escape')
         |""".stripMargin.query
}
