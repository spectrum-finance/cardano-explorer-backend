package io.ergolabs.cardano.explorer.api.v1

import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

package object endpoints {

  val baseEndpoint: Endpoint[Unit, HttpError, Unit, Any] =
    endpoint.errorOut(
      oneOf[HttpError](
        oneOfMapping(StatusCode.NotFound, jsonBody[HttpError.NotFound].description("not found")),
        oneOfMapping(StatusCode.NoContent, emptyOutputAs(HttpError.NoContent)),
        oneOfDefaultMapping(jsonBody[HttpError.Unknown].description("unknown"))
      )
    )
}
