package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.ergolabs.cardano.explorer.core.types.{Addr, BlockHash, Hash32}
import sttp.tapir.Schema
import io.ergolabs.cardano.explorer.api.v1.instances._

@derive(encoder, decoder)
final case class TxOutput(
  blockHash: BlockHash,
  index: Int,
  addr: Addr,
  value: BigInt,
  dataHash: Hash32,
  assets: List[OutAsset]
)

object TxOutput {
  implicit def schema: Schema[TxOutput] = Schema.derived
}
