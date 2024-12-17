package io.ergolabs.cardano.explorer.api.graphite

import cats.Monad
import cats.data.{Kleisli, OptionT}
import cats.effect.Clock
import org.http4s.HttpRoutes

import java.util.concurrent.TimeUnit

object MetricsMiddleware {

  def make[F[_]: Monad: Clock](implicit
    metrics: Metrics[F]
  ): MetricsMiddleware[F] =
    new MetricsMiddleware[F](metrics)

  final class MetricsMiddleware[F[_]: Monad: Clock](metrics: Metrics[F]) {

    // todo: temporal solution to prevent spamming keys with outputs.

    val outputsRegex = "[\"outputs.\"].*".r

    def middleware(routes: HttpRoutes[F]): HttpRoutes[F] = Kleisli { req =>
      val key = outputsRegex.replaceAllIn(req.pathInfo
        .renderString
        .replaceAll("/", ".").replaceAll(":", ".")
        .drop(1), ".outputs")

      for {
        start  <- OptionT.liftF(Clock[F].realTime(TimeUnit.MILLISECONDS))
        resp   <- routes(req)
        finish <- OptionT.liftF(Clock[F].realTime(TimeUnit.MILLISECONDS))
        _      <- OptionT.liftF(metrics.sendTs(key, (finish - start).toDouble))
        _      <- OptionT.liftF(metrics.sendCount(key, 1.toDouble))
      } yield resp
    }
  }
}
