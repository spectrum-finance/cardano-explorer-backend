package io.ergolabs.cardano.explorer.core

import cats.syntax.either._
import cats.Applicative
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import doobie.util.{Get, Put}
import eu.timepit.refined.api.{Refined, Validate}
import eu.timepit.refined.refineV
import eu.timepit.refined.string.HexStringSpec
import eu.timepit.refined.types.string.HexString
import io.estatico.newtype.macros.newtype
import sttp.tapir.{Codec, CodecFormat, DecodeResult, Schema, Validator}
import tofu.Throws
import tofu.logging.derivation.loggable
import tofu.syntax.monadic._
import tofu.syntax.raise._
import sttp.tapir.json.circe._
import eu.timepit.refined.collection._
import eu.timepit.refined.predicates.all.{And, Equal, Greater, Less, Not}
import io.ergolabs.cardano.explorer.core.types.specs.Hash32Spec

object types {

  object specs {
    type Hash32Spec = HexStringSpec And Size[Equal[32]]
  }

  @derive(loggable, encoder, decoder)
  @newtype case class Bytea(value: String)

  object Bytea {
    implicit val put: Put[Bytea] = deriving
    implicit val get: Get[Bytea] = deriving
  }

  @derive(loggable, encoder, decoder)
  @newtype case class Hash32(value: String)

  object Hash32 {
    implicit val put: Put[Hash32] = deriving
    implicit val get: Get[Hash32] = deriving

    implicit def schema: Schema[Hash32] =
      Schema.schemaForString.description("Hash 32").asInstanceOf[Schema[Hash32]]
  }

  @derive(loggable, encoder, decoder)
  @newtype case class Hash28(value: String)

  object Hash28 {
    implicit val put: Put[Hash28] = deriving
    implicit val get: Get[Hash28] = deriving
  }

  @derive(loggable, encoder, decoder)
  @newtype case class BlockHash(value: String)

  object BlockHash {
    implicit val put: Put[BlockHash] = deriving
    implicit val get: Get[BlockHash] = deriving

    implicit def schema: Schema[BlockHash] =
      Schema.schemaForString.description("Block Hash").asInstanceOf[Schema[BlockHash]]
  }

  @derive(loggable, encoder, decoder)
  @newtype case class TxHash(value: String)

  object TxHash {
    implicit val put: Put[TxHash] = deriving
    implicit val get: Get[TxHash] = deriving

    implicit def plainCodec: Codec.PlainCodec[TxHash] =
      deriveCodec[String, CodecFormat.TextPlain, TxHash](
        fromString[Either[Throwable, *]](_),
        _.value
      )

    implicit def jsonCodec: Codec.JsonCodec[TxHash] =
      deriveCodec[String, CodecFormat.Json, TxHash](
        fromString[Either[Throwable, *]](_),
        _.value
      )

    implicit def schema: Schema[TxHash] =
      Schema.schemaForString.description("Modifier ID").asInstanceOf[Schema[TxHash]]

    implicit def validator: Validator[TxHash] =
      Validator.pass

    def fromString[
      F[_]: Throws: Applicative
    ](s: String): F[TxHash] =
      refineV[Hash32Spec](s)
        .leftMap(e => new Exception(e))
        .toRaise[F]
        .map(_ => TxHash(s))
  }

  @derive(loggable, encoder, decoder)
  @newtype case class Addr(value: String)

  object Addr {
    implicit val put: Put[Addr] = deriving
    implicit val get: Get[Addr] = deriving

    implicit def schema: Schema[Addr] =
      Schema.schemaForString.description("Address").asInstanceOf[Schema[Addr]]
  }

  private def deriveCodec[A, CF <: CodecFormat, T](
    at: A => Either[Throwable, T],
    ta: T => A
  )(implicit c: Codec[String, A, CF]): Codec[String, T, CF] =
    c.mapDecode { x =>
      at(x).fold(DecodeResult.Error(x.toString, _), DecodeResult.Value(_))
    }(ta)
}
