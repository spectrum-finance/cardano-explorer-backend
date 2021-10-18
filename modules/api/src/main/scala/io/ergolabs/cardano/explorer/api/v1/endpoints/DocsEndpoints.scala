package io.ergolabs.cardano.explorer.api.v1.endpoints

import io.ergolabs.cardano.explorer.api.v1.HttpError
import io.ergolabs.cardano.explorer.api.v1.endpoints.BlocksEndpoints.pathPrefix
import io.ergolabs.cardano.explorer.api.v1.models.{BlockInfo, Transaction}
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody

object DocsEndpoints {

  private val PathPrefix = "docs"

  def endpoints: List[Endpoint[_, _, _, _]] = apiSpecDef :: Nil

  def apiSpecDef: Endpoint[Unit, HttpError, String, Any] =
    baseEndpoint
      .in(PathPrefix / "openapi")
      .out(plainBody[String])
      .tag(pathPrefix)
      .name("Openapi route")
      .description("Allow to get openapi.yaml")
}
