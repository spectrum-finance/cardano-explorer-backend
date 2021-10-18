package io.ergolabs.cardano.explorer.api.v1.endpoints

import io.ergolabs.cardano.explorer.api.v1.HttpError
import io.ergolabs.cardano.explorer.api.v1.models.{BlockInfo, Transaction}
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody

object BlocksEndpoints {

  val pathPrefix = "blocks"

  def endpoints: List[Endpoint[_, _, _, _]] = getBestBlockInfo :: Nil

  def getBestBlockInfo: Endpoint[Unit, HttpError, BlockInfo, Any] =
    baseEndpoint.get
      .in(pathPrefix / "bestBlock")
      .out(jsonBody[BlockInfo])
      .tag(pathPrefix)
      .name("Current best block")
      .description("Allow to get info about best block")
}
