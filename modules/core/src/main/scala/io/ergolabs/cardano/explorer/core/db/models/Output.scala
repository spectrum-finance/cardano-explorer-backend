package io.ergolabs.cardano.explorer.core.db.models

import io.ergolabs.cardano.explorer.core.types.{Addr, BlockHash, Hash32, TxHash}

final case class Output(
  blockHash: BlockHash,
  txHash: TxHash,
  index: Int,
  addr: Addr,
  value: BigInt,
  dataHash: Hash32
)
