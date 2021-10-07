package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.ergolabs.cardano.explorer.api.v1.instances._
import io.ergolabs.cardano.explorer.core.types.{Asset32, PolicyId}
import sttp.tapir.Schema

@derive(encoder, decoder)
final case class AssetInfo(
  policyId: PolicyId,
  name: Asset32,
  quantity: BigInt
)

object AssetInfo {

  implicit val schema: Schema[AssetInfo] = Schema.derived
}
