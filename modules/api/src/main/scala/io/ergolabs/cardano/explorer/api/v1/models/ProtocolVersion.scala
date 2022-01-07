package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import sttp.tapir.Schema

@derive(encoder, decoder)
final case class ProtocolVersion (major: Int, minor: Int)

object ProtocolVersion {

  implicit val scheme: Schema[ProtocolVersion] = Schema.derived[ProtocolVersion]
}