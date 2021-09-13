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
  jsValue: String
)

object TxInput {
  implicit def schema: Schema[TxInput] = Schema.derived
}
