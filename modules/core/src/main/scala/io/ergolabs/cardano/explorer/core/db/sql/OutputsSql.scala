package io.ergolabs.cardano.explorer.core.db.sql

import cats.data.NonEmptyList
import doobie._
import doobie.syntax.all._
import doobie.util.fragment.Fragment._
import io.ergolabs.cardano.explorer.core.db.instances._
import io.ergolabs.cardano.explorer.core.db.models.Output
import io.ergolabs.cardano.explorer.core.models.Sorting.SortOrder
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
           |  encode(o.payment_cred, 'hex'),
           |  o.value,
           |  encode(o.data_hash, 'hex'),
           |  d.value,
           |  encode(d.bytes, 'hex'),
           |  i.id,
           |  encode(ti.hash, 'hex'),
           |  encode(s.hash, 'hex')
           |from tx_out o
           |left join tx t on t.id = o.tx_id
           |left join block b on b.id = t.block_id
           |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
           |left join tx ti on ti.id = i.tx_in_id
           |left join datum d on d.hash = o.data_hash
           |left join script s on s.id = o.reference_script_id
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
         |  encode(o.payment_cred, 'hex'),
         |  o.value,
         |  encode(o.data_hash, 'hex'),
         |  d.value,
         |  encode(d.bytes, 'hex'),
         |  i.id,
         |  encode(ti.hash, 'hex'),
         |  encode(s.hash, 'hex')
         |from tx_out o
         |left join tx t on t.id = o.tx_id
         |left join block b on b.id = t.block_id
         |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
         |left join tx ti on ti.id = i.tx_in_id
         |left join datum d on d.hash = o.data_hash
         |left join script s on s.id = o.reference_script_id
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
         |  encode(o.payment_cred, 'hex'),
         |  o.value,
         |  encode(o.data_hash, 'hex'),
         |  d.value,
         |  encode(d.bytes, 'hex'),
         |  i.id,
         |  encode(ti.hash, 'hex'),
         |  encode(s.hash, 'hex')
         |from tx_out o
         |left join tx t on t.id = o.tx_id
         |left join block b on b.id = t.block_id
         |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
         |left join tx ti on ti.id = i.tx_in_id
         |left join datum d on d.hash = o.data_hash
         |left join script s on s.id = o.reference_script_id
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
           |  encode(o.payment_cred, 'hex'),
           |  o.value,
           |  encode(o.data_hash, 'hex'),
           |  d.value,
           |  encode(d.bytes, 'hex'),
           |  i.id,
           |  encode(ti.hash, 'hex'),
           |  encode(s.hash, 'hex')
           |from tx_out o
           |left join tx t on t.id = o.tx_id
           |left join block b on b.id = t.block_id
           |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
           |left join tx ti on ti.id = i.tx_in_id
           |left join datum d on d.hash = o.data_hash
           |left join script s on s.id = o.reference_script_id
           |""".stripMargin
    (q ++ Fragments.in(fr"where o.tx_id", txIds)).query
  }

  def getUnspent(offset: Int, limit: Int, ordering: SortOrder): Query0[Output] = {
    val q = sql"""
         |select
         |  o.id,
         |  o.tx_id,
         |  encode(b.hash, 'hex'),
         |  encode(t.hash, 'hex'),
         |  o.index,
         |  o.address,
         |  encode(o.payment_cred, 'hex'),
         |  o.value,
         |  encode(o.data_hash, 'hex'),
         |  d.value,
         |  encode(d.bytes, 'hex'),
         |  null,
         |  null,
         |  encode(s.hash, 'hex')
         |from tx_out o
         |left join tx t on t.id = o.tx_id
         |left join block b on b.id = t.block_id
         |left join datum d on d.hash = o.data_hash
         |left join script s on s.id = o.reference_script_id
         |where NOT EXISTS (select id from tx_in where tx_out_id = o.tx_id and tx_out_index = o.index)
         |""".stripMargin
    (q ++ const(s"order by o.id ${ordering.unwrapped}") ++ const(s"offset $offset limit $limit")).query
  }

  def getAll(offset: Int, limit: Int, ordering: SortOrder): Query0[Output] = {
    val q =
      sql"""
         |select
         |  o.id,
         |  o.tx_id,
         |  encode(b.hash, 'hex'),
         |  encode(t.hash, 'hex'),
         |  o.index,
         |  o.address,
         |  encode(o.payment_cred, 'hex'),
         |  o.value,
         |  encode(o.data_hash, 'hex'),
         |  d.value,
         |  encode(d.bytes, 'hex'),
         |  i.id,
         |  encode(ti.hash, 'hex'),
         |  encode(s.hash, 'hex')
         |from tx_out o
         |left join tx t on t.id = o.tx_id
         |left join block b on b.id = t.block_id
         |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
         |left join tx ti on ti.id = i.tx_in_id
         |left join datum d on d.hash = o.data_hash
         |left join script s on s.id = o.reference_script_id
         |""".stripMargin
    (q ++ const(s"order by o.id ${ordering.value}") ++ const(s"offset $offset limit $limit")).query
  }

  def getUnspentIndexed(minIndex: Int, limit: Int, ordering: SortOrder): Query0[Output] = {
    val q = sql"""
         |select
         |  o.id,
         |  o.tx_id,
         |  encode(b.hash, 'hex'),
         |  encode(t.hash, 'hex'),
         |  o.index,
         |  o.address,
         |  encode(o.payment_cred, 'hex'),
         |  o.value,
         |  encode(o.data_hash, 'hex'),
         |  d.value,
         |  encode(d.bytes, 'hex'),
         |  null,
         |  null,
         |  encode(s.hash, 'hex')
         |from tx_out o
         |left join tx t on t.id = o.tx_id
         |left join block b on b.id = t.block_id
         |left join datum d on d.hash = o.data_hash
         |left join script s on s.id = o.reference_script_id
         |where o.id >= $minIndex and NOT EXISTS (select id from tx_in where tx_out_id = o.tx_id and tx_out_index = o.index)
         |""".stripMargin
    (q ++ const(s"order by o.id ${ordering.value}") ++ const(s"limit $limit")).query
  }

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
         |  encode(o.payment_cred, 'hex'),
         |  o.value,
         |  encode(o.data_hash, 'hex'),
         |  d.value,
         |  encode(d.bytes, 'hex'),
         |  null,
         |  null,
         |  encode(s.hash, 'hex')
         |from tx_out o
         |left join tx t on t.id = o.tx_id
         |left join block b on b.id = t.block_id
         |left join datum d on d.hash = o.data_hash
         |left join script s on s.id = o.reference_script_id
         |where o.address = $addr and NOT EXISTS (select id from tx_in where tx_out_id = o.tx_id and tx_out_index = o.index)
         |offset $offset limit $limit
         |""".stripMargin.query

  def countUnspentByAddr(addr: Addr): Query0[Int] =
    sql"""
         |select count(o.id) from tx_out o
         |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
         |where o.address = $addr and i.id is null
         |""".stripMargin.query

  def getUnspentByPCred(pcred: PaymentCred, offset: Int, limit: Int, ordering: SortOrder): Query0[Output] = {
    val q =
      sql"""
           |select distinct on (o.id)
           |  o.id,
           |  o.tx_id,
           |  encode(b.hash, 'hex'),
           |  encode(t.hash, 'hex'),
           |  o.index,
           |  o.address,
           |  encode(o.payment_cred, 'hex'),
           |  o.value,
           |  encode(o.data_hash, 'hex'),
           |  d.value,
           |  encode(d.bytes, 'hex'),
           |  null,
           |  null,
           |  encode(s.hash, 'hex')
           |from tx_out o
           |left join tx t on t.id = o.tx_id
           |left join block b on b.id = t.block_id
           |left join datum d on d.hash = o.data_hash
           |left join script s on s.id = o.reference_script_id
           |where o.payment_cred = decode($pcred, 'hex') and NOT EXISTS (select id from tx_in where tx_out_id = o.tx_id and tx_out_index = o.index)
           |""".stripMargin
    (q ++ const(s"order by o.id ${ordering.unwrapped}") ++ const(s"offset $offset limit $limit")).query
  }

  def countUnspentByPCred(pcred: PaymentCred): Query0[Int] =
    sql"""
         |select count(o.id) from tx_out o
         |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
         |where o.payment_cred = decode($pcred, 'hex') and i.id is null
         |""".stripMargin.query

  def getUnspentByAsset(asset: AssetRef, offset: Int, limit: Int, ordering: SortOrder): Query0[Output] = {
    val q =
      sql"""
         |select distinct on (o.id)
         |  o.id,
         |  o.tx_id,
         |  encode(b.hash, 'hex'),
         |  encode(t.hash, 'hex'),
         |  o.index,
         |  o.address,
         |  encode(o.payment_cred, 'hex'),
         |  o.value,
         |  encode(o.data_hash, 'hex'),
         |  d.value,
         |  encode(d.bytes, 'hex'),
         |  null,
         |  null,
         |  encode(s.hash, 'hex')
         |from tx_out o
         |left join tx t on t.id = o.tx_id
         |left join block b on b.id = t.block_id
         |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
         |left join ma_tx_out a on a.tx_out_id = o.id
         |left join multi_asset ma on ma.id = a.ident
         |left join datum d on d.hash = o.data_hash
         |left join script s on s.id = o.reference_script_id
         |where ma.policy = decode(${asset.policyId.value}, 'hex') and ma.name = decode(${asset.name.value}, 'escape') and i.id is null
         |""".stripMargin
    (q ++ const(s"order by o.id ${ordering.unwrapped}") ++ const(s"offset $offset limit $limit")).query
  }

  def countUnspentByAsset(asset: AssetRef): Query0[Int] =
    sql"""
         |select count(distinct o.id)
         |from tx_out o
         |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
         |left join ma_tx_out a on a.tx_out_id = o.id
         |left join multi_asset ma on ma.id = a.ident
         |where ma.policy = decode(${asset.policyId.value}, 'hex') and ma.name = decode(${asset.name.value}, 'escape') and i.id is null
         |""".stripMargin.query

  def searchUnspent(
    pcred: PaymentCred,
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
      |  encode(o.payment_cred, 'hex'),
      |  o.value,
      |  encode(o.data_hash, 'hex'),
      |  d.value,
      |  encode(d.bytes, 'hex'),
      |  null,
      |  null,
      |  encode(s.hash, 'hex')
      |from tx_out o
      |left join tx t on t.id = o.tx_id
      |left join block b on b.id = t.block_id
      |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
      |left join ma_tx_out a on a.tx_out_id = o.id
      |left join multi_asset ma on ma.id = a.ident
      |left join datum d on d.hash = o.data_hash
      |left join script s on s.id = o.reference_script_id
      |${containsAllOf.map(innerJoinAllOfAssets("au", "o", _)).getOrElse("")}
      |where
      |  i.id is null and
      |  o.payment_cred = decode('$pcred', 'hex') ${if (containsAnyOf.isDefined) "and" else ""}
      |  ${containsAnyOf.map(as => s"ma.policy in (${as.map(s => s"decode('${s.policyId}', 'hex')").mkString(", ")}) and").getOrElse("")}
      |  ${containsAnyOf.map(as => s"ma.name in (${as.map(s => s"'${s.name}'").mkString(", ")})").getOrElse("")}
      |offset $offset limit $limit
      |""".stripMargin).query

  def countUnspent(
    pcred: PaymentCred,
    containsAllOf: Option[List[AssetRef]],
    containsAnyOf: Option[List[AssetRef]]
  ): Query0[Int] =
    const(s"""
             |select count(distinct o.id)
             |from tx_out o
             |left join tx_in i on i.tx_out_id = o.tx_id and i.tx_out_index = o.index
             |left join ma_tx_out a on a.tx_out_id = o.id
             |left join multi_asset ma on ma.id = a.ident
             |${containsAllOf.map(innerJoinAllOfAssets("au", "o", _)).getOrElse("")}
             |where
             |  i.id is null and
             |  o.payment_cred = decode('$pcred', 'hex') ${if (containsAnyOf.isDefined) "and" else ""}
             |  ${containsAnyOf.map(as => s"ma.policy in (${as.map(s => s"decode('${s.policyId}', 'hex')").mkString(", ")}) and").getOrElse("")}
             |  ${containsAnyOf.map(as => s"ma.name in (${as.map(s => s"'${s.name}'").mkString(", ")})").getOrElse("")}
             |""".stripMargin).query

  private def innerJoinAllOfAssets(
    as: String,
    tableAlias: String,
    containsAllOf: List[AssetRef]
  ): String =
    s"""
       |inner join (
       |  select a0.tx_out_id from ma_tx_out a0
       |  ${containsAllOf.zipWithIndex.tail
      .map { case (_, ix) => s"inner join ma_tx_out a$ix on a0.tx_out_id = a$ix.tx_out_id" }
      .mkString("\n")}
       |${containsAllOf.zipWithIndex
      .map { case (_, ix) => s"inner join multi_asset ma$ix on ma$ix.id = a$ix.ident" }
      .mkString("\n")}
       |  where ${containsAllOf.zipWithIndex
      .map { case (a, ix) =>
        s"ma$ix.policy = decode('${a.policyId}', 'hex') and ma$ix.name = '${a.name}'"
      }
      .mkString(" and ")}
       |) as $as on $as.tx_out_id = $tableAlias.id
       |""".stripMargin
}
