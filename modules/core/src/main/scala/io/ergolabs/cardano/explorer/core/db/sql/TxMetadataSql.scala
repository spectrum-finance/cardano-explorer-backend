package io.ergolabs.cardano.explorer.core.db.sql

import cats.data.NonEmptyList
import doobie._
import doobie.syntax.all._
import io.ergolabs.cardano.explorer.core.db.instances._
import io.ergolabs.cardano.explorer.core.db.models.TxMetadata

class TxMetadataSql(implicit lh: LogHandler) {

  def getByTxId(txId: Long): Query0[TxMetadata] =
    sql"""
         |select
         |  m.tx_Id,
         |  m.key,
         |  encode(m.bytes, 'hex'),
         |  m.json
         |from tx_metadata m
         |where m.tx_Id = $txId
         |""".stripMargin.query

  def getByTxIds(txIds: NonEmptyList[Long]): Query0[TxMetadata] = {
    val q =
      sql"""
           |select
           |  m.tx_Id,
           |  m.key,
           |  encode(m.bytes, 'hex'),
           |  m.json
           |from tx_metadata m
           |""".stripMargin
    (q ++ Fragments.in(fr"where m.tx_Id", txIds)).query
  }
}
