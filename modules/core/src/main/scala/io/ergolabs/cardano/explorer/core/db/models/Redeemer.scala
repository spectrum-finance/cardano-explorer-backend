package io.ergolabs.cardano.explorer.core.db.models

import io.ergolabs.cardano.explorer.core.ScriptPurpose
import io.ergolabs.cardano.explorer.core.types.Hash28

final case class Redeemer(
  id: Long,
  txId: Long,
  unitMem: Long,
  unitSteps: Long,
  fee: BigInt,
  purpose: ScriptPurpose,
  index: Int,
  scriptHash: Hash28
)
