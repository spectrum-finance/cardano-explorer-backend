package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import sttp.tapir.Schema

@derive(encoder, decoder)
case class NetworkId ()

object NetworkId {

  implicit val schema: Schema[NetworkId] = Schema.derived[NetworkId]
}