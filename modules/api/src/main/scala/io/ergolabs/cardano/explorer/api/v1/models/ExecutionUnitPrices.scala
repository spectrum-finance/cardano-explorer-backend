package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import sttp.tapir.Schema
  
@derive(encoder, decoder)
case class ExecutionUnitPrices(priceSteps: Option[Double], priceMemory: Option[Double])

object ExecutionUnitPrices {
  implicit val schema: Schema[ExecutionUnitPrices] = Schema.derived[ExecutionUnitPrices]
}