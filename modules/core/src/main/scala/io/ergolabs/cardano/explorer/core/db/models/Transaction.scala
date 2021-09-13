package io.ergolabs.cardano.explorer.core.db.models

import derevo.derive
import io.ergolabs.cardano.explorer.core.types.{BlockHash, TxHash}
import tofu.logging.derivation.loggable

@derive(loggable)
final case class Transaction(
  id: Long,
  blockHash: BlockHash,
  blockIndex: Long,
  hash: TxHash,
  size: Int
)
