package io.ergolabs.cardano.explorer.api

import derevo.derive
import io.estatico.newtype.macros.newtype
import tofu.logging.derivation.loggable
import tofu.{WithContext, WithLocal}

object types {

  @derive(loggable)
  @newtype case class TraceId(value: String)

  object TraceId {
    type Local[F[_]] = WithLocal[F, TraceId]
    type Has[F[_]]   = WithContext[F, TraceId]

    def fromString(s: String): TraceId = apply(s)
  }
}
