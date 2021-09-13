package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import sttp.tapir.{Schema, Validator}

@derive(encoder, decoder)
final case class Items[A](items: List[A], total: Int)

object Items {

  def empty[A]: Items[A] = Items(List.empty[A], 0)

  implicit def schema[A: Schema]: Schema[Items[A]]       = Schema.derived[Items[A]]
  implicit def validator[A: Schema]: Validator[Items[A]] = schema.validator
}
