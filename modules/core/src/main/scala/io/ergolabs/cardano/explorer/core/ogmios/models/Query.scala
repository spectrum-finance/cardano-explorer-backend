package io.ergolabs.cardano.explorer.core.ogmios.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}
import io.ergolabs.cardano.explorer.core.ogmios.models.Query.QueryName

@derive(encoder, decoder)
final case class Query(query: QueryName)

object Query {

  sealed abstract class QueryName(val value: String) extends StringEnumEntry

  object QueryName extends StringEnum[QueryName] with StringCirceEnum[QueryName] {
    case object EraSummaries extends QueryName("eraSummaries")

    val values: IndexedSeq[QueryName] = findValues
  }
}
