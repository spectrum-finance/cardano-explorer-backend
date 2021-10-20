package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.ergolabs.cardano.explorer.core.types.{Addr, AssetRef}
import sttp.tapir.Schema

@derive(encoder, decoder)
final case class UtxoSearch(
  addr: Addr,
  containsAllOf: Option[List[AssetRef]],
  containsAnyOf: Option[List[AssetRef]]
)

object UtxoSearch {

  implicit val schema: Schema[UtxoSearch] =
    Schema
      .derived[UtxoSearch]
      .modify(_.addr)(_.description("Target address"))
      .modify(_.containsAllOf)(_.description("Should contain all assets in list"))
      .modify(_.containsAnyOf)(_.description("Should contain any of assets in list"))

}
