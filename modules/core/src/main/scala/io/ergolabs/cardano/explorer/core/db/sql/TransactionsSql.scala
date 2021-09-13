package io.ergolabs.cardano.explorer.core.db.sql

import doobie._
import doobie.syntax.all._
import io.ergolabs.cardano.explorer.core.db.models.Transaction
import io.ergolabs.cardano.explorer.core.types.TxHash

class TransactionsSql(implicit lh: LogHandler) {

  def getByTxHash(txHash: TxHash): Query0[Transaction] =
    sql"""
         |select
         |  b.hash,
         |  t.block_index,
         |  t.hash,
         |  t.size
         |from tx t
         |left join block b on b.id = t.block_id
         |where t.hash = $txHash
         |""".stripMargin.query
}
