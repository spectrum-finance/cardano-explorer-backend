package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.Json
import io.ergolabs.cardano.explorer.api.v1.instances._
import io.ergolabs.cardano.explorer.core.db.models.{Asset, AssetOutput, Output}
import io.ergolabs.cardano.explorer.core.types._
import sttp.tapir.Schema

@derive(encoder, decoder)
final case class TxOutput(
  ref: OutRef,
  blockHash: BlockHash,
  txHash: TxHash,
  index: Int,
  globalIndex: Long,
  addr: Addr,
  paymentCred: Option[PaymentCred],
  value: List[OutAsset],
  dataHash: Option[Hash32],
  data: Option[Json],
  dataBin: Option[Bytea],
  spentByTxHash: Option[TxHash],
  refScriptHash: Option[Hash32]
)

object TxOutput {

  implicit def schemaJson: Schema[Json] = Schema.string[Json]

  implicit def schema: Schema[TxOutput] =
    Schema
      .derived[TxOutput]
      .modify(_.ref)(_.description("The output reference."))
      .modify(_.blockHash)(_.description("The hash identifier of the output block."))
      .modify(_.txHash)(_.description("The hash identifier of the output transaction."))
      .modify(_.index)(
        _.description("The index of an output in the transaction that contains this transaction output.")
      )
      .modify(_.globalIndex)(_.description("The index of an output in the blockchain."))
      .modify(_.addr)(
        _.description(
          "The human readable encoding of the output address. Will be Base58 for Byron era addresses and Bech32 for Shelley era."
        )
      )
      .modify(_.paymentCred)(_.description("Payment credential of the address."))
      .modify(_.value)(_.description("The output value (in Lovelace) of the transaction output."))
      .modify(_.dataHash)(_.description("The hash of the transaction output datum. (`null` for Txs without scripts)."))
      .modify(_.data)(_.description("The transaction output datum. (`null` for Txs without scripts)."))
      .modify(_.spentByTxHash)(
        _.description("The hash of the transaction that spent this output. (`null` for unspent outputs)")
      )
      .modify(_.spentByTxHash)(
        _.description("The hash of ref.input")
      )

  def inflate(out: Output, assets: List[Asset]): TxOutput = {
    TxOutput(
      OutRef(out.txHash, out.index),
      out.blockHash,
      out.txHash,
      out.index,
      out.id,
      out.addr,
      out.pcred,
      Value(out.lovelace, assets),
      out.dataHash,
      out.data,
      out.dataBin,
      out.spentByTxHash,
      out.refScriptHash
    )
  }

  def inflateBatch(outs: List[Output], assets: List[AssetOutput]): List[TxOutput] =
    outs.map(o => inflate(o, assets.filter(_.outputId == o.id).map(_.asset)))
}
