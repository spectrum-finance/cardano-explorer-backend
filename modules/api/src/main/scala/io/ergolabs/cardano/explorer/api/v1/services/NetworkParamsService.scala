package io.ergolabs.cardano.explorer.api.v1.services

import cats.Monad
import cats.syntax.option._
import cats.syntax.either._
import io.ergolabs.cardano.explorer.core.db.repositories.RepoBundle
import mouse.anyf._
import tofu.doobie.LiftConnectionIO
import tofu.doobie.transactor.Txr
import tofu.syntax.monadic._
import io.ergolabs.cardano.explorer.api.v1.models.{EnvParams, NetworkName, ProtocolParams, SystemStart}
import io.ergolabs.cardano.explorer.core.types.PoolId
import tofu.Throws
import tofu.syntax.raise._
import io.circe.parser

trait NetworkParamsService[F[_]] {

  def getNetworkParams: F[EnvParams]
}

object NetworkParamsService {

  val PlutusV1 = "PlutusV1"
  val PlutusV2 = "PlutusV2"
  val PlutusScriptV1 = "PlutusScriptV1"
  val PlutusScriptV2 = "PlutusScriptV2"
  val UnknownPlutusScriptV = "UnknownPlutusScriptV"

  //todo: remove after tests
  val constantParams = """{
                         |    "PlutusV1": {
                         |                "chooseUnit-cpu-arguments": 46417,
                         |                "appendByteString-memory-arguments-slope": 1,
                         |                "lessThanEqualsByteString-memory-arguments": 1,
                         |                "mapData-memory-arguments": 32,
                         |                "equalsData-cpu-arguments-intercept": 1060367,
                         |                "consByteString-memory-arguments-intercept": 0,
                         |                "appendByteString-memory-arguments-intercept": 0,
                         |                "lessThanEqualsByteString-cpu-arguments-slope": 156,
                         |                "equalsString-memory-arguments": 1,
                         |                "modInteger-memory-arguments-minimum": 1,
                         |                "sliceByteString-memory-arguments-slope": 0,
                         |                "nullList-memory-arguments": 32,
                         |                "consByteString-cpu-arguments-slope": 511,
                         |                "lengthOfByteString-cpu-arguments": 1000,
                         |                "appendByteString-cpu-arguments-intercept": 1000,
                         |                "cekForceCost-exBudgetMemory": 100,
                         |                "ifThenElse-cpu-arguments": 80556,
                         |                "consByteString-memory-arguments-slope": 1,
                         |                "lengthOfByteString-memory-arguments": 10,
                         |                "mkCons-cpu-arguments": 65493,
                         |                "multiplyInteger-cpu-arguments-slope": 11687,
                         |                "appendByteString-cpu-arguments-slope": 571,
                         |                "cekStartupCost-exBudgetMemory": 100,
                         |                "tailList-cpu-arguments": 41182,
                         |                "modInteger-cpu-arguments-model-arguments-slope": 220,
                         |                "cekLamCost-exBudgetMemory": 100,
                         |                "sndPair-memory-arguments": 32,
                         |                "sha3_256-memory-arguments": 4,
                         |                "chooseList-memory-arguments": 32,
                         |                "constrData-memory-arguments": 32,
                         |                "modInteger-memory-arguments-slope": 1,
                         |                "iData-memory-arguments": 32,
                         |                "chooseData-cpu-arguments": 19537,
                         |                "cekConstCost-exBudgetCPU": 23000,
                         |                "mkCons-memory-arguments": 32,
                         |                "encodeUtf8-memory-arguments-slope": 2,
                         |                "appendString-cpu-arguments-intercept": 1000,
                         |                "sliceByteString-memory-arguments-intercept": 4,
                         |                "unListData-cpu-arguments": 32247,
                         |                "headList-cpu-arguments": 43249,
                         |                "chooseUnit-memory-arguments": 4,
                         |                "lessThanInteger-cpu-arguments-slope": 511,
                         |                "cekConstCost-exBudgetMemory": 100,
                         |                "decodeUtf8-cpu-arguments-intercept": 497525,
                         |                "quotientInteger-cpu-arguments-constant": 196500,
                         |                "decodeUtf8-cpu-arguments-slope": 14068,
                         |                "subtractInteger-cpu-arguments-slope": 812,
                         |                "remainderInteger-memory-arguments-minimum": 1,
                         |                "sliceByteString-cpu-arguments-intercept": 265318,
                         |                "remainderInteger-cpu-arguments-model-arguments-slope": 220,
                         |                "sliceByteString-cpu-arguments-slope": 0,
                         |                "divideInteger-memory-arguments-minimum": 1,
                         |                "quotientInteger-memory-arguments-slope": 1,
                         |                "unBData-cpu-arguments": 31220,
                         |                "blake2b_256-cpu-arguments-intercept": 117366,
                         |                "fstPair-cpu-arguments": 80436,
                         |                "appendString-memory-arguments-intercept": 4,
                         |                "modInteger-memory-arguments-intercept": 0,
                         |                "cekVarCost-exBudgetMemory": 100,
                         |                "listData-cpu-arguments": 52467,
                         |                "unMapData-cpu-arguments": 38314,
                         |                "encodeUtf8-memory-arguments-intercept": 4,
                         |                "lessThanEqualsInteger-cpu-arguments-slope": 473,
                         |                "consByteString-cpu-arguments-intercept": 221973,
                         |                "nullList-cpu-arguments": 60091,
                         |                "encodeUtf8-cpu-arguments-slope": 28662,
                         |                "listData-memory-arguments": 32,
                         |                "equalsString-cpu-arguments-intercept": 1000,
                         |                "modInteger-cpu-arguments-model-arguments-intercept": 453240,
                         |                "sha3_256-cpu-arguments-intercept": 1927926,
                         |                "lessThanByteString-cpu-arguments-slope": 156,
                         |                "decodeUtf8-memory-arguments-intercept": 4,
                         |                "cekVarCost-exBudgetCPU": 23000,
                         |                "quotientInteger-memory-arguments-minimum": 1,
                         |                "divideInteger-cpu-arguments-constant": 196500,
                         |                "sndPair-cpu-arguments": 85931,
                         |                "unListData-memory-arguments": 32,
                         |                "equalsByteString-cpu-arguments-slope": 62,
                         |                "mkPairData-memory-arguments": 32,
                         |                "fstPair-memory-arguments": 32,
                         |                "equalsInteger-cpu-arguments-slope": 421,
                         |                "quotientInteger-cpu-arguments-model-arguments-intercept": 453240,
                         |                "remainderInteger-cpu-arguments-model-arguments-intercept": 453240,
                         |                "quotientInteger-memory-arguments-intercept": 0,
                         |                "subtractInteger-memory-arguments-slope": 1,
                         |                "unConstrData-cpu-arguments": 32696,
                         |                "sha3_256-cpu-arguments-slope": 82523,
                         |                "remainderInteger-cpu-arguments-constant": 196500,
                         |                "lessThanByteString-cpu-arguments-intercept": 197145,
                         |                "appendString-cpu-arguments-slope": 24177,
                         |                "divideInteger-memory-arguments-intercept": 0,
                         |                "mkNilPairData-cpu-arguments": 16563,
                         |                "lessThanEqualsByteString-cpu-arguments-intercept": 197145,
                         |                "unIData-memory-arguments": 32,
                         |                "blake2b_256-cpu-arguments-slope": 10475,
                         |                "indexByteString-cpu-arguments": 57667,
                         |                "addInteger-memory-arguments-slope": 1,
                         |                "equalsByteString-memory-arguments": 1,
                         |                "unConstrData-memory-arguments": 32,
                         |                "lessThanInteger-memory-arguments": 1,
                         |                "verifyEd25519Signature-cpu-arguments-slope": 18975,
                         |                "equalsByteString-cpu-arguments-intercept": 216773,
                         |                "multiplyInteger-memory-arguments-slope": 1,
                         |                "multiplyInteger-cpu-arguments-intercept": 69522,
                         |                "bData-memory-arguments": 32,
                         |                "cekDelayCost-exBudgetCPU": 23000,
                         |                "equalsData-cpu-arguments-slope": 12586,
                         |                "equalsData-memory-arguments": 1,
                         |                "addInteger-cpu-arguments-intercept": 205665,
                         |                "subtractInteger-cpu-arguments-intercept": 205665,
                         |                "cekApplyCost-exBudgetMemory": 100,
                         |                "remainderInteger-memory-arguments-slope": 1,
                         |                "unMapData-memory-arguments": 32,
                         |                "multiplyInteger-memory-arguments-intercept": 0,
                         |                "unBData-memory-arguments": 32,
                         |                "cekBuiltinCost-exBudgetCPU": 23000,
                         |                "equalsInteger-memory-arguments": 1,
                         |                "indexByteString-memory-arguments": 4,
                         |                "cekBuiltinCost-exBudgetMemory": 100,
                         |                "decodeUtf8-memory-arguments-slope": 2,
                         |                "verifyEd25519Signature-cpu-arguments-intercept": 57996947,
                         |                "trace-memory-arguments": 32,
                         |                "encodeUtf8-cpu-arguments-intercept": 1000,
                         |                "chooseList-cpu-arguments": 175354,
                         |                "lessThanInteger-cpu-arguments-intercept": 208896,
                         |                "lessThanEqualsInteger-cpu-arguments-intercept": 204924,
                         |                "subtractInteger-memory-arguments-intercept": 1,
                         |                "lessThanEqualsInteger-memory-arguments": 1,
                         |                "sha2_256-cpu-arguments-slope": 30482,
                         |                "bData-cpu-arguments": 1000,
                         |                "appendString-memory-arguments-slope": 1,
                         |                "tailList-memory-arguments": 32,
                         |                "cekDelayCost-exBudgetMemory": 100,
                         |                "remainderInteger-memory-arguments-intercept": 0,
                         |                "ifThenElse-memory-arguments": 1,
                         |                "addInteger-cpu-arguments-slope": 812,
                         |                "mkNilData-cpu-arguments": 22558,
                         |                "divideInteger-memory-arguments-slope": 1,
                         |                "mkNilData-memory-arguments": 32,
                         |                "cekLamCost-exBudgetCPU": 23000,
                         |                "divideInteger-cpu-arguments-model-arguments-intercept": 453240,
                         |                "constrData-cpu-arguments": 89141,
                         |                "modInteger-cpu-arguments-constant": 196500,
                         |                "addInteger-memory-arguments-intercept": 1,
                         |                "cekApplyCost-exBudgetCPU": 23000,
                         |                "quotientInteger-cpu-arguments-model-arguments-slope": 220,
                         |                "cekStartupCost-exBudgetCPU": 100,
                         |                "lessThanByteString-memory-arguments": 1,
                         |                "blake2b_256-memory-arguments": 4,
                         |                "equalsString-cpu-arguments-slope": 52998,
                         |                "iData-cpu-arguments": 1000,
                         |                "mapData-cpu-arguments": 64832,
                         |                "trace-cpu-arguments": 212342,
                         |                "mkPairData-cpu-arguments": 76511,
                         |                "headList-memory-arguments": 32,
                         |                "chooseData-memory-arguments": 32,
                         |                "cekForceCost-exBudgetCPU": 23000,
                         |                "mkNilPairData-memory-arguments": 32,
                         |                "equalsString-cpu-arguments-constant": 187000,
                         |                "sha2_256-memory-arguments": 4,
                         |                "equalsInteger-cpu-arguments-intercept": 208512,
                         |                "divideInteger-cpu-arguments-model-arguments-slope": 220,
                         |                "verifyEd25519Signature-memory-arguments": 10,
                         |                "equalsByteString-cpu-arguments-constant": 245000,
                         |                "sha2_256-cpu-arguments-intercept": 806990,
                         |                "unIData-cpu-arguments": 43357
                         |            },
                         |            "PlutusV2": {
                         |                "chooseUnit-cpu-arguments": 46417,
                         |                "appendByteString-memory-arguments-slope": 1,
                         |                "lessThanEqualsByteString-memory-arguments": 1,
                         |                "mapData-memory-arguments": 32,
                         |                "equalsData-cpu-arguments-intercept": 1060367,
                         |                "consByteString-memory-arguments-intercept": 0,
                         |                "appendByteString-memory-arguments-intercept": 0,
                         |                "lessThanEqualsByteString-cpu-arguments-slope": 156,
                         |                "equalsString-memory-arguments": 1,
                         |                "modInteger-memory-arguments-minimum": 1,
                         |                "sliceByteString-memory-arguments-slope": 0,
                         |                "nullList-memory-arguments": 32,
                         |                "consByteString-cpu-arguments-slope": 511,
                         |                "lengthOfByteString-cpu-arguments": 1000,
                         |                "appendByteString-cpu-arguments-intercept": 1000,
                         |                "cekForceCost-exBudgetMemory": 100,
                         |                "ifThenElse-cpu-arguments": 80556,
                         |                "consByteString-memory-arguments-slope": 1,
                         |                "lengthOfByteString-memory-arguments": 10,
                         |                "mkCons-cpu-arguments": 65493,
                         |                "multiplyInteger-cpu-arguments-slope": 11687,
                         |                "cekStartupCost-exBudgetMemory": 100,
                         |                "tailList-cpu-arguments": 41182,
                         |                "appendByteString-cpu-arguments-slope": 571,
                         |                "serialiseData-cpu-arguments-intercept": 1159724,
                         |                "modInteger-cpu-arguments-model-arguments-slope": 220,
                         |                "cekLamCost-exBudgetMemory": 100,
                         |                "sndPair-memory-arguments": 32,
                         |                "sha3_256-memory-arguments": 4,
                         |                "chooseList-memory-arguments": 32,
                         |                "constrData-memory-arguments": 32,
                         |                "modInteger-memory-arguments-slope": 1,
                         |                "iData-memory-arguments": 32,
                         |                "chooseData-cpu-arguments": 19537,
                         |                "cekConstCost-exBudgetCPU": 23000,
                         |                "mkCons-memory-arguments": 32,
                         |                "encodeUtf8-memory-arguments-slope": 2,
                         |                "appendString-cpu-arguments-intercept": 1000,
                         |                "sliceByteString-memory-arguments-intercept": 4,
                         |                "unListData-cpu-arguments": 32247,
                         |                "headList-cpu-arguments": 43249,
                         |                "chooseUnit-memory-arguments": 4,
                         |                "lessThanInteger-cpu-arguments-slope": 511,
                         |                "cekConstCost-exBudgetMemory": 100,
                         |                "serialiseData-memory-arguments-intercept": 0,
                         |                "decodeUtf8-cpu-arguments-intercept": 497525,
                         |                "quotientInteger-cpu-arguments-constant": 196500,
                         |                "decodeUtf8-cpu-arguments-slope": 14068,
                         |                "subtractInteger-cpu-arguments-slope": 812,
                         |                "remainderInteger-memory-arguments-minimum": 1,
                         |                "sliceByteString-cpu-arguments-intercept": 265318,
                         |                "remainderInteger-cpu-arguments-model-arguments-slope": 220,
                         |                "sliceByteString-cpu-arguments-slope": 0,
                         |                "divideInteger-memory-arguments-minimum": 1,
                         |                "quotientInteger-memory-arguments-slope": 1,
                         |                "unBData-cpu-arguments": 31220,
                         |                "blake2b_256-cpu-arguments-intercept": 117366,
                         |                "fstPair-cpu-arguments": 80436,
                         |                "appendString-memory-arguments-intercept": 4,
                         |                "modInteger-memory-arguments-intercept": 0,
                         |                "cekVarCost-exBudgetMemory": 100,
                         |                "listData-cpu-arguments": 52467,
                         |                "unMapData-cpu-arguments": 38314,
                         |                "encodeUtf8-memory-arguments-intercept": 4,
                         |                "lessThanEqualsInteger-cpu-arguments-slope": 473,
                         |                "consByteString-cpu-arguments-intercept": 221973,
                         |                "nullList-cpu-arguments": 60091,
                         |                "encodeUtf8-cpu-arguments-slope": 28662,
                         |                "listData-memory-arguments": 32,
                         |                "equalsString-cpu-arguments-intercept": 1000,
                         |                "modInteger-cpu-arguments-model-arguments-intercept": 453240,
                         |                "sha3_256-cpu-arguments-intercept": 1927926,
                         |                "lessThanByteString-cpu-arguments-slope": 156,
                         |                "decodeUtf8-memory-arguments-intercept": 4,
                         |                "cekVarCost-exBudgetCPU": 23000,
                         |                "quotientInteger-memory-arguments-minimum": 1,
                         |                "divideInteger-cpu-arguments-constant": 196500,
                         |                "sndPair-cpu-arguments": 85931,
                         |                "unListData-memory-arguments": 32,
                         |                "equalsByteString-cpu-arguments-slope": 62,
                         |                "mkPairData-memory-arguments": 32,
                         |                "verifySchnorrSecp256k1Signature-cpu-arguments-slope": 32947,
                         |                "fstPair-memory-arguments": 32,
                         |                "equalsInteger-cpu-arguments-slope": 421,
                         |                "quotientInteger-cpu-arguments-model-arguments-intercept": 453240,
                         |                "remainderInteger-cpu-arguments-model-arguments-intercept": 453240,
                         |                "quotientInteger-memory-arguments-intercept": 0,
                         |                "subtractInteger-memory-arguments-slope": 1,
                         |                "unConstrData-cpu-arguments": 32696,
                         |                "sha3_256-cpu-arguments-slope": 82523,
                         |                "remainderInteger-cpu-arguments-constant": 196500,
                         |                "lessThanByteString-cpu-arguments-intercept": 197145,
                         |                "appendString-cpu-arguments-slope": 24177,
                         |                "divideInteger-memory-arguments-intercept": 0,
                         |                "mkNilPairData-cpu-arguments": 16563,
                         |                "lessThanEqualsByteString-cpu-arguments-intercept": 197145,
                         |                "unIData-memory-arguments": 32,
                         |                "blake2b_256-cpu-arguments-slope": 10475,
                         |                "indexByteString-cpu-arguments": 57667,
                         |                "addInteger-memory-arguments-slope": 1,
                         |                "equalsByteString-memory-arguments": 1,
                         |                "lessThanInteger-memory-arguments": 1,
                         |                "verifyEcdsaSecp256k1Signature-memory-arguments": 10,
                         |                "unConstrData-memory-arguments": 32,
                         |                "verifyEd25519Signature-cpu-arguments-slope": 18975,
                         |                "equalsByteString-cpu-arguments-intercept": 216773,
                         |                "multiplyInteger-memory-arguments-slope": 1,
                         |                "multiplyInteger-cpu-arguments-intercept": 69522,
                         |                "bData-memory-arguments": 32,
                         |                "verifyEcdsaSecp256k1Signature-cpu-arguments": 35892428,
                         |                "cekDelayCost-exBudgetCPU": 23000,
                         |                "equalsData-cpu-arguments-slope": 12586,
                         |                "equalsData-memory-arguments": 1,
                         |                "addInteger-cpu-arguments-intercept": 205665,
                         |                "subtractInteger-cpu-arguments-intercept": 205665,
                         |                "cekApplyCost-exBudgetMemory": 100,
                         |                "remainderInteger-memory-arguments-slope": 1,
                         |                "verifySchnorrSecp256k1Signature-cpu-arguments-intercept": 38887044,
                         |                "unMapData-memory-arguments": 32,
                         |                "multiplyInteger-memory-arguments-intercept": 0,
                         |                "unBData-memory-arguments": 32,
                         |                "cekBuiltinCost-exBudgetCPU": 23000,
                         |                "equalsInteger-memory-arguments": 1,
                         |                "verifySchnorrSecp256k1Signature-memory-arguments": 10,
                         |                "cekBuiltinCost-exBudgetMemory": 100,
                         |                "decodeUtf8-memory-arguments-slope": 2,
                         |                "indexByteString-memory-arguments": 4,
                         |                "serialiseData-memory-arguments-slope": 2,
                         |                "verifyEd25519Signature-cpu-arguments-intercept": 57996947,
                         |                "trace-memory-arguments": 32,
                         |                "encodeUtf8-cpu-arguments-intercept": 1000,
                         |                "chooseList-cpu-arguments": 175354,
                         |                "lessThanInteger-cpu-arguments-intercept": 208896,
                         |                "lessThanEqualsInteger-cpu-arguments-intercept": 204924,
                         |                "subtractInteger-memory-arguments-intercept": 1,
                         |                "lessThanEqualsInteger-memory-arguments": 1,
                         |                "sha2_256-cpu-arguments-slope": 30482,
                         |                "bData-cpu-arguments": 1000,
                         |                "appendString-memory-arguments-slope": 1,
                         |                "tailList-memory-arguments": 32,
                         |                "cekDelayCost-exBudgetMemory": 100,
                         |                "remainderInteger-memory-arguments-intercept": 0,
                         |                "ifThenElse-memory-arguments": 1,
                         |                "addInteger-cpu-arguments-slope": 812,
                         |                "mkNilData-cpu-arguments": 22558,
                         |                "divideInteger-memory-arguments-slope": 1,
                         |                "mkNilData-memory-arguments": 32,
                         |                "serialiseData-cpu-arguments-slope": 392670,
                         |                "cekLamCost-exBudgetCPU": 23000,
                         |                "divideInteger-cpu-arguments-model-arguments-intercept": 453240,
                         |                "constrData-cpu-arguments": 89141,
                         |                "modInteger-cpu-arguments-constant": 196500,
                         |                "addInteger-memory-arguments-intercept": 1,
                         |                "cekApplyCost-exBudgetCPU": 23000,
                         |                "quotientInteger-cpu-arguments-model-arguments-slope": 220,
                         |                "cekStartupCost-exBudgetCPU": 100,
                         |                "lessThanByteString-memory-arguments": 1,
                         |                "blake2b_256-memory-arguments": 4,
                         |                "equalsString-cpu-arguments-slope": 52998,
                         |                "iData-cpu-arguments": 1000,
                         |                "mapData-cpu-arguments": 64832,
                         |                "trace-cpu-arguments": 212342,
                         |                "mkPairData-cpu-arguments": 76511,
                         |                "headList-memory-arguments": 32,
                         |                "chooseData-memory-arguments": 32,
                         |                "cekForceCost-exBudgetCPU": 23000,
                         |                "mkNilPairData-memory-arguments": 32,
                         |                "equalsString-cpu-arguments-constant": 187000,
                         |                "sha2_256-memory-arguments": 4,
                         |                "equalsInteger-cpu-arguments-intercept": 208512,
                         |                "divideInteger-cpu-arguments-model-arguments-slope": 220,
                         |                "verifyEd25519Signature-memory-arguments": 10,
                         |                "equalsByteString-cpu-arguments-constant": 245000,
                         |                "sha2_256-cpu-arguments-intercept": 806990,
                         |                "unIData-cpu-arguments": 43357
                         |            }
                         |}""".stripMargin

