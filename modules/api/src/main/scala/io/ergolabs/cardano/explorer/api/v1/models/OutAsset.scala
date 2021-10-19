package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.ergolabs.cardano.explorer.api.v1.instances._
import io.ergolabs.cardano.explorer.core.types.{Asset32, AssetRef, PolicyId}
import sttp.tapir.Schema

@derive(encoder, decoder)
final case class OutAsset(
  policy: PolicyId,
  name: Asset32,
  quantity: BigInt,
  jsQuantity: String
)

object OutAsset {
  implicit def schema: Schema[OutAsset] = Schema.derived
}
