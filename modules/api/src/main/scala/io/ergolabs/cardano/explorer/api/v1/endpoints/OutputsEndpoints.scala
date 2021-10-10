package io.ergolabs.cardano.explorer.api.v1.endpoints

import io.ergolabs.cardano.explorer.api.v1.HttpError
import io.ergolabs.cardano.explorer.api.v1.models.{Indexing, Items, Paging, TxOutput, UtxoSearch}
import io.ergolabs.cardano.explorer.core.types.{Addr, AssetRef, OutRef}
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody

class OutputsEndpoints {

  val pathPrefix = "outputs"

  def getByOutRef: Endpoint[OutRef, HttpError, TxOutput, Any] =
    baseEndpoint.get
      .in(pathPrefix / path[OutRef])
      .out(jsonBody[TxOutput])

  def getUnspent: Endpoint[Paging, HttpError, Items[TxOutput], Any] =
    baseEndpoint.get
      .in(pathPrefix / "unspent")
      .in(paging)
      .out(jsonBody[Items[TxOutput]])

  def getUnspentIndexed: Endpoint[Indexing, HttpError, Items[TxOutput], Any] =
    baseEndpoint.get
      .in(pathPrefix / "unspent" / "indexed")
      .in(indexing)
      .out(jsonBody[Items[TxOutput]])

  def getUnspentByAddr: Endpoint[(Addr, Paging), HttpError, Items[TxOutput], Any] =
    baseEndpoint.get
      .in(pathPrefix / "unspent" / "addr" / path[Addr])
      .in(paging)
      .out(jsonBody[Items[TxOutput]])

  def getUnspentByAsset: Endpoint[(AssetRef, Paging), HttpError, Items[TxOutput], Any] =
    baseEndpoint.get
      .in(pathPrefix / "unspent" / "asset" / path[AssetRef])
      .in(paging)
      .out(jsonBody[Items[TxOutput]])

  def searchUnspent: Endpoint[(Paging, UtxoSearch), HttpError, Items[TxOutput], Any] =
    baseEndpoint
      .in(pathPrefix  / "unspent"/ "search")
      .in(paging)
      .in(jsonBody[UtxoSearch])
      .out(jsonBody[Items[TxOutput]])
}
