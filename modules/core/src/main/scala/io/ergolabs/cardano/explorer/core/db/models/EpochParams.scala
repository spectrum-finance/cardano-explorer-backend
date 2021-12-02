package io.ergolabs.cardano.explorer.core.db.models

final case class EpochParams(
  epochNo: Int,
  majorVersion: Int,
  minorVersion: Int,
  decentralization: Double,
  entropy: Option[Int],
  maxBlockHeaderSize: Int,
  maxBlockSize: Int,
  maxTxSize: Int,
  txFeeFixed: Int, //todo maybe swap
  txFeePerByte: Int, //todo maybe swap
  minUtxoValue: Option[Int],
  keyDeposit: Int,
  poolDeposit: Int,
  minPoolCost: Int,
  maxEpoch: Int,
  optimalPoolCount: Int,
  influence: Double,
  monetaryExpansion: Double,
  treasuryGrowthRate: Double,
  costPerWord: Option[Long],
  costModelId: Int,
  priceStep: Option[Double],
  maxTxExSteps: Option[Double],
  maxBlockExSteps: Option[Double],
  maxValSize: Option[Int],
  collateralPercent: Option[Int],
  maxCollateralInputs: Option[Int]
)
