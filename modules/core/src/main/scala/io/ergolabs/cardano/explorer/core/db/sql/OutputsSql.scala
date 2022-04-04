package io.ergolabs.cardano.explorer.core.db.sql

import cats.data.NonEmptyList
import doobie._
import doobie.syntax.all._
import doobie.util.fragment.Fragment._
import io.ergolabs.cardano.explorer.core.db.instances._
import io.ergolabs.cardano.explorer.core.db.models.Output
import io.ergolabs.cardano.explorer.core.types.{Addr, AssetRef, OutRef, PaymentCred, TxHash}

final class OutputsSql(implicit lh: LogHandler) {

  def getByOutRef(ref: OutRef): Query0[Output] = {
    val OutRef(txHash, i) = ref
    sql"""
           |select
           |  o.id,
           |  o.tx_id,
           |  encode(b.hash, 'hex'),
           |  encode(t.hash, 'hex'),
           |  o.index,
           |  o.address,
           |  encode(o.address_raw, 'hex'),
           |  o.value,
           |  encode(o.data_hash, 'hex'),
           |  case when (d.value is null) then rd.value else d.value end,
           |  rd.raw_value,
           |  i.id,
           |  encode(ti.hash, 'hex')
           |from tx_out o
           |left join tx t on t.id = o.tx_id
           |left join block b on b.id = t.block_id
           |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
           |left join tx ti on ti.id = i.tx_in_id
           |left join datum d on d.hash = o.data_hash
           |left join reported_datum rd on rd.hash = o.data_hash
           |where t.hash = decode($txHash, 'hex') and o.index = $i
           |""".stripMargin.query
  }

  def getByTxId(txId: Long): Query0[Output] =
    sql"""
         |select
         |  o.id,
         |  o.tx_id,
         |  encode(b.hash, 'hex'),
         |  encode(t.hash, 'hex'),
         |  o.index,
         |  o.address,
         |  encode(o.address_raw, 'hex'),
         |  o.value,
         |  encode(o.data_hash, 'hex'),
         |  case when (d.value is null) then rd.value else d.value end,
         |  rd.raw_value,
         |  i.id,
         |  encode(ti.hash, 'hex')
         |from tx_out o
         |left join tx t on t.id = o.tx_id
         |left join block b on b.id = t.block_id
         |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
         |left join tx ti on ti.id = i.tx_in_id
         |left join datum d on d.hash = o.data_hash
         |left join reported_datum rd on rd.hash = o.data_hash
         |where o.tx_id = $txId
         |""".stripMargin.query

  def getByTxHash(txHash: TxHash): Query0[Output] =
    sql"""
         |select
         |  o.id,
         |  o.tx_id,
         |  encode(b.hash, 'hex'),
         |  encode(t.hash, 'hex'),
         |  o.index,
         |  o.address,
         |  encode(o.address_raw, 'hex'),
         |  o.value,
         |  encode(o.data_hash, 'hex'),
         |  case when (d.value is null) then rd.value else d.value end,
         |  rd.raw_value,
         |  i.id,
         |  encode(ti.hash, 'hex')
         |from tx_out o
         |left join tx t on t.id = o.tx_id
         |left join block b on b.id = t.block_id
         |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
         |left join tx ti on ti.id = i.tx_in_id
         |left join datum d on d.hash = o.data_hash
         |left join reported_datum rd on rd.hash = o.data_hash
         |where t.hash = $txHash
         |""".stripMargin.query

  def getByTxIds(txIds: NonEmptyList[Long]): Query0[Output] = {
    val q =
      sql"""
           |select
           |  o.id,
           |  o.tx_id,
           |  encode(b.hash, 'hex'),
           |  encode(t.hash, 'hex'),
           |  o.index,
           |  o.address,
           |  encode(o.address_raw, 'hex'),
           |  o.value,
           |  encode(o.data_hash, 'hex'),
           |  case when (d.value is null) then rd.value else d.value end,
           |  rd.raw_value,
           |  i.id,
           |  encode(ti.hash, 'hex')
           |from tx_out o
           |left join tx t on t.id = o.tx_id
           |left join block b on b.id = t.block_id
           |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
           |left join tx ti on ti.id = i.tx_in_id
           |left join datum d on d.hash = o.data_hash
           |left join reported_datum rd on rd.hash = o.data_hash
           |""".stripMargin
    (q ++ Fragments.in(fr"where o.tx_id", txIds)).query
  }

