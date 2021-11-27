package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.ergolabs.cardano.explorer.api.v1.instances._
import io.ergolabs.cardano.explorer.core.types.{Asset32, PolicyId}
import sttp.tapir.Schema

@derive(encoder, decoder)
final case class AssetInfo(
  policyId: PolicyId,
  name: Asset32,
  quantity: BigInt,
  jsQuantity: String
)

object AssetInfo {

  implicit val schema: Schema[AssetInfo] =
    Schema
      .derived[AssetInfo]
      .modify(_.policyId)(_.description("The asset minting policy id"))
      .modify(_.name)(_.description("The asset name"))
      .modify(_.quantity)(_.description("Current emission of the asset"))
      .modify(_.jsQuantity)(_.description("Current emission of the asset (Encoded as a string)"))
}
