package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.ergolabs.cardano.explorer.api.v1.instances._
import io.ergolabs.cardano.explorer.core.ScriptPurpose
import io.ergolabs.cardano.explorer.core.types.Hash28
import sttp.tapir.Schema

@derive(encoder, decoder)
final case class Redeemer(
  unitMem: Long,
  unitSteps: Long,
  fee: BigInt,
  purpose: ScriptPurpose,
  index: Int,
  scriptHash: Hash28
)

object Redeemer {
  implicit def schema: Schema[Redeemer] = Schema.derived
}
