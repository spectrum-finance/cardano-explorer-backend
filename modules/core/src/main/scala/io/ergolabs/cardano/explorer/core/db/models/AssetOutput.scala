package io.ergolabs.cardano.explorer.core.db.models

import io.ergolabs.cardano.explorer.core.types.{Asset32, PolicyId}

final case class Asset(
  name: Asset32,
  nameHex: String,
  quantity: BigInt,
  policy: PolicyId
)

final case class AssetOutput(
  outputId: Long,
  outputTxId: Long,
  asset: Asset
)

final case class AssetInput(
  outputId: Long,
  inputTxId: Long,
  asset: Asset
)
