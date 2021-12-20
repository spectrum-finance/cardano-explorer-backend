package io.ergolabs.cardano.explorer.api.v1.endpoints

import io.ergolabs.cardano.explorer.api.v1.HttpError
import io.ergolabs.cardano.explorer.api.v1.models.AssetInfo
import io.ergolabs.cardano.explorer.core.types.{Asset32, AssetRef}
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody

object AssetsEndpoints {

  val pathPrefix = "assets"

  def endpoints: List[Endpoint[_, _, _, _]] = getAssetInfo :: Nil

  def getAssetInfo: Endpoint[AssetRef, HttpError, AssetInfo, Any] =
    baseEndpoint.get
      .in(pathPrefix / "info" / path[AssetRef].description("Asset reference"))
      .out(jsonBody[AssetInfo])
      .tag(pathPrefix)
      .name("Info by reference")
      .description("Allow to get info about asset by asset ref")
}