  def make[F[_], D[_]: Monad: LiftConnectionIO: Throws](implicit
    txr: Txr[F, D],
    repos: RepoBundle[D]
  ): NetworkParamsService[F] = new Live[F, D](txr, repos)

  final class Live[F[_], D[_]: Monad: Throws](txr: Txr[F, D], repos: RepoBundle[D]) extends NetworkParamsService[F] {

    def getNetworkParams: F[EnvParams] =
      (for {
        meta            <- repos.network.getMeta
        epochParams     <- repos.network.getLastEpochParams
        //costModel       <- repos.network.getCostModel(epochParams.costModelId)
        parsedCm        <- parser.parse(constantParams).toRaise
        transformed     <- parsedCm.as[Map[String, Map[String, Long]]].toRaise
        _ <- Monad[D].pure(println(transformed))
        cmCorrectFormat = transformed.map {
          case (pv1, v) if pv1 == PlutusV1 => PlutusScriptV1 -> v
          case (pv2, v) if pv2 == PlutusV2 => PlutusScriptV2 -> v
          case (pvU, v) => s"UnknownPlutusScriptV: $pvU" -> v
        }
      } yield 
          EnvParams(
            ProtocolParams.fromEpochParams(epochParams, cmCorrectFormat),
            NetworkName(meta.networkName),
            SystemStart.fromExplorer(meta.startTime),
            epochParams.collateralPercent
          )
        ) ||> txr.trans

  }
}
