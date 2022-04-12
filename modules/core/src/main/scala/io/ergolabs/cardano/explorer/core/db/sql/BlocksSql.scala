package io.ergolabs.cardano.explorer.core.db.sql

import doobie._
import doobie.syntax.all._
import io.ergolabs.cardano.explorer.core.db.instances._
import doobie.implicits.javasql._
import doobie.postgres.implicits._
import io.ergolabs.cardano.explorer.core.db.models.BlockHeader

final class BlocksSql(implicit lh: LogHandler) {

  def getBestBlockHeader: Query0[BlockHeader] =
    sql"""
         |select
         |  id,
         |  encode(hash, 'hex'),
         |  block_no,
         |  epoch_no,
         |  slot_no,
         |  slot_leader_id,
         |  tx_count,
         |  time
         |from block
         |where id = (select max(id) from block)
         |""".stripMargin.query
}
