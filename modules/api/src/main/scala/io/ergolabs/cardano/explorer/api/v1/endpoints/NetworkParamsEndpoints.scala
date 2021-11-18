package io.ergolabs.cardano.explorer.api.v1.endpoints

import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import io.ergolabs.cardano.explorer.api.v1.HttpError
import io.ergolabs.cardano.explorer.api.v1.endpoints.BlocksEndpoints.pathPrefix
import io.ergolabs.cardano.explorer.api.v1.models.NetworkParams

object NetworkParamsEndpoints {
  
  private val PathPrefix = "networkParams"

  def endpoints: List[Endpoint[_, _, _, _]] = networkParamsDef :: Nil

  def networkParamsDef: Endpoint[Unit, HttpError, NetworkParams, Any] =
    baseEndpoint.get
      .in(PathPrefix)
      .out(jsonBody[NetworkParams])
      .tag(pathPrefix)
      .name("Network params")
      .description("Get current network params")
}
