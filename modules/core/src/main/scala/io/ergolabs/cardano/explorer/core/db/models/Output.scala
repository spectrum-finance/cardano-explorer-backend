package io.ergolabs.cardano.explorer.core.db.models

import io.circe.Json
import io.ergolabs.cardano.explorer.core.types.{Addr, BlockHash, Bytea, Hash32, PaymentCred, TxHash}

final case class Output(
  id: Long,
  txId: Long,
  blockHash: BlockHash,
  txHash: TxHash,
  index: Int,
  addr: Addr,
  rawAddr: Bytea,
  pcred: Option[PaymentCred],
  lovelace: BigInt,
  dataHash: Option[Hash32],
  data: Option[Json],
  dataBin: Option[Bytea],
  inputId: Option[Long],
  spentByTxHash: Option[TxHash],
  refScriptHash: Option[Hash32]
)
