package io.ergolabs.cardano.explorer.api.configs

import derevo.derive
import derevo.pureconfig.pureconfigReader
import tofu.logging.derivation.loggable

import scala.concurrent.duration.FiniteDuration

@derive(loggable, pureconfigReader)
final case class PgConfig(
  url: String,
  user: String,
  pass: String,
  connectionTimeout: FiniteDuration,
  minConnections: Int,
  maxConnections: Int
)