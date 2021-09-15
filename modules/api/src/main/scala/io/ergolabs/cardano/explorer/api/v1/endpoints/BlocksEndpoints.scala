package io.ergolabs.cardano.explorer.api.v1.endpoints

import io.ergolabs.cardano.explorer.api.v1.HttpError
import io.ergolabs.cardano.explorer.api.v1.models.{BlockInfo, Transaction}
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody

class BlocksEndpoints {

  val pathPrefix = "blocks"

  def getBestBlockInfo: Endpoint[Unit, HttpError, BlockInfo, Any] =
    baseEndpoint.get
      .in(pathPrefix / "bestBlock")
      .out(jsonBody[BlockInfo])
}
