package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.Json
import io.ergolabs.cardano.explorer.api.v1.instances._
import io.ergolabs.cardano.explorer.core.types.Bytea
import sttp.tapir.Schema

@derive(encoder, decoder)
final case class Metadata(key: BigInt, raw: Bytea, json: Json)

object Metadata {
  implicit def schemaJson: Schema[Json] = Schema.string[Json]
  implicit def schema: Schema[Metadata] = Schema.derived
}
