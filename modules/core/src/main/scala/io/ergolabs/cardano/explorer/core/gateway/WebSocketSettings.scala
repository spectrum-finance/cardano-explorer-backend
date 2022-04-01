package io.ergolabs.cardano.explorer.core.gateway

import derevo.derive
import derevo.pureconfig.pureconfigReader
import tofu.logging.derivation.loggable

@derive(pureconfigReader, loggable)
final case class WebSocketSettings(uri: String)
