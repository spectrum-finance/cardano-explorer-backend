package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.ergolabs.cardano.explorer.core.types.TxHash
import sttp.tapir.Schema
import io.ergolabs.cardano.explorer.api.v1.instances._

@derive(encoder, decoder)
final case class TxInput(outTxHash: TxHash, outIndex: Int, value: BigInt)

object TxInput {
  implicit def schema: Schema[TxInput] = Schema.derived
}
