package io.ergolabs.cardano.explorer.core.db.models

import io.ergolabs.cardano.explorer.core.types.LeaderHash

final case class SlotLeaderInfo(
  id: BigInt,
  hash: LeaderHash,
  poolHashId: Option[BigInt],
  description: String
)
