package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.ergolabs.cardano.explorer.api.v1.instances._
import io.ergolabs.cardano.explorer.core.types.{OutRef, TxHash}
import sttp.tapir.Schema

@derive(encoder, decoder)
final case class TxInput(
  outRef: OutRef,
  outTxHash: TxHash,
  outIndex: Int,
  value: BigInt,
  jsValue: String,
  redeemer: Option[Redeemer]
)

object TxInput {

  implicit def schema: Schema[TxInput] =
    Schema
      .derived[TxInput]
      .modify(_.outRef)(_.description("The output reference."))
      .modify(_.outTxHash)(_.description("The hash of transaction."))
      .modify(_.outIndex)(_.description("The index of transaction output that corresponding to this input."))
      .modify(_.value)(_.description("The value of the input."))
      .modify(_.jsValue)(_.description("The value of the input in json."))
      .modify(_.redeemer)(_.description("The input redeemer."))

}
