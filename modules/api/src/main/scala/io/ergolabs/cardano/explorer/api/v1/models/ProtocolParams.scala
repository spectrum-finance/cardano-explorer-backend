package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.ergolabs.cardano.explorer.core.db.models.EpochParams
import sttp.tapir.Schema
// From https://github.com/input-output-hk/cardano-node/blob/0a553b572a3e2da9e401886155b1e1451c851901/cardano-api/src/Cardano/Api/ProtocolParameters.hs

@derive(encoder, decoder)
final case class ProtocolParams(
  protocolVersion: ProtocolVersion,
  decentralization: Double,
  extraPraosEntropy: Option[Int],
  maxBlockHeaderSize: Int,
  maxBlockBodySize: Int,
  maxTxSize: Int,
  txFeeFixed: Int,
  txFeePerByte: Int,
  minUTxOValue: Option[Int],
  stakeAddressDeposit: Int,
  stakePoolDeposit: Int,
  minPoolCost: Int,
  poolRetireMaxEpoch: Int,
  stakePoolTargetNum: Int,
  poolPledgeInfluence: Double,
  monetaryExpansion: Double,
  treasuryCut: Double,
  coinsPerUtxoByte: Option[Long],
  costModels: Map[String, Map[String, Long]],
  executionUnitPrices: ExecutionUnitPrices,
  maxTxExecutionUnits: ExecutionUnits,
  maxBlockExecutionUnits: ExecutionUnits,
  maxValueSize: Option[Int],
  collateralPercentage: Option[Int],
  maxCollateralInputs: Option[Int]
)

object ProtocolParams {

  implicit val schema: Schema[ProtocolParams] = Schema.derived[ProtocolParams]

  def fromEpochParams(epochParams: EpochParams, costModelsMap: Map[String, Map[String, Long]]): ProtocolParams =
    ProtocolParams(
      protocolVersion        = ProtocolVersion(epochParams.majorVersion, epochParams.minorVersion),
      decentralization       = epochParams.decentralization,
      extraPraosEntropy      = epochParams.extraEntropy,
      maxBlockHeaderSize     = epochParams.maxBlockHeaderSize,
      maxBlockBodySize       = epochParams.maxBlockSize,
      maxTxSize              = epochParams.maxTxSize,
      txFeeFixed             = epochParams.txFeeFixed,
      txFeePerByte           = epochParams.txFeePerByte,
      minUTxOValue           = epochParams.minUtxoValue,
      stakeAddressDeposit    = epochParams.keyDeposit,
      stakePoolDeposit       = epochParams.poolDeposit,
      minPoolCost            = epochParams.minPoolCost,
      poolRetireMaxEpoch     = epochParams.maxEpoch,
      stakePoolTargetNum     = epochParams.optimalPoolCount,
      poolPledgeInfluence    = epochParams.influence,
      monetaryExpansion      = epochParams.monetaryExpansion,
      treasuryCut            = epochParams.treasuryGrowthRate,
      coinsPerUtxoByte       = epochParams.coinsPerUtxoByte,
      costModels             = costModelsMap,
      executionUnitPrices    = ExecutionUnitPrices(epochParams.priceStep, epochParams.priceMemory),
      maxTxExecutionUnits    = ExecutionUnits(epochParams.maxTxExSteps, epochParams.maxTxExMem),
      maxBlockExecutionUnits = ExecutionUnits(epochParams.maxBlockExSteps, epochParams.maxBlockExMem),
      maxValueSize           = epochParams.maxValSize,
      collateralPercentage   = epochParams.collateralPercent,
      maxCollateralInputs    = epochParams.maxCollateralInputs
    )
}
