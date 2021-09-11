package io.ergolabs.cardano.explorer.core.db.models

import io.ergolabs.cardano.explorer.core.types.{Hash28, TxHash}

final case class Asset(
  name: String,
  quantity: BigInt,
  policy: Hash28,
  txHash: TxHash,
  outIndex: Int
)
