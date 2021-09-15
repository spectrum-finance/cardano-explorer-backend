package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.Json
import io.ergolabs.cardano.explorer.api.v1.instances._
import io.ergolabs.cardano.explorer.core.db.models.{Asset, Output}
import io.ergolabs.cardano.explorer.core.types._
import sttp.tapir.Schema

@derive(encoder, decoder)
final case class TxOutput(
  ref: OutRef,
  blockHash: BlockHash,
  txHash: TxHash,
  index: Int,
  addr: Addr,
  value: BigInt,
  jsValue: String,
  dataHash: Option[Hash32],
  data: Option[Json],
  spentByTxHash: Option[TxHash],
  assets: List[OutAsset]
)

object TxOutput {
  implicit def schemaJson: Schema[Json] = Schema.string[Json]
  implicit def schema: Schema[TxOutput] = Schema.derived

  def inflate(out: Output, assets: List[Asset]): TxOutput = {
    val outAssets = assets.map(a => OutAsset(a.name, a.quantity))
    TxOutput(
      OutRef(out.txHash, out.index),
      out.blockHash,
      out.txHash,
      out.index,
      out.addr,
      out.value,
      out.value.toString(),
      out.dataHash,
      out.data,
      out.spentByTxHash,
      outAssets
    )
  }
}
