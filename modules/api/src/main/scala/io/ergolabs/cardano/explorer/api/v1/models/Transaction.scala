package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.ergolabs.cardano.explorer.core.db.models.{Asset, Input, Output, Transaction => DbTransaction}
import io.ergolabs.cardano.explorer.core.types.{BlockHash, TxHash}
import sttp.tapir.Schema

@derive(encoder, decoder)
final case class Transaction(
  blockHash: BlockHash,
  blockIndex: Long,
  hash: TxHash,
  inputs: List[TxInput],
  outputs: List[TxOutput],
  size: Int
)

object Transaction {

  implicit def schema: Schema[Transaction] = Schema.derived

  def inflate(tx: DbTransaction, inputs: List[Input], outputs: List[Output], assets: List[Asset]): Transaction = {
    val txInputs = inputs.map(i => TxInput(i.outTxHash, i.outIndex, i.value))
    val txOutputs = outputs.map { o =>
      val outAssets = assets.filter(_.outIndex == o.index).map(a => OutAsset(a.name, a.quantity))
      TxOutput(o.blockHash, o.index, o.addr, o.value, o.dataHash, outAssets)
    }
    Transaction(tx.blockHash, tx.blockIndex, tx.hash, txInputs, txOutputs, tx.size)
  }
}
