package io.ergolabs.cardano.explorer.api.graphite

import derevo.derive
import derevo.pureconfig.pureconfigReader
import tofu.logging.derivation.loggable

@derive(pureconfigReader, loggable)
final case class GraphiteSettings(
  host: String,
  port: Int,
)
