package io.ergolabs.cardano.explorer.core.db.models

import io.ergolabs.cardano.explorer.core.types.{Asset32, PolicyId}

final case class AssetOutput(
  outputId: Long,
  txId: Long,
  name: Asset32,
  quantity: BigInt,
  policy: PolicyId,
  outIndex: Int
)
