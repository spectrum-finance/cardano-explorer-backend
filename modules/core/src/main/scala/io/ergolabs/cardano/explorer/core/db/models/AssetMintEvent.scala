package io.ergolabs.cardano.explorer.core.db.models

import io.ergolabs.cardano.explorer.core.types.{Asset32, PolicyId}

final case class AssetMintEvent(
  id: Long,
  policy: PolicyId,
  name: Asset32,
  quantity: BigInt,
  txId: Long
)
