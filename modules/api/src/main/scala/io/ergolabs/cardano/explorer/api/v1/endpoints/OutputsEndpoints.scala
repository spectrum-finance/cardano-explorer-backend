package io.ergolabs.cardano.explorer.api.v1.endpoints

import io.ergolabs.cardano.explorer.api.v1.HttpError
import io.ergolabs.cardano.explorer.api.v1.models.{Paging, TxOutput, UtxoSearch}
import io.ergolabs.cardano.explorer.core.types.{Addr, Asset32, OutRef}
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody

class OutputsEndpoints {

  val pathPrefix = "outputs"

  def getByOutRef: Endpoint[OutRef, HttpError, TxOutput, Any] =
    baseEndpoint.get
      .in(pathPrefix / path[OutRef])
      .out(jsonBody[TxOutput])

  def getUnspent: Endpoint[(Addr, Paging), HttpError, List[TxOutput], Any] =
    baseEndpoint
      .in(pathPrefix / "unspentByAddr" / path[Addr])
      .in(paging)
      .out(jsonBody[List[TxOutput]])

  def getUnspentByAssetId: Endpoint[(Asset32, Paging), HttpError, List[TxOutput], Any] =
    baseEndpoint
      .in(pathPrefix / "unspentByAsset" / path[Asset32])
      .in(paging)
      .out(jsonBody[List[TxOutput]])

  def searchUnspent: Endpoint[(Paging, UtxoSearch), HttpError, List[TxOutput], Any] =
    baseEndpoint
      .in(pathPrefix / "searchUnspent")
      .in(paging)
      .in(jsonBody[UtxoSearch])
      .out(jsonBody[List[TxOutput]])

  def searchUnspentUnion: Endpoint[(Paging, UtxoSearch), HttpError, List[TxOutput], Any] =
    baseEndpoint
      .in(pathPrefix / "searchUnspentUnion")
      .in(paging)
      .in(jsonBody[UtxoSearch])
      .out(jsonBody[List[TxOutput]])
}
