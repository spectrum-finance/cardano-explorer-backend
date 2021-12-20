package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.ergolabs.cardano.explorer.api.v1.instances._
import io.ergolabs.cardano.explorer.core.types.{Asset32, AssetRef, PolicyId}
import sttp.tapir.Schema

@derive(encoder, decoder)
final case class OutAsset(
  policyId: PolicyId,
  name: Asset32,
  quantity: BigInt,
  jsQuantity: String
)

object OutAsset {

  implicit def schema: Schema[OutAsset] =
    Schema
      .derived[OutAsset]
      .modify(_.policyId)(_.description("The Asset policy hash."))
      .modify(_.name)(_.description("The Asset name."))
      .modify(_.quantity)(_.description("The Asset quantity."))
}