  def getUnspent(offset: Int, limit: Int): Query0[Output] =
    sql"""
         |select
         |  o.id,
         |  o.tx_id,
         |  encode(b.hash, 'hex'),
         |  encode(t.hash, 'hex'),
         |  o.index,
         |  o.address,
         |  encode(o.address_raw, 'hex'),
         |  o.value,
         |  encode(o.data_hash, 'hex'),
         |  case when (d.value is null) then rd.value else d.value end,
         |  rd.raw_value,
         |  null,
         |  null
         |from tx_out o
         |left join tx t on t.id = o.tx_id
         |left join block b on b.id = t.block_id
         |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
         |left join datum d on d.hash = o.data_hash
         |left join reported_datum rd on rd.hash = o.data_hash
         |where i.id is null
         |offset $offset limit $limit
         |""".stripMargin.query

  def getUnspentIndexed(minIndex: Int, limit: Int): Query0[Output] =
    sql"""
         |select
         |  o.id,
         |  o.tx_id,
         |  encode(b.hash, 'hex'),
         |  encode(t.hash, 'hex'),
         |  o.index,
         |  o.address,
         |  encode(o.address_raw, 'hex'),
         |  o.value,
         |  encode(o.data_hash, 'hex'),
         |  case when (d.value is null) then rd.value else d.value end,
         |  rd.raw_value,
         |  null,
         |  null
         |from tx_out o
         |left join tx t on t.id = o.tx_id
         |left join block b on b.id = t.block_id
         |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
         |left join datum d on d.hash = o.data_hash
         |left join reported_datum rd on rd.hash = o.data_hash
         |where o.id >= $minIndex and i.id is null
         |limit $limit
         |""".stripMargin.query

  def countUnspent: Query0[Int] =
    sql"""
         |select count(o.id) from tx_out o
         |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
         |where i.id is null
         |""".stripMargin.query

  def getUnspentByAddr(addr: Addr, offset: Int, limit: Int): Query0[Output] =
    sql"""
         |select
         |  o.id,
         |  o.tx_id,
         |  encode(b.hash, 'hex'),
         |  encode(t.hash, 'hex'),
         |  o.index,
         |  o.address,
         |  encode(o.address_raw, 'hex'),
         |  o.value,
         |  encode(o.data_hash, 'hex'),
         |  case when (d.value is null) then rd.value else d.value end,
         |  rd.raw_value,
         |  null,
         |  null
         |from tx_out o
         |left join tx t on t.id = o.tx_id
         |left join block b on b.id = t.block_id
         |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
         |left join datum d on d.hash = o.data_hash
         |left join reported_datum rd on rd.hash = o.data_hash
         |where o.address = $addr and i.id is null
         |offset $offset limit $limit
         |""".stripMargin.query

  def countUnspentByAddr(addr: Addr): Query0[Int] =
    sql"""
         |select count(o.id) from tx_out o
         |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
         |where o.address = $addr and i.id is null
         |""".stripMargin.query

  def getUnspentByPCred(pcred: PaymentCred, offset: Int, limit: Int): Query0[Output] =
    sql"""
         |select
         |  o.id,
         |  o.tx_id,
         |  encode(b.hash, 'hex'),
         |  encode(t.hash, 'hex'),
         |  o.index,
         |  o.address,
         |  encode(o.address_raw, 'hex'),
         |  o.value,
         |  encode(o.data_hash, 'hex'),
         |  case when (d.value is null) then rd.value else d.value end,
         |  rd.raw_value,
         |  null,
         |  null
         |from tx_out o
         |left join tx t on t.id = o.tx_id
         |left join block b on b.id = t.block_id
         |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
         |left join datum d on d.hash = o.data_hash
         |left join reported_datum rd on rd.hash = o.data_hash
         |where o.payment_cred = decode($pcred, 'hex') and i.id is null
         |offset $offset limit $limit
         |""".stripMargin.query

  def countUnspentByPCred(pcred: PaymentCred): Query0[Int] =
    sql"""
         |select count(o.id) from tx_out o
         |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
         |where o.payment_cred = decode($pcred, 'hex') and i.id is null
         |""".stripMargin.query

