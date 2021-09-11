package io.ergolabs.cardano.explorer.api.v1.models

import io.ergolabs.cardano.explorer.core.types.{Addr, BlockHash, Hash32}

final case class TxOutput(
  blockHash: BlockHash,
  index: Int,
  addr: Addr,
  value: BigInt,
  dataHash: Hash32,
  assets: List[OutAsset]
)
