package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import sttp.tapir._
import sttp.tapir.json.circe._
import io.circe.generic.semiauto._
import io.ergolabs.cardano.explorer.api.v1.instances._
import io.ergolabs.cardano.explorer.core.db.models.EpochParams
import sttp.tapir.Schema

// From https://github.com/input-output-hk/cardano-node/blob/0a553b572a3e2da9e401886155b1e1451c851901/cardano-api/src/Cardano/Api/ProtocolParameters.hs

@derive(encoder, decoder)
final case class ProtocolParams(
  protocolParamProtocolVersion: (Int, Int),
  protocolParamDecentralization: Double,
  protocolParamExtraPraosEntropy: Option[Int],
  protocolParamMaxBlockHeaderSize: Int,
  protocolParamMaxBlockBodySize: Int,
  protocolParamMaxTxSize: Int,
  protocolParamTxFeeFixed: Int,
  protocolParamTxFeePerByte: Int,
  protocolParamMinUTxOValue: Option[Int],
  protocolParamStakeAddressDeposit: Int,
  protocolParamStakePoolDeposit: Int,
  protocolParamMinPoolCost: Int,
  protocolParamPoolRetireMaxEpoch: Int,
  protocolParamStakePoolTargetNum: Int,
  protocolParamPoolPledgeInfluence: Double,
  protocolParamMonetaryExpansion: Double,
  protocolParamTreasuryCut: Double,
  protocolParamUTxOCostPerWord: Option[Long],
  protocolParamCostModels: Int,
  protocolParamPrices: Option[Double],
  protocolParamMaxTxExUnits: Option[Double],
  protocolParamMaxBlockExUnits: Option[Double],
  protocolParamMaxValueSize: Option[Int],
  protocolParamCollateralPercent: Option[Int],
  protocolParamMaxCollateralInputs: Option[Int]
)

object ProtocolParams {

  implicit val schema: Schema[ProtocolParams] = Schema.derived[ProtocolParams]

  def fromEpochParams(epochParams: EpochParams): ProtocolParams = ProtocolParams(
    protocolParamProtocolVersion     = (epochParams.majorVersion, epochParams.minorVersion),
    protocolParamDecentralization    = epochParams.decentralization,
    protocolParamExtraPraosEntropy   = epochParams.entropy,
    protocolParamMaxBlockHeaderSize  = epochParams.maxBlockHeaderSize,
    protocolParamMaxBlockBodySize    = epochParams.maxBlockSize,
    protocolParamMaxTxSize           = epochParams.maxTxSize,
    protocolParamTxFeeFixed          = epochParams.txFeeFixed,
    protocolParamTxFeePerByte        = epochParams.txFeePerByte,
    protocolParamMinUTxOValue        = epochParams.minUtxoValue,
    protocolParamStakeAddressDeposit = epochParams.keyDeposit,
    protocolParamStakePoolDeposit    = epochParams.poolDeposit,
    protocolParamMinPoolCost         = epochParams.minPoolCost,
    protocolParamPoolRetireMaxEpoch  = epochParams.maxEpoch,
    protocolParamStakePoolTargetNum  = epochParams.optimalPoolCount,
    protocolParamPoolPledgeInfluence = epochParams.influence,
    protocolParamMonetaryExpansion   = epochParams.monetaryExpansion,
    protocolParamTreasuryCut         = epochParams.treasuryGrowthRate,
    protocolParamUTxOCostPerWord     = epochParams.costPerWord,
    protocolParamCostModels          = epochParams.costModelId,
    protocolParamPrices              = epochParams.priceStep,
    protocolParamMaxTxExUnits        = epochParams.maxTxExSteps,
    protocolParamMaxBlockExUnits     = epochParams.maxBlockExSteps,
    protocolParamMaxValueSize        = epochParams.maxValSize,
    protocolParamCollateralPercent   = epochParams.collateralPercent,
    protocolParamMaxCollateralInputs = epochParams.maxCollateralInputs
  )
}
