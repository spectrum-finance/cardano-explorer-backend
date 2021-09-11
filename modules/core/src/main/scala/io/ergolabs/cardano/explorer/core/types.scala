package io.ergolabs.cardano.explorer.core

import derevo.derive
import doobie.util.{Get, Put}
import io.estatico.newtype.macros.newtype
import tofu.logging.derivation.loggable

object types {

  @derive(loggable)
  @newtype case class Bytea(value: String)

  object Bytea {
    implicit val put: Put[Bytea] = deriving
    implicit val get: Get[Bytea] = deriving
  }

  @derive(loggable)
  @newtype case class Hash32(value: String)

  object Hash32 {
    implicit val put: Put[Hash32] = deriving
    implicit val get: Get[Hash32] = deriving
  }

  @derive(loggable)
  @newtype case class Hash28(value: String)

  object Hash28 {
    implicit val put: Put[Hash28] = deriving
    implicit val get: Get[Hash28] = deriving
  }

  @derive(loggable)
  @newtype case class BlockHash(value: String)

  object BlockHash {
    implicit val put: Put[BlockHash] = deriving
    implicit val get: Get[BlockHash] = deriving
  }

  @derive(loggable)
  @newtype case class TxHash(value: String)

  object TxHash {
    implicit val put: Put[TxHash] = deriving
    implicit val get: Get[TxHash] = deriving
  }

  @derive(loggable)
  @newtype case class Addr(value: String)

  object Addr {
    implicit val put: Put[Addr] = deriving
    implicit val get: Get[Addr] = deriving
  }
}
