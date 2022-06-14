package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.ergolabs.cardano.explorer.api.v1.instances._
import io.ergolabs.cardano.explorer.core.db.models.{
  AssetInput,
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
  globalIndex: Long,
  hash: TxHash,
  inputs: List[TxInput],
  outputs: List[TxOutput],
  invalidBefore: Option[BigInt],
  invalidHereafter: Option[BigInt],
  metadata: Option[Metadata],
  size: Int,
  timestamp: Long
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
      .modify(_.timestamp)(_.description(s"Timestamp of a block where transaction exists."))

  def inflate(
    tx: DbTransaction,
    inputs: List[Input],
    outputs: List[Output],
    inputAssets: List[AssetInput],
    outputAssets: List[AssetOutput],
    redeemers: List[DbRedeemer],
    meta: Option[TxMetadata]
  ): Transaction = {
    val txInputs = inputs.map { i =>
      val maybeRedeemer =
        for {
          rid <- i.redeemerId
          r   <- redeemers.find(_.id == rid)
        } yield Redeemer(r.unitMem, r.unitSteps, r.fee, r.purpose, r.index, r.scriptHash, r.data, r.dataBin)
      TxInput(TxOutput.inflate(i.output, inputAssets.filter(_.outputId == i.output.id).map(_.asset)), maybeRedeemer)
    }
    val txOutputs = outputs.map(o => TxOutput.inflate(o, outputAssets.filter(_.outputId == o.id).map(_.asset)))
    val metadata  = meta.map(m => Metadata(m.key, m.raw, m.json))
    Transaction(
      tx.blockHash,
      tx.blockIndex,
      tx.id,
      tx.hash,
      txInputs,
      txOutputs,
      tx.invalidBefore,
      tx.invalidHereafter,
      metadata,
      tx.size,
      tx.timestamp
    )
  }

  def inflateBatch(
    txs: List[DbTransaction],
    inputs: List[Input],
    outputs: List[Output],
    inputAssets: List[AssetInput],
    outputAssets: List[AssetOutput],
    redeemers: List[DbRedeemer],
    meta: List[TxMetadata]
  ): List[Transaction] = {
    val inputsByTx       = inputs.groupBy(_.txId)
    val outputsByTx      = outputs.groupBy(_.txId)
    val inputAssetsByTx  = inputAssets.groupBy(_.inputTxId)
    val outputAssetsByTx = outputAssets.groupBy(_.outputTxId)
    val redeemerByTx     = redeemers.groupBy(_.txId)
    val metaByTx         = meta.groupBy(_.txId)
    txs.map { tx =>
      Transaction.inflate(
        tx,
        inputsByTx.getOrElse(tx.id, List.empty),
        outputsByTx.getOrElse(tx.id, List.empty),
        inputAssetsByTx.getOrElse(tx.id, List.empty),
        outputAssetsByTx.getOrElse(tx.id, List.empty),
        redeemerByTx.getOrElse(tx.id, List.empty),
        metaByTx.get(tx.id).flatMap(_.headOption)
      )
    }
  }
}
