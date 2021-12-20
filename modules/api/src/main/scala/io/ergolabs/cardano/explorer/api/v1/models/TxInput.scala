package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.ergolabs.cardano.explorer.api.v1.instances._
import io.ergolabs.cardano.explorer.core.types.{OutRef, TxHash}
import sttp.tapir.Schema

@derive(encoder, decoder)
final case class TxInput(
  out: TxOutput,
  redeemer: Option[Redeemer]
)

object TxInput {

  implicit def schema: Schema[TxInput] =
    Schema
      .derived[TxInput]
      .modify(_.out)(_.description("The output this input refers to."))
      .modify(_.redeemer)(_.description("The input redeemer."))
}
