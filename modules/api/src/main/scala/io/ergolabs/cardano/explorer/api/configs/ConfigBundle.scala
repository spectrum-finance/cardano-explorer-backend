package io.ergolabs.cardano.explorer.api.configs

import derevo.derive
import derevo.pureconfig.pureconfigReader
import io.ergolabs.cardano.explorer.core.gateway.WebSocketSettings
import tofu.logging.derivation.loggable
import tofu.optics.macros.{promote, ClassyOptics}

@derive(loggable, pureconfigReader)
@ClassyOptics
final case class ConfigBundle(
  @promote http: HttpConfig,
  @promote pg: PgConfig,
  @promote requests: RequestConfig,
  @promote webSocketCfg: WebSocketSettings,
  @promote timeouts: ClientTimeouts
)

object ConfigBundle extends ConfigBundleCompanion[ConfigBundle]
