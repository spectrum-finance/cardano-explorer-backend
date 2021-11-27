package io.ergolabs.cardano.explorer.api.v1

import io.ergolabs.cardano.explorer.core.db.models.Asset
import io.ergolabs.cardano.explorer.core.types.{Asset32, PolicyId}

package object models {

  type Value = List[OutAsset]

  object Value {

    def apply(lovelace: BigInt, assets: List[Asset]): List[OutAsset] =
      OutAsset(PolicyId.Ada, Asset32.Ada, lovelace, lovelace.toString()) +:
      assets.map(a => OutAsset(a.policy, a.name, a.quantity, a.quantity.toString()))
  }
}
