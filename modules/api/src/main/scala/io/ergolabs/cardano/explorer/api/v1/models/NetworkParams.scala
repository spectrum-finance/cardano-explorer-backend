package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.Json
import io.ergolabs.cardano.explorer.api.v1.instances._
import io.ergolabs.cardano.explorer.core.types.Bytea
import sttp.tapir.Schema

@derive(encoder, decoder)
final case class NetworkParams(
  startTime: String,
  networkName: String,
  epochNo: Int,
  ids: List[Int]
)

object NetworkParams {

  implicit def schema: Schema[NetworkParams] =
    Schema
      .derived[NetworkParams]
      .modify(_.startTime)(_.description(""))
      .modify(_.networkName)(_.description(""))

}