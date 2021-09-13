package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import sttp.tapir.Schema
import io.ergolabs.cardano.explorer.api.v1.instances._

@derive(encoder, decoder)
final case class OutAsset(name: String, quantity: BigInt)

object OutAsset {
  implicit def schema: Schema[OutAsset] = Schema.derived
}
