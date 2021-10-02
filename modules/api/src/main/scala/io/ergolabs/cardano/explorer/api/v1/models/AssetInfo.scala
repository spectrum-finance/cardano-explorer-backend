package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.ergolabs.cardano.explorer.api.v1.instances._
import io.ergolabs.cardano.explorer.core.types.{Asset32, BlockHash, PolicyHash, TxHash}
import sttp.tapir.Schema

@derive(encoder, decoder)
final case class AssetInfo(
  policyHash: PolicyHash,
  name: Asset32,
  quantity: BigInt,
  mintTxsIds: List[Long]
)

object AssetInfo {

  implicit val schema: Schema[AssetInfo] = Schema.derived

  def emptyById(name: Asset32): AssetInfo =
    AssetInfo(PolicyHash.empty, name, -1, List.empty)
}
