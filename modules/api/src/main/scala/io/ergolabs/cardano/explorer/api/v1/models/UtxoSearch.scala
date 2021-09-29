package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.ergolabs.cardano.explorer.core.types.{Addr, Asset32}
import sttp.tapir.Schema

@derive(encoder, decoder)
final case class UtxoSearch(
  address: Addr,
  assets: List[Asset32]
)

object UtxoSearch {

  implicit val schema: Schema[UtxoSearch] = Schema.derived
}
