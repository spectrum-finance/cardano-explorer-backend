package io.ergolabs.cardano.explorer.core.db.sql

import doobie._
import doobie.syntax.all._
import io.ergolabs.cardano.explorer.core.db.instances._
import io.ergolabs.cardano.explorer.core.db.models.Output
import io.ergolabs.cardano.explorer.core.types.TxHash

final class OutputsSql(implicit lh: LogHandler) {

  def getByTxId(txId: Long): Query0[Output] =
    sql"""
         |select
         |  b.hash,
         |  t.hash,
         |  o.index,
         |  o.address,
         |  o.value,
         |  o.data_hash
         |from tx_out o
         |left join tx t on t.id = o.tx_id
         |left join block b on b.id = t.block_id
         |where o.tx_id = $txId
         |""".stripMargin.query[Output]

  def getByTxHash(txHash: TxHash): Query0[Output] =
    sql"""
         |select
         |  b.hash,
         |  t.hash,
         |  o.index,
         |  o.address,
         |  o.value,
         |  o.data_hash
         |from tx_out o
         |left join tx t on t.id = o.tx_id
         |left join block b on b.id = t.block_id
         |where t.hash = $txHash
         |""".stripMargin.query[Output]
}
