package io.ergolabs.cardano.explorer.api.v1.models

import io.ergolabs.cardano.explorer.core.types.TxHash

final case class TxInput(outTxHash: TxHash, outIndex: Int, value: BigInt)
