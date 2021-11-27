package io.ergolabs.cardano.explorer.core.db.models

import io.ergolabs.cardano.explorer.core.types.{BlockHash, TxHash}

final case class Input(
  txId: Long,
  redeemerId: Option[Long],
  blockHash: BlockHash,
  txHash: TxHash,
  output: Output
)
