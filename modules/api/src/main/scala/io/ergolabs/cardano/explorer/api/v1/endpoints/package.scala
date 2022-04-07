package io.ergolabs.cardano.explorer.api.v1

import cats.syntax.option._
import io.circe.generic.auto._
import io.ergolabs.cardano.explorer.api.v1.models.{Indexing, Paging}
import io.ergolabs.cardano.explorer.core.models.Sorting
import io.ergolabs.cardano.explorer.core.models.Sorting.SortOrder
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

package object endpoints {

  val V1Prefix: EndpointInput[Unit] = "v1"

  val baseEndpoint: Endpoint[Unit, HttpError, Unit, Any] =
    endpoint
      .in(V1Prefix)
      .errorOut(
        oneOf[HttpError](
          oneOfMapping(StatusCode.NotFound, jsonBody[HttpError.NotFound].description("not found")),
          oneOfMapping(StatusCode.NoContent, emptyOutputAs(HttpError.NoContent)),
          oneOfDefaultMapping(jsonBody[HttpError.Unknown].description("unknown"))
        )
      )

  def paging(maxLimit: Int): EndpointInput[Paging] =
    (query[Option[Int]]("offset").validateOption(Validator.min(0)) and
      query[Option[Int]]("limit")
        .validateOption(Validator.min(1))
        .validateOption(Validator.max(maxLimit)))
      .map { input =>
        Paging(input._1.getOrElse(0), input._2.getOrElse(20))
      } { case Paging(offset, limit) => offset.some -> limit.some }

  def indexing: EndpointInput[Indexing] = indexing(Int.MaxValue)

  def indexing(maxLimit: Int): EndpointInput[Indexing] =
    (query[Option[Int]]("minIndex").validateOption(Validator.min(0)) and
      query[Option[Int]]("limit")
        .validateOption(Validator.min(1))
        .validateOption(Validator.max(maxLimit)))
      .map { input =>
        Indexing(input._1.getOrElse(0), input._2.getOrElse(20))
      } { case Indexing(minIndex, limit) => minIndex.some -> limit.some }

  def ordering: EndpointInput[SortOrder] =
    query[Option[SortOrder]]("ordering")
      .map(_ getOrElse SortOrder.Desc) { ordering =>
        ordering.some
      }
}
