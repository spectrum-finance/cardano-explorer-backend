package io.ergolabs.cardano.explorer.core.ogmios.models

import enumeratum._
import io.circe.{Decoder, Encoder}

sealed trait OgmiosResponseBody extends EnumEntry

object OgmiosResponseBody extends Enum[OgmiosResponseBody] {

  final case class EraSummaries(infoList: List[EraInfo]) extends OgmiosResponseBody
  object EraSummaries {
    implicit val encoder: Encoder[EraSummaries] = Encoder[List[EraInfo]].contramap(_.infoList)
    implicit val decoder: Decoder[EraSummaries] = Decoder[List[EraInfo]].map(EraSummaries(_))
  }

  val values = findValues
}
