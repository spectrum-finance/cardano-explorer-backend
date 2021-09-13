package io.ergolabs.cardano.explorer.core.db.sql

import cats.data.NonEmptyList
import doobie._
import doobie.syntax.all._
import io.ergolabs.cardano.explorer.core.db.instances._
import io.ergolabs.cardano.explorer.core.db.models.Output
import io.ergolabs.cardano.explorer.core.types.{OutRef, TxHash}

final class OutputsSql(implicit lh: LogHandler) {

  def getByOutRef(ref: OutRef): Query0[Output] = {
    val OutRef(txHash, i) = ref
    sql"""
           |select
           |  o.id,
           |  o.tx_id,
           |  encode(b.hash, 'hex'),
           |  encode(t.hash, 'hex'),
           |  o.index,
           |  o.address,
           |  o.value,
           |  o.data_hash,
           |  i.id,
           |  encode(ti.hash, 'hex')
           |from tx_out o
           |left join tx t on t.id = o.tx_id
           |left join block b on b.id = t.block_id
           |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
           |left join tx ti on ti.id = i.tx_in_id
           |where t.hash = decode($txHash, 'hex') and o.index = $i
           |""".stripMargin.query
  }

  def getByTxId(txId: Long): Query0[Output] =
    sql"""
         |select
         |  o.id,
         |  o.tx_id,
         |  encode(b.hash, 'hex'),
         |  encode(t.hash, 'hex'),
         |  o.index,
         |  o.address,
         |  o.value,
         |  o.data_hash,
         |  i.id,
         |  encode(ti.hash, 'hex')
         |from tx_out o
         |left join tx t on t.id = o.tx_id
         |left join block b on b.id = t.block_id
         |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
         |left join tx ti on ti.id = i.tx_in_id
         |where o.tx_id = $txId
         |""".stripMargin.query

  def getByTxHash(txHash: TxHash): Query0[Output] =
    sql"""
         |select
         |  o.id,
         |  o.tx_id,
         |  encode(b.hash, 'hex'),
         |  encode(t.hash, 'hex'),
         |  o.index,
         |  o.address,
         |  o.value,
         |  o.data_hash,
         |  i.id,
         |  encode(ti.hash, 'hex')
         |from tx_out o
         |left join tx t on t.id = o.tx_id
         |left join block b on b.id = t.block_id
         |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
         |left join tx ti on ti.id = i.tx_in_id
         |where t.hash = $txHash
         |""".stripMargin.query

  def getByTxIds(txIds: NonEmptyList[Long]): Query0[Output] = {
    val q =
      sql"""
           |select
           |  o.id,
           |  o.tx_id,
           |  encode(b.hash, 'hex'),
           |  encode(t.hash, 'hex'),
           |  o.index,
           |  o.address,
           |  o.value,
           |  o.data_hash,
           |  i.id,
           |  encode(ti.hash, 'hex')
           |from tx_out o
           |left join tx t on t.id = o.tx_id
           |left join block b on b.id = t.block_id
           |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
           |left join tx ti on ti.id = i.tx_in_id
           |""".stripMargin
    (q ++ Fragments.in(fr"where o.tx_id", txIds)).query
  }
}
