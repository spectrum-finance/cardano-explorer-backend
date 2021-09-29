package io.ergolabs.cardano.explorer.core.db.models

import io.ergolabs.cardano.explorer.core.types.{Asset32, Hash28}

final case class AssetMintEvent(
  id: BigInt,
  policy: Hash28,
  name: Asset32,
  quantity: BigInt,
  txId: Long
)