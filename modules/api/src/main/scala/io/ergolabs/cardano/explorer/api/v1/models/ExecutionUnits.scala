package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import sttp.tapir.Schema

@derive(encoder, decoder)
case class ExecutionUnits(steps: Option[Double], memory: Option[Double])

object ExecutionUnits {
  implicit val schema: Schema[ExecutionUnits] = Schema.derived[ExecutionUnits]
}