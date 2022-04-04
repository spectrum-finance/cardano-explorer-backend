package io.ergolabs.cardano.explorer.core.ogmios.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import sttp.tapir.Schema

@derive(encoder, decoder)
final case class EraInfo(start: EpochSlot, end: Option[EpochSlot], parameters: EpochParams)

object EraInfo {

  implicit val schema: Schema[EraInfo] = Schema.derived[EraInfo]
}
