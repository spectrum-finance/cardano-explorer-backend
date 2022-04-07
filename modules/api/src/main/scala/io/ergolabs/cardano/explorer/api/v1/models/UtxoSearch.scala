package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.ergolabs.cardano.explorer.core.types.{Addr, AssetRef, PaymentCred}
import sttp.tapir.Schema

@derive(encoder, decoder)
final case class UtxoSearch(
  paymentCred: PaymentCred,
  containsAllOf: Option[List[AssetRef]],
  containsAnyOf: Option[List[AssetRef]]
)

object UtxoSearch {

  implicit val schema: Schema[UtxoSearch] =
    Schema
      .derived[UtxoSearch]
      .modify(_.paymentCred)(_.description("Target address credential"))
      .modify(_.containsAllOf)(_.description("Should contain all assets in list"))
      .modify(_.containsAnyOf)(_.description("Should contain any of assets in list"))
}
