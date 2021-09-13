package io.ergolabs.cardano.explorer.api.v1

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive

@derive(encoder, decoder)
sealed trait HttpError

object HttpError {
  final case class NotFound(what: String) extends HttpError
  final case class Unknown(code: Int, msg: String) extends HttpError
  case object NoContent extends HttpError
}