  def getUnspentByAsset(asset: AssetRef, offset: Int, limit: Int): Query0[Output] =
    sql"""
         |select distinct on (o.id)
         |  o.id,
         |  o.tx_id,
         |  encode(b.hash, 'hex'),
         |  encode(t.hash, 'hex'),
         |  o.index,
         |  o.address,
         |  encode(o.address_raw, 'hex'),
         |  o.value,
         |  encode(o.data_hash, 'hex'),
         |  case when (d.value is null) then rd.value else d.value end,
         |  rd.raw_value,
         |  null,
         |  null
         |from tx_out o
         |left join tx t on t.id = o.tx_id
         |left join block b on b.id = t.block_id
         |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
         |left join ma_tx_out a on a.tx_out_id = o.id
         |left join datum d on d.hash = o.data_hash
         |left join reported_datum rd on rd.hash = o.data_hash
         |where a.policy = ${asset.policyId.value} and a.name = ${asset.name.value} and i.id is null
         |offset $offset limit $limit
         |""".stripMargin.query

  def countUnspentByAsset(asset: AssetRef): Query0[Int] =
    sql"""
         |select count(distinct o.id)
         |from tx_out o
         |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
         |left join ma_tx_out a on a.tx_out_id = o.id
         |where a.policy = ${asset.policyId.value} and a.name = ${asset.name.value} and i.id is null
         |""".stripMargin.query

  def searchUnspent(
    addr: Addr,
    containsAllOf: Option[List[AssetRef]],
    containsAnyOf: Option[List[AssetRef]],
    offset: Int,
    limit: Int
  ): Query0[Output] =
    const(s"""
      |select distinct on (o.id)
      |  o.id,
      |  o.tx_id,
      |  encode(b.hash, 'hex'),
      |  encode(t.hash, 'hex'),
      |  o.index,
      |  o.address,
      |  encode(o.address_raw, 'hex'),
      |  o.value,
      |  encode(o.data_hash, 'hex'),
      |  case when (d.value is null) then rd.value else d.value end,
      |  rd.raw_value,
      |  null,
      |  null
      |from tx_out o
      |left join tx t on t.id = o.tx_id
      |left join block b on b.id = t.block_id
      |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
      |left join ma_tx_out a on a.tx_out_id = o.id
      |left join datum d on d.hash = o.data_hash
      |left join reported_datum rd on rd.hash = o.data_hash
      |${containsAllOf.map(innerJoinAllOfAssets("au", "o", _)).getOrElse("")}
      |where
      |  i.id is null and
      |  o.address = '$addr' and
      |  ${containsAnyOf.map(as => s"a.policy in (${as.map(s => s"'${s.policyId}'").mkString(", ")}) and").getOrElse("")}
      |  ${containsAnyOf.map(as => s"a.name in (${as.map(s => s"'${s.name}'").mkString(", ")}) and").getOrElse("")}
      |offset $offset limit $limit
      |""".stripMargin).query

  def countUnspent(
    addr: Addr,
    containsAllOf: Option[List[AssetRef]],
    containsAnyOf: Option[List[AssetRef]]
  ): Query0[Int] =
    const(s"""
             |select count(distinct o.id)
             |from tx_out o
             |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
             |left join ma_tx_out a on a.tx_out_id = o.id
             |${containsAllOf.map(innerJoinAllOfAssets("au", "o", _)).getOrElse("")}
             |where
             |  i.id is null and
             |  o.address = '$addr' and
             |  ${containsAnyOf.map(as => s"a.policy in (${as.map(s => s"'${s.policyId}'").mkString(", ")}) and").getOrElse("")}
             |  ${containsAnyOf.map(as => s"a.name in (${as.map(s => s"'${s.name}'").mkString(", ")}) and").getOrElse("")}
             |""".stripMargin).query

  private def innerJoinAllOfAssets(
    as: String,
    tableAlias: String,
    containsAllOf: List[AssetRef]
  ): String =
    s"""
       |inner join (
       |  select a0.id from ma_tx_out a0
       |  ${containsAllOf.zipWithIndex.tail
      .map { case (_, ix) => s"inner join ma_tx_out a$ix on a0.tx_out_id = a$ix.tx_out_id" }
      .mkString("\n")}
       |  where ${containsAllOf.zipWithIndex
      .map { case (a, ix) => s"a$ix.policy = '${a.policyId}' and a$ix.name = '${a.name}'" }
      .mkString(" and ")}
       |) as $as on $as.tx_out_id = $tableAlias.id
       |""".stripMargin
}
