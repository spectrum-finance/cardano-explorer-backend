package io.ergolabs.cardano.explorer.api.v1.endpoints

import io.ergolabs.cardano.explorer.api.configs.RequestConfig
import io.ergolabs.cardano.explorer.api.v1.HttpError
import io.ergolabs.cardano.explorer.api.v1.endpoints.BlocksEndpoints.pathPrefix
import io.ergolabs.cardano.explorer.api.v1.models.{Items, Paging, Transaction}
import io.ergolabs.cardano.explorer.core.types.{Addr, TxHash}
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody

final class TransactionsEndpoints(requestConfig: RequestConfig) {

  val pathPrefix = "transactions"

  def endpoints: List[Endpoint[_, _, _, _]] =
    getByTxHash :: getAll :: getByBlock :: getByAddress :: Nil

  def getByTxHash: Endpoint[TxHash, HttpError, Transaction, Any] =
    baseEndpoint.get
      .in(pathPrefix / path[TxHash].description("Transaction hash"))
      .out(jsonBody[Transaction])
      .tag(pathPrefix)
      .name("Info by tx hash")
      .description("Allow to get info about transaction by transaction hash")

  def getAll: Endpoint[Paging, HttpError, Items[Transaction], Any] =
    baseEndpoint.get
      .in(pathPrefix)
      .in(paging(requestConfig.maxLimitTransactions))
      .out(jsonBody[Items[Transaction]])
      .tag(pathPrefix)
      .name("All transactions")
      .description("Allow to get all transactions with paging")

  def getByBlock: Endpoint[Int, HttpError, Items[Transaction], Any] =
    baseEndpoint.get
      .in(pathPrefix / "byBlockHeight" / path[Int].description("Bloch height"))
      .out(jsonBody[Items[Transaction]])
      .tag(pathPrefix)
      .name("Transactions in block")
      .description("Allow to get info transactions in block by block height")

  def getByAddress: Endpoint[(Addr, Paging), HttpError, Items[Transaction], Any] =
    baseEndpoint.get
      .in(pathPrefix / "byAddress" / path[Addr].description("An address to search by"))
      .in(paging(requestConfig.maxLimitTransactions))
      .out(jsonBody[Items[Transaction]])
      .tag(pathPrefix)
      .name("Transactions by address")
      .description("Allow to get transactions involving a given address by address with paging")
}
