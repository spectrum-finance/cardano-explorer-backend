package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.ergolabs.cardano.explorer.api.v1.instances._
import io.ergolabs.cardano.explorer.core.db.models.{
  AssetOutput,
  Input,
  Output,
  TxMetadata,
  Redeemer => DbRedeemer,
  Transaction => DbTransaction
}
import io.ergolabs.cardano.explorer.core.types.{BlockHash, OutRef, TxHash}
import sttp.tapir.Schema

@derive(encoder, decoder)
final case class Transaction(
  blockHash: BlockHash,
  blockIndex: Long,
  hash: TxHash,
  inputs: List[TxInput],
  outputs: List[TxOutput],
  invalidBefore: Option[BigInt],
  invalidHereafter: Option[BigInt],
  metadata: Option[Metadata],
  size: Int
)

object Transaction {

  implicit def schema: Schema[Transaction] =
    Schema
      .derived[Transaction]
      .modify(_.blockHash)(_.description("The hash identifier of the transaction block."))
      .modify(_.blockIndex)(_.description("The index of this transaction with the block (zero based)."))
      .modify(_.hash)(_.description("The hash identifier of the transaction."))
      .modify(_.inputs)(_.description("The transaction inputs."))
      .modify(_.outputs)(_.description("The transaction outputs."))
      .modify(_.invalidBefore)(_.description("Transaction in invalid before this slot number."))
      .modify(_.invalidHereafter)(_.description("Transaction in invalid at or after this slot number."))
      .modify(_.metadata)(_.description("The transaction metadata."))
      .modify(_.size)(_.description("The size of the transaction in bytes."))

  def inflate(
    tx: DbTransaction,
    inputs: List[Input],
    outputs: List[Output],
    assets: List[AssetOutput],
    redeemers: List[DbRedeemer],
    meta: Option[TxMetadata]
  ): Transaction = {
    val txInputs = inputs.map { i =>
      val maybeRedeemer =
        for {
          rid <- i.redeemerId
          r   <- redeemers.find(_.id == rid)
        } yield Redeemer(r.unitMem, r.unitSteps, r.fee, r.purpose, r.index, r.scriptHash)
      TxInput(OutRef(i.outTxHash, i.outIndex), i.outTxHash, i.outIndex, i.value, i.value.toString(), maybeRedeemer)
    }
    val txOutputs = outputs.map(o => TxOutput.inflate(o, assets.filter(_.outIndex == o.index)))
    val metadata  = meta.map(m => Metadata(m.key, m.raw, m.json))
    Transaction(
      tx.blockHash,
      tx.blockIndex,
      tx.hash,
      txInputs,
      txOutputs,
      tx.invalidBefore,
      tx.invalidHereafter,
      metadata,
      tx.size
    )
  }

  def inflateBatch(
    txs: List[DbTransaction],
    inputs: List[Input],
    outputs: List[Output],
    assets: List[AssetOutput],
    redeemers: List[DbRedeemer],
    meta: List[TxMetadata]
  ): List[Transaction] = {
    val inputsByTx   = inputs.groupBy(_.txId)
    val outputsByTx  = outputs.groupBy(_.txId)
    val assetsByTx   = assets.groupBy(_.txId)
    val redeemerByTx = redeemers.groupBy(_.txId)
    val metaByTx     = meta.groupBy(_.txId)
    txs.map { tx =>
      Transaction.inflate(
        tx,
        inputsByTx.getOrElse(tx.id, List.empty),
        outputsByTx.getOrElse(tx.id, List.empty),
        assetsByTx.getOrElse(tx.id, List.empty),
        redeemerByTx.getOrElse(tx.id, List.empty),
        metaByTx.get(tx.id).flatMap(_.headOption)
      )
    }
  }
}
