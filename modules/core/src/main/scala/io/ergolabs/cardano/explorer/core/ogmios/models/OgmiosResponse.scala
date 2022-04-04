package io.ergolabs.cardano.explorer.core.ogmios.models

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class OgmiosResponse[Body <: OgmiosResponseBody](
  `type`: String,
  version: String,
  servicename: String,
  methodname: String,
  result: Body,
  reflection: Option[String]
)

object OgmiosResponse {

  implicit def encoder[Body <: OgmiosResponseBody](implicit bodyEncoder: Encoder[Body]): Encoder[OgmiosResponse[Body]] =
    deriveEncoder

  implicit def decoder[Body <: OgmiosResponseBody](implicit bodyDecoder: Decoder[Body]): Decoder[OgmiosResponse[Body]] =
    deriveDecoder
}
