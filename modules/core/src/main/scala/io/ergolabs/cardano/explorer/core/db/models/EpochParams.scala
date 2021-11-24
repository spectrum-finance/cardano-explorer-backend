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
    txFeeFixed: Int,   //todo maybe swap
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
    max_tx_ex_steps: Option[Double],
    max_block_ex_steps: Option[Double],
    max_val_size: Option[Int],
    collateral_percent: Option[Int],
    max_collateral_inputs: Option[Int]
)