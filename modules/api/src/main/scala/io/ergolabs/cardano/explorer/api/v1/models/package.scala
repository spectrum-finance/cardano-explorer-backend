package io.ergolabs.cardano.explorer.api.v1

import io.circe.{Decoder, Encoder}
import io.ergolabs.cardano.explorer.core.db.models.AssetOutput
import io.ergolabs.cardano.explorer.core.db.models.Asset
import io.ergolabs.cardano.explorer.core.types.{Asset32, PolicyId}
import io.estatico.newtype.macros.newtype
import sttp.tapir.Schema
import cats.syntax.option._
import java.text.SimpleDateFormat

package object models {

  type Value = List[OutAsset]

  object Value {

    def apply(lovelace: BigInt, assets: List[Asset]): List[OutAsset] =
      OutAsset(PolicyId.Ada, Asset32.Ada, "", lovelace, lovelace.toString()) +:
      assets.map(a => OutAsset(a.policy, a.name, a.nameHex, a.quantity, a.quantity.toString()))
  }

  @newtype final case class NetworkName(value: String)

  object NetworkName {

    implicit val encoder: Encoder[NetworkName] = deriving
    implicit val decoder: Decoder[NetworkName] = deriving

    implicit val schema: Schema[NetworkName] = Schema.schemaForString.map(NetworkName(_).some)(_.value)
  }

  @newtype final case class SystemStart(value: String)

  object SystemStart {

    def fromExplorer(str: String): SystemStart = {
      val formatterFrom = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
      val parsedDate    = formatterFrom.parse(str)
      val formatterTo   = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ")
      SystemStart(formatterTo.format(parsedDate))
    }

    implicit val encoder: Encoder[SystemStart] = deriving
    implicit val decoder: Decoder[SystemStart] = deriving

    implicit val schema: Schema[SystemStart] = Schema.schemaForString.map(SystemStart(_).some)(_.value)
  }
}
