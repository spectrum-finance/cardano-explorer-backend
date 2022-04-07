package io.ergolabs.cardano.explorer.core.models

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.MatchesRegex
import eu.timepit.refined.{refineMV, W}
import io.ergolabs.cardano.explorer.core.models.Sorting.SortOrder
import sttp.tapir.{Codec, DecodeResult}

final case class Sorting(sortBy: String, order: SortOrder)

object Sorting {

  type OrderingSpec = MatchesRegex[W.`"^(?i)(asc|desc)$"`.T]

  type OrderingString = String Refined OrderingSpec

  sealed trait SortOrder {
    def value: OrderingString
    def unwrapped: String = value.value
  }

  object SortOrder {

    case object Asc extends SortOrder {
      override def toString: String = value.value
      def value: OrderingString     = refineMV("asc")
    }

    case object Desc extends SortOrder {
      override def toString: String = value.value
      def value: OrderingString     = refineMV("desc")
    }

    implicit val codec: Codec.PlainCodec[SortOrder] = Codec.string
      .mapDecode(fromString)(_.toString)

    private def fromString(s: String): DecodeResult[SortOrder] =
      s.trim.toLowerCase match {
        case "asc"  => DecodeResult.Value(Asc)
        case "desc" => DecodeResult.Value(Desc)
        case other  => DecodeResult.Mismatch("`asc` or `desc`", other)
      }
  }
}
