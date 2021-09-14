package io.ergolabs.cardano.explorer.core.db.sql

import doobie.LogHandler
import doobie._
import doobie.syntax.all._
import io.ergolabs.cardano.explorer.core.db.instances._
import io.ergolabs.cardano.explorer.core.db.models.SlotLeaderInfo

final class SlotsSql(implicit lh: LogHandler) {

  def getSlotLeaderInfoById(id: BigInt): Query0[SlotLeaderInfo] =
    sql"""
         |select
         |  id,
         |  encode(hash, 'hex'),
         |  pool_hash_id,
         |  description
         |from slot_leader
         |where id = $id
         |""".stripMargin.query
}
