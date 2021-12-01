package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import sttp.tapir.Schema

@derive(encoder, decoder)
case class ProtocolParams ()

object ProtocolParams {

  implicit val schema: Schema[ProtocolParams] = Schema.derived[ProtocolParams]
}
