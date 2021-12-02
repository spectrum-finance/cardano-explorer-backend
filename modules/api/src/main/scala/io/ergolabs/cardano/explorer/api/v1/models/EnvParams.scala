package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.Json
import io.ergolabs.cardano.explorer.api.v1.instances._
import io.ergolabs.cardano.explorer.core.types.{Bytea, PoolId}
import sttp.tapir.Schema

@derive(encoder, decoder)
final case class EnvParams(
  pparams: ProtocolParams,
  network: NetworkName,
  sysstart: SystemStart,
  pools: List[PoolId],
  collateralPercent: Option[Int]
)

object EnvParams {

  implicit def schema: Schema[EnvParams] =
    Schema
      .derived[EnvParams]
      .modify(_.pparams)(_.description("Protocol parameters"))
      .modify(_.network)(_.description("Network Id"))
      .modify(_.sysstart)(_.description(""))
      .modify(_.pools)(_.description("Pools list"))
      .modify(_.collateralPercent)(_.description("Collateral Percent"))
}
