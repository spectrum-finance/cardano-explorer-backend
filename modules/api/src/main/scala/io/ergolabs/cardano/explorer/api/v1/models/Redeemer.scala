package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.Json
import io.ergolabs.cardano.explorer.api.v1.instances._
import io.ergolabs.cardano.explorer.core.ScriptPurpose
import io.ergolabs.cardano.explorer.core.types.{Bytea, Hash28}
import sttp.tapir.Schema

@derive(encoder, decoder)
final case class Redeemer(
  unitMem: Long,
  unitSteps: Long,
  fee: BigInt,
  purpose: ScriptPurpose,
  index: Int,
  scriptHash: Hash28,
  data: Option[Json],
  dataBin: Option[Bytea]
)

object Redeemer {
  implicit def schemaJson: Schema[Json] = Schema.string[Json]

  implicit def schema: Schema[Redeemer] =
    Schema.derived[Redeemer]
      .modify(_.unitMem)(_.description("The budget in Memory to run a script."))
      .modify(_.unitSteps)(_.description("The budget in Cpu steps to run a script."))
      .modify(_.fee)(_.description("The budget in fees to run a script. The fees depend on the ExUnits and the current prices."))
      .modify(_.purpose)(_.description("What kind pf validation this redeemer is used for. It can be one of 'spend', 'mint', 'cert', 'reward'."))
      .modify(_.index)(_.description("The index of the redeemer pointer in the transaction."))
      .modify(_.scriptHash)(_.description("The script hash this redeemer is used for."))
      .modify(_.data)(_.description("Redeemer data."))
      .modify(_.dataBin)(_.description("Serialized redeemer data."))
}
