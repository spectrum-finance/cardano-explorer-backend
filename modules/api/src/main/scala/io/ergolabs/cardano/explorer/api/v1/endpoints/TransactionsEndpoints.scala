package io.ergolabs.cardano.explorer.api.v1.endpoints

import io.ergolabs.cardano.explorer.api.configs.RequestConfig
import io.ergolabs.cardano.explorer.api.v1.HttpError
import io.ergolabs.cardano.explorer.api.v1.models.{Items, Paging, Transaction}
import io.ergolabs.cardano.explorer.core.models.Sorting.SortOrder
import io.ergolabs.cardano.explorer.core.types.{Addr, PaymentCred, TxHash}
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody

final class TransactionsEndpoints[F[_]](conf: RequestConfig) {

  val pathPrefix = "transactions"

  def endpoints: List[Endpoint[_, _, _, _]] =
    streamAll :: getByTxHash :: getAll :: getByBlock :: getByAddress :: getByPCred :: Nil

  def getByTxHash: Endpoint[TxHash, HttpError, Transaction, Any] =
    baseEndpoint.get
      .in(pathPrefix / path[TxHash].description("Transaction hash"))
      .out(jsonBody[Transaction])
      .tag(pathPrefix)
      .name("Info by tx hash")
      .description("Allow to get info about transaction by transaction hash")

  def getAll: Endpoint[(Paging, SortOrder), HttpError, Items[Transaction], Any] =
    baseEndpoint.get
      .in(pathPrefix)
      .in(paging(conf.maxLimitTransactions))
      .in(ordering)
      .out(jsonBody[Items[Transaction]])
      .tag(pathPrefix)
      .name("All transactions")
      .description("Allow to get all transactions with paging")

  def streamAll: Endpoint[(Paging, SortOrder), HttpError, fs2.Stream[F, Byte], Fs2Streams[F]] =
    baseEndpoint.get
      .in(pathPrefix / "stream")
      .in(paging(conf.maxLimitTransactions))
      .in(ordering)
      .out(streamBody(Fs2Streams[F])(Schema.derived[List[Transaction]], CodecFormat.Json(), None))
      .tag(pathPrefix)
      .name("TransactionsStreamAll")
      .description("Stream all transactions")

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
      .in(paging(conf.maxLimitTransactions))
      .out(jsonBody[Items[Transaction]])
      .tag(pathPrefix)
      .name("Transactions by address")
      .description("Allow to get transactions involving a given address with paging")

  def getByPCred: Endpoint[(PaymentCred, Paging, SortOrder), HttpError, List[Transaction], Any] =
    baseEndpoint.get
      .in(pathPrefix / "byPaymentCred" / path[PaymentCred].description("Payment Credential to search by"))
      .in(paging(conf.maxLimitTransactions))
      .in(ordering)
      .out(jsonBody[List[Transaction]])
      .tag(pathPrefix)
      .name("Transactions by Payment Credential")
      .description("Allow to get transactions involving a given Payment Credential with paging")
}
