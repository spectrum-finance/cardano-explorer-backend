package io.ergolabs.cardano.explorer.core.db.models

import io.ergolabs.cardano.explorer.core.types.{BlockHash, TxHash}

final case class Input(
  blockHash: BlockHash,
  txHash: TxHash,
  outTxHash: TxHash,
  outIndex: Int,
  value: BigInt
)
