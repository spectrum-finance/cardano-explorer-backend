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
  implicit def schema: Schema[Metadata] =
    Schema
      .derived[Metadata]
      .modify(_.key)(_.description("The metadata key (a Word64/unsigned 64 bit number)."))
      .modify(_.raw)(_.description("The raw bytes of the payload."))
      .modify(_.json)(_.description("The JSON payload if it can be decoded as JSON."))

}
