package io.ergolabs.cardano.explorer.core.ogmios.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}
import io.ergolabs.cardano.explorer.core.ogmios.models.Query
import io.ergolabs.cardano.explorer.core.ogmios.models.Query.QueryName

@derive(encoder, decoder)
final case class OgmiosRequest(
  `type`: String,
  version: String,
  servicename: String,
  methodname: String,
  args: Query
)

object OgmiosRequest {

  def makeRequest(query: QueryName): OgmiosRequest = OgmiosRequest(
    "jsonwsp/request",
    "1.0",
    "ogmios",
    "Query",
    Query(query)
  )
}
