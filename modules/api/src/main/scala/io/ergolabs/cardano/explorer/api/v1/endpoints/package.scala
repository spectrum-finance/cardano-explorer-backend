package io.ergolabs.cardano.explorer.api.v1

import cats.syntax.option._
import io.circe.generic.auto._
import io.ergolabs.cardano.explorer.api.v1.models.Paging
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

  def paging: EndpointInput[Paging] = paging(Int.MaxValue)

  def paging(maxLimit: Int): EndpointInput[Paging] =
    (query[Option[Int]]("offset").validateOption(Validator.min(0)) and
      query[Option[Int]]("limit")
        .validateOption(Validator.min(1))
        .validateOption(Validator.max(maxLimit)))
      .map { input =>
        Paging(input._1.getOrElse(0), input._2.getOrElse(20))
      } { case Paging(offset, limit) => offset.some -> limit.some }
}
