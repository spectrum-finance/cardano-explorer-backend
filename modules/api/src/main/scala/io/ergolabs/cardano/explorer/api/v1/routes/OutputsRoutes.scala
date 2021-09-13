package io.ergolabs.cardano.explorer.api.v1.routes

import cats.effect.{Concurrent, ContextShift, Timer}
import io.ergolabs.cardano.explorer.api.v1.endpoints.OutputsEndpoints
import io.ergolabs.cardano.explorer.api.v1.services.Outputs
import io.ergolabs.cardano.explorer.api.v1.syntax._
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}

final class OutputsRoutes[F[_]: Concurrent: ContextShift: Timer](implicit
  service: Outputs[F],
  opts: Http4sServerOptions[F, F]
) {

  private val endpoints = new OutputsEndpoints
  import endpoints._

  private val interpreter = Http4sServerInterpreter(opts)

  def routes: HttpRoutes[F] = getByOutRefR

  def getByOutRefR: HttpRoutes[F] =
    interpreter.toRoutes(getByOutRef)(ref => service.getByOutRef(ref).orNotFound(s"Output{ref=$ref}"))
}

object OutputsRoutes {

  def make[F[_]: Concurrent: ContextShift: Timer](implicit
    service: Outputs[F],
    opts: Http4sServerOptions[F, F]
  ): HttpRoutes[F] =
    new OutputsRoutes().routes
}
