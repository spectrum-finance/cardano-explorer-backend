package io.ergolabs.cardano.explorer.api.v1.endpoints

import io.ergolabs.cardano.explorer.api.configs.RequestConfig
import io.ergolabs.cardano.explorer.api.v1.HttpError
import io.ergolabs.cardano.explorer.api.v1.endpoints.BlocksEndpoints.pathPrefix
import io.ergolabs.cardano.explorer.api.v1.models.{Indexing, Items, Paging, TxOutput, UtxoSearch}
import io.ergolabs.cardano.explorer.core.models.Sorting.SortOrder
import io.ergolabs.cardano.explorer.core.types.{Addr, AssetRef, OutRef, PaymentCred}
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody

final class OutputsEndpoints(conf: RequestConfig) {

  val pathPrefix = "outputs"

  def endpoints: List[Endpoint[_, _, _, _]] =
    getByOutRef ::
    getUnspent ::
    getUnspentIndexed ::
    getUnspentByAddr ::
    getUnspentByPCred ::
    getUnspentByAsset ::
    searchUnspent ::
    Nil

  def getByOutRef: Endpoint[OutRef, HttpError, TxOutput, Any] =
    baseEndpoint.get
      .in(pathPrefix / path[OutRef].description("Output reference"))
      .out(jsonBody[TxOutput])
      .tag(pathPrefix)
      .name("Info by reference")
      .description("Allow to get info about output by reference")

  def getUnspent: Endpoint[(Paging, SortOrder), HttpError, Items[TxOutput], Any] =
    baseEndpoint.get
      .in(pathPrefix / "unspent")
      .in(paging(conf.maxLimitOutputs))
      .in(ordering)
      .out(jsonBody[Items[TxOutput]])
      .tag(pathPrefix)
      .name("Unspent outputs with paging")
      .description("Allow to get info about unspent outputs with paging")

  def getUnspentIndexed: Endpoint[(Indexing, SortOrder), HttpError, Items[TxOutput], Any] =
    baseEndpoint.get
      .in(pathPrefix / "unspent" / "indexed")
      .in(indexing)
      .in(ordering)
      .out(jsonBody[Items[TxOutput]])
      .tag(pathPrefix)
      .name("Unspent outputs with indexing")
      .description("Allow to get info about unspent outputs with indexing")

  def getUnspentByAddr: Endpoint[(Addr, Paging), HttpError, Items[TxOutput], Any] =
    baseEndpoint.get
      .in(pathPrefix / "unspent" / "byAddr" / path[Addr].description("An address to search by"))
      .in(paging(conf.maxLimitOutputs))
      .out(jsonBody[Items[TxOutput]])
      .tag(pathPrefix)
      .name("Unspent outputs by address")
      .description("Query unspent outputs by address")

  def getUnspentByPCred: Endpoint[(PaymentCred, Paging, SortOrder), HttpError, Items[TxOutput], Any] =
    baseEndpoint.get
      .in(pathPrefix / "unspent" / "byPaymentCred" / path[PaymentCred].description("A payment credential to search by"))
      .in(paging(conf.maxLimitOutputs))
      .in(ordering)
      .out(jsonBody[Items[TxOutput]])
      .tag(pathPrefix)
      .name("Unspent outputs by PaymentCred")
      .description("Query unspent outputs by payment credential")

  def getUnspentByAsset: Endpoint[(AssetRef, Paging, SortOrder), HttpError, Items[TxOutput], Any] =
    baseEndpoint.get
      .in(pathPrefix / "unspent" / "byAsset" / path[AssetRef].description("Asset reference"))
      .in(paging(conf.maxLimitOutputs))
      .in(ordering)
      .out(jsonBody[Items[TxOutput]])
      .tag(pathPrefix)
      .name("Unspent outputs with assets")
      .description("Allow to get info about unspent outputs by assetRef with paging")

  def searchUnspent: Endpoint[(Paging, UtxoSearch), HttpError, Items[TxOutput], Any] =
    baseEndpoint.post
      .in(pathPrefix / "unspent" / "search")
      .in(paging(conf.maxLimitOutputs))
      .in(jsonBody[UtxoSearch])
      .out(jsonBody[Items[TxOutput]])
      .tag(pathPrefix)
      .name("Unspent outputs by search")
      .description("Allow to get info about unspent outputs by utxoSearch with paging")
}
