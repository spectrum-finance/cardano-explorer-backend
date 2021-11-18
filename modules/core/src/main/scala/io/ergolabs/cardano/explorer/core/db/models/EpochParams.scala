package io.ergolabs.cardano.explorer.core.db.models

final case class EpochParams(
    epochNo: Int,
    majorVersion: Int,
    minorVersion: Int,
    decentralization: Int,
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
    
)