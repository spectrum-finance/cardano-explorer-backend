package io.ergolabs.cardano.explorer.api.configs

import derevo.derive
import derevo.pureconfig.pureconfigReader
import io.ergolabs.cardano.explorer.api.graphite.GraphiteSettings
import tofu.logging.derivation.loggable
import tofu.optics.macros.{promote, ClassyOptics}

@derive(loggable, pureconfigReader)
@ClassyOptics
final case class ConfigBundle(
  @promote http: HttpConfig,
  @promote pg: PgConfig,
  @promote requests: RequestConfig,
  @promote graphite: GraphiteSettings,
  @promote graphitePathPrefix: String
)

object ConfigBundle extends ConfigBundleCompanion[ConfigBundle]
