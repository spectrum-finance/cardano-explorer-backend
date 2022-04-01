package io.ergolabs.cardano.explorer.core.gateway

sealed trait GatewayError extends Throwable

object GatewayError {
  final case class UnexpectedError(description: String) extends GatewayError
  final case class GatewayResponseDecodingFailure(description: String) extends GatewayError
}
