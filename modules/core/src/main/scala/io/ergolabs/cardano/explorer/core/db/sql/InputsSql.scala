package io.ergolabs.cardano.explorer.core.db.sql

import cats.data.NonEmptyList
import doobie._
import doobie.syntax.all._
import io.ergolabs.cardano.explorer.core.db.instances._
import io.ergolabs.cardano.explorer.core.db.models.Input
import io.ergolabs.cardano.explorer.core.types.TxHash

final class InputsSql(implicit lh: LogHandler) {

  def getByTxId(txId: Long): Query0[Input] =
    sql"""
         |select
         |  i.tx_in_id,
         |  i.redeemer_id,
         |  encode(b.hash, 'hex'),
         |  encode(t.hash, 'hex'),
         |  o.id,
         |  o.tx_id,
         |  encode(b.hash, 'hex'),
         |  encode(ot.hash, 'hex'),
         |  o.index,
         |  o.address,
         |  encode(o.address_raw, 'hex'),
         |  encode(o.payment_cred, 'hex'),
         |  o.value,
         |  encode(o.data_hash, 'hex'),
         |  d.value,
         |  encode(d.bytes, 'hex'),
         |  i.id,
         |  encode(t.hash, 'hex')
         |from tx_in i
         |left join tx t on t.id = i.tx_in_id
         |left join block b on b.id = t.block_id
         |left join tx ot on ot.id = i.tx_out_id
         |left join tx_out o on o.tx_id = i.tx_out_id and o.index = i.tx_out_index
         |left join redeemer r on r.id = i.redeemer_id
         |left join datum d on d.hash = o.data_hash
         |where i.tx_in_id = $txId
         |""".stripMargin.query

  def getByTxHash(txHash: TxHash): Query0[Input] =
    sql"""
         |select
         |  i.tx_in_id,
         |  i.redeemer_id,
         |  encode(b.hash, 'hex'),
         |  encode(t.hash, 'hex'),
         |  o.id,
         |  o.tx_id,
         |  encode(b.hash, 'hex'),
         |  encode(ot.hash, 'hex'),
         |  o.index,
         |  o.address,
         |  encode(o.address_raw, 'hex'),
         |  encode(o.payment_cred, 'hex'),
         |  o.value,
         |  encode(o.data_hash, 'hex'),
         |  d.value,
         |  encode(d.bytes, 'hex'),
         |  i.id,
         |  encode(t.hash, 'hex')
         |from tx_in i
         |left join tx t on t.id = i.tx_in_id
         |left join block b on b.id = t.block_id
         |left join tx ot on ot.id = i.tx_out_id
         |left join tx_out o on o.tx_id = i.tx_out_id and o.index = i.tx_out_index
         |left join datum d on d.hash = o.data_hash
         |where t.hash = $txHash
         |""".stripMargin.query[Input]

  def getByTxIds(txIds: NonEmptyList[Long]): Query0[Input] = {
    val q =
      sql"""
           |select
           |  i.tx_in_id,
           |  i.redeemer_id,
           |  encode(b.hash, 'hex'),
           |  encode(t.hash, 'hex'),
           |  o.id,
           |  o.tx_id,
           |  encode(b.hash, 'hex'),
           |  encode(ot.hash, 'hex'),
           |  o.index,
           |  o.address,
           |  encode(o.address_raw, 'hex'),
           |  encode(o.payment_cred, 'hex'),
           |  o.value,
           |  encode(o.data_hash, 'hex'),
           |  d.value,
           |  encode(d.bytes, 'hex'),
           |  i.id,
           |  encode(t.hash, 'hex')
           |from tx_in i
           |left join tx t on t.id = i.tx_in_id
           |left join block b on b.id = t.block_id
           |left join tx ot on ot.id = i.tx_out_id
           |left join tx_out o on o.tx_id = i.tx_out_id and o.index = i.tx_out_index
           |left join datum d on d.hash = o.data_hash
           |""".stripMargin
    (q ++ Fragments.in(fr"where i.tx_in_id", txIds)).query[Input]
  }
}
