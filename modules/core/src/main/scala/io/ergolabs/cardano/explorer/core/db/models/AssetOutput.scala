package io.ergolabs.cardano.explorer.core.db.models

import io.ergolabs.cardano.explorer.core.types.{Asset32, PolicyId}

final case class Asset(
  name: Asset32,
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
  inputTxId: Option[Long],
  asset: Asset
)
