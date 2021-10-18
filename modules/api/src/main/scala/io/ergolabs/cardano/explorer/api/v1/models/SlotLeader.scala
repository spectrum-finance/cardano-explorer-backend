package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.ergolabs.cardano.explorer.core.types.LeaderHash
import sttp.tapir.Schema
import io.ergolabs.cardano.explorer.api.v1.instances._

@derive(encoder, decoder)
final case class SlotLeader(id: BigInt, hash: LeaderHash, poolHashId: BigInt, description: String)

object SlotLeader {

  implicit val schema: Schema[SlotLeader] =
    Schema
      .derived[SlotLeader]
      .modify(_.id)(_.description("The slot leader index number."))
      .modify(_.hash)(_.description("The hash of of the block producer identifier."))
      .modify(_.poolHashId)(_.description("If the slot leader is a pool, an index number of poolHash."))
      .modify(_.description)(_.description("An auto-generated description of the slot leader."))

}
