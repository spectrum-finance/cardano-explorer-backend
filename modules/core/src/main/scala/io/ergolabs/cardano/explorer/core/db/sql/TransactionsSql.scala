package io.ergolabs.cardano.explorer.core.db.sql

import doobie._
import doobie.syntax.all._
import doobie.util.fragment.Fragment.const
import io.ergolabs.cardano.explorer.core.db.instances._
import io.ergolabs.cardano.explorer.core.db.models.Transaction
import io.ergolabs.cardano.explorer.core.models.Sorting.SortOrder
import io.ergolabs.cardano.explorer.core.types.{Addr, TxHash}

class TransactionsSql(implicit lh: LogHandler) {

  def getByTxHash(txHash: TxHash): Query0[Transaction] =
    sql"""
         |select
         |  t.id,
         |  encode(b.hash, 'hex'),
         |  t.block_index,
         |  encode(t.hash, 'hex'),
         |  t.invalid_before,
         |  t.invalid_hereafter,
         |  t.size
         |from tx t
         |left join block b on b.id = t.block_id
         |where t.hash = decode($txHash, 'hex')
         |""".stripMargin.query

  def getAll(offset: Int, limit: Int, order: SortOrder): Query0[Transaction] = {
    val q =
      sql"""
           |select
           |  t.id,
           |  encode(b.hash, 'hex'),
           |  t.block_index,
           |  encode(t.hash, 'hex'),
           |  t.invalid_before,
           |  t.invalid_hereafter,
           |  t.size
           |from tx t
           |left join block b on b.id = t.block_id
           |""".stripMargin
    (q ++ const(s"order by t.id ${order.value}") ++ const(s"offset $offset limit $limit")).query
  }

  def countAll: Query0[Int] =
    sql"select count(*) from tx".query[Int]

  def getByBlock(blockHeight: Int): Query0[Transaction] =
    sql"""
         |select
         |  t.id,
         |  encode(b.hash, 'hex'),
         |  t.block_index,
         |  encode(t.hash, 'hex'),
         |  t.invalid_before,
         |  t.invalid_hereafter,
         |  t.size
         |from tx t
         |left join block b on b.id = t.block_id
         |where t.block_id = $blockHeight
         |""".stripMargin.query

  def countByBlock(blockHeight: Int): Query0[Int] =
    sql"select count(*) from tx t where t.block_id = $blockHeight".query

  def getByAddress(addr: Addr, offset: Int, limit: Int): Query0[Transaction] =
    sql"""
         |select
         |  t.id,
         |  encode(b.hash, 'hex'),
         |  t.block_index,
         |  encode(t.hash, 'hex'),
         |  t.invalid_before,
         |  t.invalid_hereafter,
         |  t.size
         |from tx t
         |left join block b on b.id = t.block_id
         |left join tx_out o on o.tx_id = t.id
         |left join tx_in i on i.tx_in_id = t.id
         |left join tx_out io on io.tx_id = i.tx_out_id and io.index = i.tx_out_index
         |where o.address = $addr or io.address = $addr
         |offset $offset limit $limit
         |""".stripMargin.query

  def countByAddress(addr: Addr): Query0[Int] =
    sql"""
         |select count(distinct t.id)
         |from tx t
         |left join block b on b.id = t.block_id
         |left join tx_out o on o.tx_id = t.id
         |left join tx_in i on i.tx_in_id = t.id
         |left join tx_out io on io.tx_id = i.tx_out_id and io.index = i.tx_out_index
         |where o.address = $addr or io.address = $addr
         |""".stripMargin.query
}
