package io.ergolabs.cardano.explorer.api.v1.endpoints

import io.ergolabs.cardano.explorer.api.v1.HttpError
import io.ergolabs.cardano.explorer.api.v1.models.AssetInfo
import io.ergolabs.cardano.explorer.core.types.{Asset32, AssetRef}
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody

class AssetsEndpoints {

  val pathPrefix = "assets"

  def getAssetInfo: Endpoint[AssetRef, HttpError, AssetInfo, Any] =
    baseEndpoint.get
      .in(pathPrefix / "getInfo" / path[AssetRef])
      .out(jsonBody[AssetInfo])
}
