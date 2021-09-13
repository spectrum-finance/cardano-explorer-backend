package io.ergolabs.cardano.explorer.api.v1

import sttp.tapir.Schema

object instances {
  implicit def schemaBigInt: Schema[BigInt] = Schema.schemaForBigDecimal.map(_.toBigIntExact)(BigDecimal(_))
}
