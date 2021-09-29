package io.ergolabs.cardano.explorer.core.db.models

import io.ergolabs.cardano.explorer.core.types.{Asset32, Hash28, TxHash}

final case class AssetOutput(
  outputId: Long,
  txId: Long,
  name: Asset32,
  quantity: BigInt,
  policy: Hash28,
  txHash: TxHash,
  outIndex: Int
)
