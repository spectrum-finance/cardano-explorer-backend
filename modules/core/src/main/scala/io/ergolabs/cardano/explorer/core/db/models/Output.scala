package io.ergolabs.cardano.explorer.core.db.models

import io.circe.Json
import io.ergolabs.cardano.explorer.core.types.{Addr, BlockHash, Hash32, TxHash}

final case class Output(
  id: Long,
  txId: Long,
  blockHash: BlockHash,
  txHash: TxHash,
  index: Int,
  addr: Addr,
  value: BigInt,
  dataHash: Option[Hash32],
  data: Option[Json],
  inputId: Option[Long],
  spentByTxHash: Option[TxHash]
)
