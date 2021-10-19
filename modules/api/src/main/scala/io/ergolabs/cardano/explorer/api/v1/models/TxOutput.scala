package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.Json
import io.ergolabs.cardano.explorer.api.v1.instances._
import io.ergolabs.cardano.explorer.core.db.models.{AssetOutput, Output}
import io.ergolabs.cardano.explorer.core.types._
import sttp.tapir.Schema

@derive(encoder, decoder)
final case class TxOutput(
  ref: OutRef,
  blockHash: BlockHash,
  txHash: TxHash,
  index: Int,
  addr: Addr,
  value: List[OutAsset],
  dataHash: Option[Hash32],
  data: Option[Json],
  spentByTxHash: Option[TxHash]
)

object TxOutput {
  implicit def schemaJson: Schema[Json] = Schema.string[Json]
  implicit def schema: Schema[TxOutput] = Schema.derived

  def inflate(out: Output, assets: List[AssetOutput]): TxOutput =
    TxOutput(
      OutRef(out.txHash, out.index),
      out.blockHash,
      out.txHash,
      out.index,
      out.addr,
      Value(out.lovelace, assets),
      out.dataHash,
      out.data,
      out.spentByTxHash
    )

  def inflateBatch(outs: List[Output], assets: List[AssetOutput]): List[TxOutput] =
    outs.map(o => inflate(o, assets.filter(_.outputId == o.id)))
}
