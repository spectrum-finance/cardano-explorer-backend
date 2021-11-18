package io.ergolabs.cardano.explorer.core

import cats.Applicative
import cats.syntax.either._
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import doobie.util.{Get, Put}
import eu.timepit.refined.collection._
import eu.timepit.refined.predicates.all.{And, Equal}
import eu.timepit.refined.refineV
import eu.timepit.refined.string.HexStringSpec
import io.ergolabs.cardano.explorer.core.types.specs.{Hash32Spec, HexStringR}
import io.estatico.newtype.macros.newtype
import sttp.tapir.json.circe._
import sttp.tapir.{Codec, CodecFormat, DecodeResult, Schema, Validator}
import tofu.Throws
import tofu.logging.derivation.loggable
import tofu.syntax.monadic._
import tofu.syntax.raise._

import scala.util.Try

object types {

  object specs {
    val HexStringR = "^(([0-9a-f]+)|([0-9A-F]+))$"
    type Hash32Spec = HexStringSpec And Size[Equal[64]]
  }

  @derive(loggable, encoder, decoder)
  @newtype case class Bytea(value: String)

  object Bytea {
    implicit val put: Put[Bytea] = deriving
    implicit val get: Get[Bytea] = deriving

    implicit def schema: Schema[Bytea] =
      Schema.schemaForString.description("Byte Array").asInstanceOf[Schema[Bytea]]
  }

  @derive(loggable, encoder, decoder)
  @newtype case class Asset32(value: String)

  object Asset32 {
    implicit def plainCodec: Codec.PlainCodec[Asset32] = deriving

    implicit val put: Put[Asset32] = deriving
    implicit val get: Get[Asset32] = deriving

    implicit def schema: Schema[Asset32] =
      Schema.schemaForString.description("Asset 32").asInstanceOf[Schema[Asset32]]

    def fromStringUnsafe(s: String): Asset32 = Asset32(s)

    val Ada: Asset32 = fromStringUnsafe("")
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

    implicit def schema: Schema[Hash28] =
      Schema.schemaForString.description("Hash 28").asInstanceOf[Schema[Hash28]]
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
  @newtype case class LeaderHash(value: String)

  object LeaderHash {
    implicit val put: Put[LeaderHash] = deriving
    implicit val get: Get[LeaderHash] = deriving

    implicit def schema: Schema[LeaderHash] =
      Schema.schemaForString.description("Leader Hash").asInstanceOf[Schema[LeaderHash]]
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
      Schema.schemaForString.description("Transaction Hash").asInstanceOf[Schema[TxHash]]

    implicit def validator: Validator[TxHash] =
      Validator.pass

    def fromString[F[_]: Throws: Applicative](s: String): F[TxHash] =
      refineV[Hash32Spec](s)
        .leftMap(e => new Exception(e))
        .toRaise[F]
        .map(_ => TxHash(s))

    def fromStringUnsafe(s: String): TxHash = TxHash(s)
  }

  @derive(loggable, encoder, decoder)
  @newtype case class OutRef private[types] (value: String)

  object OutRef {
    implicit def plainCodec: Codec.PlainCodec[OutRef] = deriving

    implicit def jsonCodec: Codec.JsonCodec[OutRef] = deriving

    implicit def schema: Schema[OutRef] =
      Schema.schemaForString.description("Modifier ID").asInstanceOf[Schema[OutRef]]

    implicit def validator: Validator[OutRef] =
      Validator.pass

    def apply(txHash: TxHash, index: Int): OutRef =
      OutRef(s"$txHash:$index")

    def unapply(ref: OutRef): Option[(TxHash, Int)] =
      Try {
        val Array(hash, i) = ref.value.split("#")
        TxHash.fromStringUnsafe(hash) -> i.toInt
      }.toOption
  }

  @derive(loggable, encoder, decoder)
  @newtype case class PolicyId(value: String)

  object PolicyId {
    implicit val put: Put[PolicyId] = deriving
    implicit val get: Get[PolicyId] = deriving

    implicit def schema: Schema[PolicyId] =
      Schema.schemaForString.description("Minting policy ID").asInstanceOf[Schema[PolicyId]]

    def fromStringUnsafe(s: String): PolicyId = PolicyId(s)

    val Ada: PolicyId = fromStringUnsafe("")
  }

  @derive(loggable, encoder, decoder)
  @newtype case class AssetRef private[types] (value: String) {
    def policyId: PolicyId = PolicyId.fromStringUnsafe(value.split(AssetRef.RefSeparator)(0))
    def name: Asset32      = Asset32.fromStringUnsafe(value.split(AssetRef.RefSeparator)(1))
  }

  object AssetRef {

    implicit def plainCodec: Codec.PlainCodec[AssetRef] = deriving

    implicit def jsonCodec: Codec.JsonCodec[AssetRef] = deriving

    implicit def schema: Schema[AssetRef] =
      Schema.schemaForString.description("Modifier ID").asInstanceOf[Schema[AssetRef]]

    implicit def validator: Validator[AssetRef] =
      Validator.pass

    val RefSeparator = "."

    def apply(policy: PolicyId, assetName: Asset32): AssetRef =
      AssetRef(s"$policy.$assetName")

    def unapply(ref: AssetRef): Option[(PolicyId, Asset32)] =
      Try {
        val Array(hash, name) = ref.value.split(RefSeparator)
        PolicyId.fromStringUnsafe(hash) -> Asset32.fromStringUnsafe(name)
      }.toOption
  }

  @derive(loggable, encoder, decoder)
  @newtype case class Addr(value: String)

  object Addr {
    implicit val put: Put[Addr] = deriving
    implicit val get: Get[Addr] = deriving

    implicit def plainCodec: Codec.PlainCodec[Addr] = deriving

    implicit def jsonCodec: Codec.JsonCodec[Addr] = deriving

    implicit def schema: Schema[Addr] =
      Schema.schemaForString.description("Address").asInstanceOf[Schema[Addr]]

    implicit def validator: Validator[Addr] =
      Validator.pass
  }

  @derive(loggable, encoder, decoder)
  @newtype case class PaymentCred(value: String)

  object PaymentCred {
    implicit val put: Put[PaymentCred] = deriving
    implicit val get: Get[PaymentCred] = deriving

    implicit def plainCodec: Codec.PlainCodec[PaymentCred] = deriving

    implicit def jsonCodec: Codec.JsonCodec[PaymentCred] = deriving

    implicit def schema: Schema[PaymentCred] =
      Schema.schemaForString
        .description("Serialized PaymentCredential represented as a HEX string")
        .asInstanceOf[Schema[PaymentCred]]

    implicit def validator: Validator[PaymentCred] =
      Validator.pattern[String](HexStringR).contramap(_.value)
  }

  private def deriveCodec[A, CF <: CodecFormat, T](
    at: A => Either[Throwable, T],
    ta: T => A
  )(implicit c: Codec[String, A, CF]): Codec[String, T, CF] =
    c.mapDecode { x =>
      at(x).fold(DecodeResult.Error(x.toString, _), DecodeResult.Value(_))
    }(ta)
}
