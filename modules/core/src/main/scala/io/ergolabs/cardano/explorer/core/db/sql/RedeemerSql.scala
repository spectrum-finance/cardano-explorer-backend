package io.ergolabs.cardano.explorer.core.db.sql

import cats.data.NonEmptyList
import doobie._
import doobie.syntax.all._
import io.ergolabs.cardano.explorer.core.db.instances._
import io.ergolabs.cardano.explorer.core.db.models.Redeemer

final class RedeemerSql(implicit lh: LogHandler) {

  def getByTxId(txId: Long): Query0[Redeemer] =
    sql"""
         |select
         |  r.id,
         |  r.tx_id,
         |  r.unit_mem,
         |  r.unit_steps,
         |  r.fee,
         |  r.purpose,
         |  r.index,
         |  encode(r.script_hash, 'hex'),
         |  case when (d.value is null) then rd.value else d.value end,
         |  encode(rd.raw_value, 'hex')
         |from redeemer r
         |left join datum d on d.id = r.datum_id
         |left join reported_datum rd on rd.hash = d.hash
         |where r.tx_id = $txId
         |""".stripMargin.query

  def getByTxIds(txIds: NonEmptyList[Long]): Query0[Redeemer] = {
    val q =
      sql"""
           |select
           |  r.id,
           |  r.tx_id,
           |  r.unit_mem,
           |  r.unit_steps,
           |  r.fee,
           |  r.purpose,
           |  r.index,
           |  encode(r.script_hash, 'hex'),
           |  case when (d.value is null) then rd.value else d.value end,
           |  encode(rd.raw_value, 'hex')
           |left join datum d on d.id = r.datum_id
           |left join reported_datum rd on rd.hash = d.hash
           |from redeemer r
           |""".stripMargin
    (q ++ Fragments.in(fr"where r.tx_id", txIds)).query
  }
}
