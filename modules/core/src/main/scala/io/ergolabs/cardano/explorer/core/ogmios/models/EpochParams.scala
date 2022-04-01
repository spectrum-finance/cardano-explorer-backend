package io.ergolabs.cardano.explorer.core.ogmios.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import sttp.tapir.Schema

@derive(encoder, decoder)
final case class EpochParams(epochLength: Int, slotLength: Int, safeZone: Int)

object EpochParams {

  implicit val schema: Schema[EpochParams] = Schema.derived[EpochParams]
}
