package io.ergolabs.cardano.explorer.core.db.models

import io.circe.Json
import io.ergolabs.cardano.explorer.core.types.Bytea

final case class TxMetadata(txId: Long, key: BigInt, raw: Bytea, json: Json)
