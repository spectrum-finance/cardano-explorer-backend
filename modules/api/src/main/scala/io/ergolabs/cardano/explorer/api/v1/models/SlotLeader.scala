package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.ergolabs.cardano.explorer.core.types.LeaderHash
import sttp.tapir.Schema
import io.ergolabs.cardano.explorer.api.v1.instances._

@derive(encoder, decoder)
final case class SlotLeader(id: BigInt, hash: LeaderHash, poolHashId: BigInt, description: String)

object SlotLeader {

  implicit val schema: Schema[SlotLeader] = Schema.derived
}
