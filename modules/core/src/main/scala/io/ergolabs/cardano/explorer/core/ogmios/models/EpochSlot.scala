package io.ergolabs.cardano.explorer.core.ogmios.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import sttp.tapir.Schema

@derive(encoder, decoder)
final case class EpochSlot(time: Int, slot: Int, epoch: Int)

object EpochSlot {

  implicit val schema: Schema[EpochSlot] = Schema.derived[EpochSlot]
}
