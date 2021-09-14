package io.ergolabs.cardano.explorer.api

import derevo.derive
import io.ergolabs.cardano.explorer.api.configs.ConfigBundle
import io.ergolabs.cardano.explorer.api.types.TraceId
import io.estatico.newtype.ops._
import tofu.WithContext
import tofu.logging.derivation.{hidden, loggable}
import tofu.optics.macros.{promote, ClassyOptics}

@ClassyOptics
@derive(loggable)
final case class AppContext(
  @promote @hidden config: ConfigBundle,
  @promote traceId: TraceId
)

object AppContext extends WithContext.Companion[AppContext] {

  def init(configs: ConfigBundle): AppContext =
    AppContext(configs, "<Root>".coerce[TraceId])
}
