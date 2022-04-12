package io.ergolabs.cardano.explorer.core.db.models

import io.ergolabs.cardano.explorer.core.types.BlockHash

import java.time.LocalDateTime

final case class BlockHeader(
  blockId: BigInt,
  blockHash: BlockHash,
  blockNo: Int,
  epochNo: Int,
  slotNo: Int,
  slotLeaderId: BigInt,
  txCount: BigInt,
  time: LocalDateTime
)
