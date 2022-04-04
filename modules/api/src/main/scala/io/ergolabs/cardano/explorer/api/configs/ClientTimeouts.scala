package io.ergolabs.cardano.explorer.api.configs

import derevo.derive
import derevo.pureconfig.pureconfigReader
import tofu.logging.derivation.loggable

import scala.concurrent.duration.FiniteDuration

@derive(pureconfigReader, loggable)
final case class ClientTimeouts(connectTimeout: FiniteDuration, timeout: FiniteDuration)
