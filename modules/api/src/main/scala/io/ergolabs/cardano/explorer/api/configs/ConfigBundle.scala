package io.ergolabs.cardano.explorer.api.configs

import derevo.derive
import derevo.pureconfig.pureconfigReader
import tofu.logging.derivation.loggable
import tofu.optics.macros.{ClassyOptics, promote}

@derive(loggable, pureconfigReader)
@ClassyOptics
final case class ConfigBundle(@promote http: HttpConfig, @promote pg: PgConfig)

object ConfigBundle extends ConfigBundleCompanion[ConfigBundle]
