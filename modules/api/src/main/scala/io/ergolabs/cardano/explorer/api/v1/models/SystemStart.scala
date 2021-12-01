package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import sttp.tapir.Schema

@derive(encoder, decoder)
case class SystemStart ()

object SystemStart {

  implicit val schema: Schema[SystemStart] = Schema.derived[SystemStart]
}
