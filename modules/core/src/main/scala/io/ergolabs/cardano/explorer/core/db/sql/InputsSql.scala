package io.ergolabs.cardano.explorer.core.db.sql

import doobie._
import doobie.syntax.all._
import io.ergolabs.cardano.explorer.core.db.models.Input
import io.ergolabs.cardano.explorer.core.db.instances._
import io.ergolabs.cardano.explorer.core.types.TxHash

final class InputsSql(implicit lh: LogHandler) {

  def getByTxId(txId: Long): Query0[Input] =
    sql"""
         |select
         |  b.hash,
         |  t.hash,
         |  ot.hash,
         |  o.tx_out_index,
         |  o.value
         |from tx_in i
         |left join tx t on t.id = i.tx_in_id
         |left join block b on b.id = t.block_id
         |left join tx ot on ot.id = i.tx_out_id
         |where i.tx_in_id = $txId
         |""".stripMargin.query[Input]

  def getByTxHash(txHash: TxHash): Query0[Input] =
    sql"""
         |select
         |  b.hash,
         |  t.hash,
         |  ot.hash,
         |  o.tx_out_index,
         |  o.value
         |from tx_in i
         |left join tx t on t.id = i.tx_in_id
         |left join block b on b.id = t.block_id
         |left join tx ot on ot.id = i.tx_out_id
         |where t.hash = $txHash
         |""".stripMargin.query[Input]
}
