package io.ergolabs.cardano.explorer.core.db.sql

import doobie.Query0
import doobie._
import doobie.syntax.all._
import io.ergolabs.cardano.explorer.core.db.instances._
import doobie.implicits.javasql._
import doobie.postgres.implicits._
import io.ergolabs.cardano.explorer.core.db.models.{Asset, BlockHeader}

final class BlocksSql(implicit lh: LogHandler) {

  def getBestBlockHeader: Query0[BlockHeader] =
    sql"""
         |select
         |  id,
         |  encode(hash, 'hex'),
         |  epoch_no,
         |  slot_no,
         |  slot_leader_id,
         |  tx_count,
         |  time
         |from block
         |where id = (SELECT MAX(id) FROM block)
         |""".stripMargin.query
}
